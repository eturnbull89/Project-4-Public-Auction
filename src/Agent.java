import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.UnknownObjectException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @autho Tristin Glunt | tglunt@unm.edu
 * =============================================
 */
public class Agent
{
    private String agentName;   //to have 100% unique agent IDs I think the names would have to be made from a server
    private Account bankAccount;    //given from a bank
    private String biddingKey;      //given when registered with auction central
    //private AuctionCentral
    //private AuctionHouse

    /**
     * So an agent needs to be it's own client, so it will have it's own Main method and not a
     * constructor. The client will somehow know the hostname of the Bank server and it will be the
     * first argument when the Agent(client) program is launched, it will first pass the Bank the bankAccount
     * object, letting the Bank know it wants to be registered, the Bank then returns the bankAccount object
     * with the correctly filled out fields.
     *
     *
     * I've wrote a bit of code here, mainly to just work through the process of how the project will run. If any of the
     * code seems really wrong, I don't mind rewriting it all. Lots of the Data Types for the objects are temporary,
     * I just didn't feel like making the Auction Central class, Auction House class, or Auction item class; used Strings
     * to represent them for now but can easily swap out later.
     */

    public static void main(String[] args) throws IOException, UnknownObjectException, ClassNotFoundException
    {
        String hostName = args[0];  //host name will be first argument
        int bankPortNumber = Integer.parseInt(args[1]); //port number for bank will be 2nd
        int auctioCenPortNumber = Integer.parseInt(args[2]);  //port number for auction central will be third
        Agent agent = new Agent();

        try(
            Socket agentBankSocket = new Socket(hostName, bankPortNumber);  //setup bank socket
            Socket agentAuctionCentralSocket = new Socket(hostName, auctioCenPortNumber);
            ObjectOutputStream outBank = new ObjectOutputStream(agentBankSocket.getOutputStream()); //setup object output first
            ObjectInputStream inBank = new ObjectInputStream(agentBankSocket.getInputStream()); //setup object input next
            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());
        )
        {   //sets up bank account initially
            outBank.writeObject(agent.bankAccount); //write the bank account to the object for the Bank to read
            outBank.flush();
            agent.bankAccount = (Account) inBank.readObject(); //read from the socket the bank account from the bank

            //then auction central register
            outAuctionCen.writeObject(agent.bankAccount.getBankKey());
            outAuctionCen.writeObject(agent.agentName);
            outAuctionCen.flush();
            agent.biddingKey = (String) inAuctionCen.readObject();
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

        /*
            idea: while loop here to ask to read input from user,
            can read if the user wants to talk to auctionHouse,
            will then call AutionHouse method that talks to AuctionHouse server
         */
        agent.pollUserInput(hostName, auctioCenPortNumber);
    }

    /**
     * pollUserInput: asks the user if they would like to see the list of auction houses
     * @param hostName
     * @param auctionCenPortNumber
     * @throws UnknownObjectException
     * @throws IOException
     */
    public void pollUserInput(String hostName, int auctionCenPortNumber) throws UnknownObjectException, IOException
    {
        Scanner sc = new Scanner(System.in);
        String input = sc.next();
        while(!input.equals("Exit"))
        {
            System.out.println("Would you like to " +
                    "(1) see list of auction houses ");
            input = Integer.toString(sc.nextInt());
            if(input.equals("1"))
            {
                auctionCentralAuctionHouses(hostName, auctionCenPortNumber);  //will talk over socket to auction central
            }

        }
    }

    /**
     * auctionCentralAuctionHouses: this method will communicate with the auction central over a socket to get the list
     * of auction houses. once the auction central retruns the list of auction houses, we will ask the agent
     * which house they are wanting to join, we will then join that house
     * @param hostName
     * @param portNumber
     * @throws UnknownObjectException
     * @throws IOException
     */
    public void auctionCentralAuctionHouses(String hostName, int portNumber) throws UnknownObjectException, IOException
    {
        try(
            Socket agentAuctionCentralSocket = new Socket(hostName, portNumber);
            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());
        )
        {

            String requestForAuctionHouses = "AuctionHouses";
            outAuctionCen.writeObject(requestForAuctionHouses);
            outAuctionCen.flush();
            ArrayList<String> listOfAuctionHouses = (ArrayList<String>)inAuctionCen.readObject();   //would be a list of AuctionHouses but set to String for now
            printListOfAuctionHouses(listOfAuctionHouses);
            System.out.println("Which auction house would you like to join?");
            Scanner sc = new Scanner(System.in);
            int auctionHouseNum = sc.nextInt();
            joinAuctionHouse(listOfAuctionHouses, auctionHouseNum, hostName);

        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * joinAuctionHouse: once a user joins an auction house, will display the list of auction items.
     * We will then ask the agent which item they're interested in bidding for. Once we get the item they're bidding
     * for, we will display how much the current bid for that item is. We will then write to the auction house our
     * bidding key, the item we're bidding on, and the amount we're bidding. The bidding process may be put into it's
     * own method later, and be in a while loop to go until the itemBiddingOn timer runs out.
     * @param listOfAuctionHouses
     * @param auctionHouseNum
     * @param hostname
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void joinAuctionHouse(ArrayList<String> listOfAuctionHouses, int auctionHouseNum, String hostname) throws IOException, ClassNotFoundException
    {
        String auctionHouse = listOfAuctionHouses.get(auctionHouseNum);  //get the auction house we're joining. i think this will really just be a port number for a socket so we can communicate

        Socket auctionHouseSocket = new Socket(hostname, Integer.parseInt(auctionHouse));   //this is all temp with the actual values, just setting up a skeleton
        ObjectOutputStream outAuctionHouse = new ObjectOutputStream(auctionHouseSocket.getOutputStream());
        ObjectInputStream inAuctionHouse = new ObjectInputStream(auctionHouseSocket.getInputStream());

        String requestForAuctionItems = "AuctionItems";
        outAuctionHouse.writeObject(requestForAuctionItems);
        outAuctionHouse.flush();
        ArrayList<String> listOfAuctionItems = (ArrayList<String>)inAuctionHouse.readObject();  //will be AuctionItem objects not strings
        printListOfAuctionItems(listOfAuctionItems);   //will need another print method because different objects then auction houses
        System.out.println("Which auction item would you like to bid on?");
        Scanner sc = new Scanner(System.in);
        int itemNumber = sc.nextInt();

        String itemBiddingOn = listOfAuctionItems.get(itemNumber);
        //itemBiddingOn.getCurrentBid   //will display the current bid for the item they're bidding on
        System.out.println("How much would you like to bid?");
        int bidAmount = sc.nextInt();
        outAuctionHouse.writeObject(biddingKey);
        outAuctionHouse.writeObject(itemBiddingOn);
        outAuctionHouse.writeObject(bidAmount);


    }

    public void printListOfAuctionItems(ArrayList<String> auctionItems)
    {
        int counter = 0;
        for(String ah : auctionItems)
        {
            counter++;
            System.out.println(counter + ". " + ah);
        }
    }

    public void printListOfAuctionHouses(ArrayList<String> auctionHouses)
    {
        int counter = 0;
        for(String ah : auctionHouses)
        {
            counter++;
            System.out.println(counter + ". " + ah);
        }
    }


}