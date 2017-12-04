import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Eric Turnbull  | eturnbull@unm.edu
 *          Zach Fleharty  |
 *          Tristin Glunt  | tglunt@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * AuctionHouse is used to communicate with Agents and allow them to bid on items stored in the auction house. Each
 * AuctionHouse has a number of variables that are set from the command line. houseHost is a string that contains the
 * host name that his auction house will be located at. housePort is the port number for this auction house. houseName
 * is the visual name of an auction house, i.e. if the house is selling art it could be named Art. centralPort is the
 * port number that auction central is located at. centralHost is the hostname that auction central is located at. The
 * above variables are set via command line arguments. houseReg is used to hold the Confirmation that a house registered
 * with auction central. listIndex is used to indicate where the sales list should start. items stores the AuctionItems
 * this auction house will have for auction.
 */
public class AuctionHouse
{
    private String houseHost;

    private int housePort;

    private String houseName;

    private int centralPort;

    private String centralHost;

    private Confirmation houseReg;

    private int listIndex = 0;

    private ArrayList<AuctionItem> items;

    /**
     * @param args - String of arguments needed for an auction house to function.
     *             args[0] is used to set the host name of the auction house.
     *             args[1] is used to set the port number of the auction house.
     *             args[2] is the visual name for the auction house.
     *             args[3] is the port number Auction Central is located at.
     *             args[4] is the host name that Auction Central is located at.
     *             args[5] is used to indicate what items this house should sell, 1 = art, 2 = books, and 3 = cars.
     *             Anything else will default to 1 and list art.
     * @throws IOException - Couldn't listen on the given house port number.
     * main is an AuctionHouses starting point. It first checks that the number of arguments given in the command line
     * is at least 6. If not it prints an error message and exits. If it is it then creates a new AuctionHouse object
     * and sets its variables to the parameters passed. The last argument is used to specify which items from the
     * salesList this auction house should show. If the value set is not between 1 and 3 it defaults to a value of 1 to
     * sell art items. It then creates a variable to hold the auction houses server socket and a boolean that will
     * indicates if the house was registered with auction central. It then tries to set the server socket variable to the
     * port number passed in args[1], create a socket with auction central using the given host name and port number,
     * and open input/output streams with auction central. It then enters a while loop that will run until the auction
     * house is closed. It checks if the house has been registered yet, if not it creates a new registration object
     * and sends it to auction central via the centralOut output stream. Once it receives a confirmation back it sets
     * the value of houseReg to the confirmation returned and then sets the value of registered to true to prevent
     * re-registration. It then starts to accept Agents start their bidding. When an Agent connects to an auction house
     * it creates a MiniHouse that runs on its own thread to service the agent and starts that thread.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length != 6)
        {
            System.err.println("Not enough or too many initial arguments");

            System.exit(1);
        }

        AuctionHouse house = new AuctionHouse();

        house.houseHost = args[0];

        house.housePort = Integer.parseInt(args[1]);

        house.houseName = args[2];

        house.centralPort = Integer.parseInt(args[3]);

        house.centralHost = args[4];

        int index = Integer.parseInt(args[5]);

        if(index > 0 && index < 4)
        {
            switch (index)
            {
                case 1:
                    house.listIndex = 0;
                    break;
                case 2:
                    house.listIndex = 6;
                    break;
                case 3:
                    house.listIndex = 12;
                    break;
            }
        }

        else
        {
            System.out.println("invalid index given, setting to default");
        }

        ServerSocket serverSocket;

        boolean registered = false;

        try
        {
            serverSocket = new ServerSocket(house.housePort);

            Socket centralSocket = new Socket(house.centralHost, house.centralPort);

            ObjectOutputStream centralOut = new ObjectOutputStream(centralSocket.getOutputStream());

            ObjectInputStream centralIn = new ObjectInputStream(centralSocket.getInputStream());

            //noinspection InfiniteLoopStatement
            while(true)
            {
                if(!registered)
                {
                    house.register(house.houseHost, house.housePort, house.centralHost, centralOut,
                                   centralIn);

                    house.itemLists(house.houseReg);

                    registered = true;
                }

                Socket clientSocket = serverSocket.accept();

                MiniHouse mini = new MiniHouse(clientSocket, house.items, house.houseReg.getAuctionKey(),
                                               centralOut, centralIn);

                mini.start();
            }
        }
        catch (IOException e)
        {
            System.err.println("could not listen on port: "+ house.housePort);
        }
    }

    /**
     * @param houseHost - The host name of this auction house.
     * @param housePort - The port number this auction house is using.
     * @param centralHost - Name of auction Central host.
     * @param out - Object output stream to auction central.
     * @param in - Object input stream to auction central.
     * @throws IOException - Couldnt get get I/O  for the connection
     * register is used to register an auction house with auction central.  It starts by creating a new Registration
     * object that contains the name of an auction house, the port number of the auction house, and the host name of
     * the auction house and writes it to auction central via out. It then tries to set the houseReg field to the
     * Confirmation object that is returned by auction central. It then closes the object input and output streams. If
     * there were any errors it prints out a message corresponding to the error.
     */
    private void register(String houseHost, int housePort, String centralHost, ObjectOutputStream out,
                          ObjectInputStream in) throws IOException
    {
        Registration centralReg = new Registration(houseName, housePort, houseHost);

        try
        {
            out.flush();

            out.writeObject(centralReg);

            try
            {
                this.houseReg = (Confirmation) in.readObject();

                System.out.println("house Registered, id is = "+this.houseReg.getPublicId());

            }

            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }

        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + centralHost);

            System.exit(1);
        }

        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " + centralHost);

            System.exit(1);
        }
    }

    /**
     * @param houseReg - Confirmation returned by auction central, contains an auction houses public id and
     *                   its auction key.
     *itemsLists is used to create the list of items each auction house has for auction.  It starts by creating an
     *an array list of strings set to the value returned by readList(). It then starts a for loop that will add
     *three items to the houseList array list. In the loop it starts by setting the item name to the string at an
     *even value of listIndex and the minBid to an odd value of listIndex. It then uses these to create a new
     *AuctionItem object and add the newly created auction item to the houseList array list. It then updates
     *the value of listIndex by 2. Once the list has been created it sets the items list to the created list.
     */
    private void itemLists(Confirmation houseReg)
    {
        ArrayList<AuctionItem> houseList = new ArrayList<>();

        ArrayList<String> itemList = readList();

        int key = houseReg.getAuctionKey();

        Random serialNum = new Random(1000);

        for(int i = 0; i < 3; i++)
        {
            String itemName = itemList.get(listIndex);

            int minBid = Integer.parseInt(itemList.get(listIndex+1));

            AuctionItem listing = new AuctionItem(itemName, minBid, key, serialNum.nextInt(1000));

            houseList.add(i, listing);

            listIndex = listIndex + 2;
        }

        this.items = houseList;
    }

    /**
     * @return readList returns an array list of strings.
     * readList is used to convert the saleList text file to an array list of strings that will be used to create
     * auction items. It starts by creating an inputStream from the saleList text file. It then creates a buffered
     * reader to read from the created input stream. It then loops through each line from the input stream and adds
     * the read string to the itemList array list. Once done it closes the input stream and returns the itemList
     * array list.
     */
    private ArrayList<String> readList()
    {
        ArrayList<String> itemList = new ArrayList<>();

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("saleList");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(input)))
        {
            for(String line; (line = reader.readLine()) != null;)
            {
                itemList.add(line);
            }

            input.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return itemList;
    }
}
