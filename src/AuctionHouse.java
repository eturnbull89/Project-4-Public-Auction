import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AuctionHouse
{
    //Auction house server host name.
    private String houseHost;

    //Port number for this auction house
    private int housePort;

    //Name of this auction house
    private String houseName;

    //Port number of auction central
    private int centralPort;

    //Auction central server host name.
    private String centralHost;

    //Confirmation object, holds registration info set by auction central.
    private Confirmation houseReg;

    //Socket connection with auction central
    private Socket centralSocket;

    //Int used to keep track of the list index between different auction houses
    private static int listIndex = 0;

    private ArrayList<AuctionItem> items;

    private ObjectInputStream centralIn;

    private ObjectOutputStream centralOut;

    public static void main(String[] args) throws IOException
    {
        //Check that initial arguments number 4, if not throw an error message and exit
        if(args.length != 5)
        {
            System.err.println("Not enough or too many initial arguments");
            System.exit(1);
        }

        //Create a new auction house and set needed variables.
        AuctionHouse house = new AuctionHouse();

        //auction house server host name, first argument
        house.houseHost = args[0];

        //This auction houses port number, second argument
        house.housePort = Integer.parseInt(args[1]);

        //This auction houses given name, third argument
        house.houseName = args[2];

        //The port number for auction central, forth argument
        house.centralPort = Integer.parseInt(args[3]);

        //auction central server host name, fifth argument.
        house.centralHost = args[4];

        //Commented out until it can be tested.
        //Create a server socket for this auction house.
        ServerSocket serverSocket;

        //Boolean to keep the creating sockets for each agent that connects.
        boolean listeningSocket = true;

        boolean registered = false;

        try
        {
            //Create a server socket from the given house port number
            serverSocket = new ServerSocket(house.housePort);

            //Create the socket used to talk to the auction central
            Socket centralSocket = new Socket(house.centralHost, house.centralPort);

            //Create an object output stream from this auction house.
            ObjectOutputStream centralOut = new ObjectOutputStream(centralSocket.getOutputStream());

            //Create an object input stream from auction central
            ObjectInputStream centralIn = new ObjectInputStream(centralSocket.getInputStream());

            while(listeningSocket)
            {
                if(!registered)
                {
                    //Register with auction central and set houseReg field
                    house.register(house.houseHost, house.housePort, house.centralHost, house.centralPort, centralOut,
                                   centralIn);

                    //Set the house items list
                    house.itemLists(house.houseReg);

                    registered = true;
                }
                //Create a socket from the agent that connects to the auction house.
                Socket clientSocket = serverSocket.accept();

                //Create a miniHouse object for each agent that connects to auction central
                MiniHouse mini = new MiniHouse(clientSocket, centralSocket, house.items, house.houseReg.getAuctionKey(),
                                               centralOut, centralIn);

                //Start the miniHouse thread for each agent.
                mini.start();
            }

            serverSocket.close();
        }
        catch (IOException e)
        {
            System.err.println("could not listen on port: "+ house.housePort);
        }
    }

    //***********************************
    //String houseHost - The location that this auction house is located at, i.e. localhost or an ip address.
    //int housePort - port number that this auction house is located at.
    //String centralHost - The location that auction central is located at, i.e. localhost or an ip address.
    //int centralPort - the port number that auction central is located at.
    //register has no return value.
    //register is used to register an auction house with auction central.  It starts by creating a new Registration
    //object that contains the name of an auction house, the port number of the auction house, and the host name of
    //the auction house. Register then tries to open a socket to auction central using centralHost and centralPort,
    //and opening object input and output streams using the created socket. If it was able of open a socket with
    //auction central it then sets the centralSocket variable to the created socket and writes the Registration object
    //to auction central. It then tries to set the houseReg field to the Confirmation object that is returned by
    //auction central. It then closes the object input and output streams. If there were any errors it prints out
    //a message corresponding to the error.
    //***********************************
    private void register(String houseHost, int housePort, String centralHost, int centralPort, ObjectOutputStream out,
                          ObjectInputStream in) throws IOException
    {
        //Registration object that will be sent to auction central to register.
        Registration centralReg = new Registration(houseName, housePort, houseHost);

        try
        {
            //Needed statement
            out.flush();

            //Write to auction central the created registration.
            out.writeObject(centralReg);

            try
            {
                //Receive back the confirmation object and set houseReg.
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

    //***********************************
    //Confirmation houseReg - Confirmation returned by auction central, contains an auction houses public id and
    //                        its auction key.
    //itemLists returns an array list of auction items.
    //itemsLists is used to create the list of items each auction house has for auction.  It starts by creating an
    //an array list of strings set to the value returned by readList(). It then starts a for loop that will add
    //three items to the houseList array list. In the loop it starts by setting the item name to the string at an
    //even value of listIndex and the minBid to an odd value of listIndex. It then uses these to create a new
    //AuctionItem object and add the newly created auction item to the houseList array list. It then updates
    //the value of listIndex by 2.
    //***********************************
    private void itemLists(Confirmation houseReg)
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

        this.items = houseList;
    }

    //***********************************
    //readList returns an array list of strings.
    //readList is used to convert the saleList text file to an array list of strings that will be used to create
    //auction items. It starts by creating an inputStream from the saleList text file. It then creates a buffered
    //reader to read from the created input stream. It then loops through each line from the input stream and adds
    //the read string to the itemList array list. Once done it closes the input stream and returns the itemList
    //array list.
    //***********************************
    private ArrayList<String> readList()
    {
        //An array list of strings that will contain each items name and minimum bid amount given by the saleList
        ArrayList<String> itemList = new ArrayList<>();

        //Create and inputStream using the saleList.txt file.
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("saleList");

        //Create a buffered reader to read the from the input stream.
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(input)))
        {
            //Loop through each line from the input stream until it is null
            for(String line; (line = reader.readLine()) != null;)
            {
                //Add each line to the itemList
                itemList.add(line);
            }

            //Close the input stream after reading each line.
            input.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return itemList;
    }
}
