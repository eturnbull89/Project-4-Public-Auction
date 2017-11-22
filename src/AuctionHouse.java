import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AuctionHouse
{
    //Server host name
    private String hostName;

    //Port number for this auction house
    private int housePort;

    //Name of this auction house
    private String houseName;

    //Port number of auction central
    private int centralPort;

    //Confirmation object, holds registration info set by auction central.
    private Confirmation houseReg;

    //Socket connection with auction central
    private Socket centralSocket;

    //Int used to keep track of the list index between different auction houses
    private static int listIndex = 0;

    public static void main(String[] args) throws IOException
    {
        //Check that initial arguments number 4, if not throw an error message and exit
        if(args.length != 4)
        {
            System.err.println("Please retry and input the hostName, the auction house port number, auction house name, " +
                    "and auction central port number");
            System.exit(1);
        }

        //Create a new auction house and set needed variables.
        AuctionHouse house = new AuctionHouse();

        //Server host name, first argument
        house.hostName = args[0];

        //This auction houses port number, second argument
        house.housePort = Integer.parseInt(args[1]);

        //This auction houses given name, third argument
        house.houseName = args[2];

        //The port number for auction central, forth argument
        house.centralPort = Integer.parseInt(args[3]);

        //set up testing
        house.testingMethod();

        //Register with auction central and set houseReg field
        //house.register(house.hostName, house.housePort, house.centralPort);


    }

    private void testingMethod()
    {
        //Check that the server socket can be opened and print a message.
        try(
                ServerSocket serverSocket = new ServerSocket(housePort);
                )
        {
            System.out.println("Server socket opened");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Create a testing confirmation
        Confirmation testCon = new Confirmation(housePort, 2256);

        //Create an auction house item list to check that itemList works
        ArrayList<AuctionItem> houseItems = itemLists(testCon);

        //Print the command line arguments to check they are being set.
        System.out.println("hostName = "+hostName+", port = "+housePort+", house name = "+ houseName+", centralPort = "
                           + centralPort);

        //Print each of the items info to check that it was created, in this case the first 3 should be paintings
        for (int i = 0; i < 3; i ++)
        {
            System.out.println("Auction house id = "+houseItems.get(i).getAuctionHouseId());
            System.out.println("Item "+houseItems.get(i).getName());
            System.out.println("Item id = "+houseItems.get(i).getItemId());
            System.out.println("Minimum bid = "+houseItems.get(i).getMinimumBid());
            System.out.println("Current bid = "+houseItems.get(i).getCurrentBid());
        }

        //Check if the current bit can be set using the key, will currently allow bids under min bid
        houseItems.get(2).setCurrentBid(3, testCon.getAuctionKey());
        System.out.println("Auction house id = "+houseItems.get(2).getAuctionHouseId());
        System.out.println("Item "+houseItems.get(2).getName());
        System.out.println("Item id = "+houseItems.get(2).getItemId());
        System.out.println("Minimum bid = "+houseItems.get(2).getMinimumBid());
        System.out.println("Current bid = "+houseItems.get(2).getCurrentBid());
    }

    private void register(String hostName, int housePort, int centralPort) throws IOException
    {
        //Registration object that will be sent to auction central to register.
        Registration centralReg = new Registration(houseName, housePort, hostName);

        try(
                //Create the socket to talk to the auction central
                Socket centralSocket = new Socket(hostName, centralPort);

                //Create an object output stream from this auction house.
                ObjectOutputStream outFromHouse = new ObjectOutputStream(centralSocket.getOutputStream());

                //Create an object input stream from auction central
                ObjectInputStream inFromCentral = new ObjectInputStream(centralSocket.getInputStream())
                )
        {
            //Set the centralSocket field to the created socket for use later.
            this.centralSocket = centralSocket;

            //Write to auction central the created registration.
            outFromHouse.writeObject(centralReg);

            try
            {
                //Receive back the registration object and set houseReg.
                this.houseReg = (Confirmation) inFromCentral.readObject();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                //Close the object input stream from auction central.
                inFromCentral.close();

                //Close the object output stream from this auction house.
                outFromHouse.close();
            }

        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    private ArrayList<AuctionItem> itemLists(Confirmation houseReg)
    {
        //List of each houses auction items. List that will be returned.
        ArrayList<AuctionItem> houseList = new ArrayList<>();

        //Array list of each string in the text file "sale list", used to build auction items.
        ArrayList<String> itemList = readList();

        //The auction houses id, assigned by auction central and held in houseReg variable.
        int houseId = houseReg.getPublicId();

        //The auction houses key, assigned by auction central and held in houseReg variable.
        int key = houseReg.getAuctionKey();

        //Create 3 new Auction items and added them to the item list.
        for(int i = 0; i < 3; i++)
        {
            //The name of the item, is at an even value of list index
            String itemName = itemList.get(listIndex);

            //The set minimum bid amount, is at an odd value of list index.
            int minBid = Integer.parseInt(itemList.get(listIndex+1));

            //Create a new auction item
            AuctionItem listing = new AuctionItem(houseId, itemName, i, minBid, key);

            //Add the auction item to the house list
            houseList.add(i, listing);

            //increase the static index variable so that other houses can work off a single list
            listIndex = listIndex + 2;
        }

        return houseList;
    }

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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return itemList;
    }
}
