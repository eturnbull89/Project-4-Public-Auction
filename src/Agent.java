import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.UnknownObjectException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  |
 *          Adam Spanswick |
 * =============================================
 */

public class Agent
{
    private String agentName = "Test1234";   //to have 100% unique agent IDs I think the names would have to be made from a server
    private AcctKey bankAccount;    //given from a bank
    private Integer biddingKey;      //given when registered with auction central
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
//        if(args.length != 5)
//        {
//            System.err.println("Please retry and input the bank hostName, auction central hostname, portNumber of the bank, portNumber "
//                                + "of the auction central and your user name.");
//            System.exit(1);
//        }

        Agent agent = new Agent();

        //String bankHostName = args[0];  //bank host name will be first argument
        //int bankPortNumber = Integer.parseInt(args[2]); //port number for bank will be 3rd arg
//        String bankHostName = "localhost";
//        int bankPortNumber = 1026;

        String auctionCentralHostName = "10.82.150.178"; //AC host name will be 2nd arg
        int auctionCenPortNumber = 1027;  //port number for auction central will be fourth

        try
        {
//            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            Socket agentAuctionCentralSocket = new Socket(auctionCentralHostName, auctionCenPortNumber);

//            ObjectOutputStream outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
//            outBank.flush();
//            ObjectInputStream inBank = new ObjectInputStream(agentBankSocket.getInputStream());

            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            outAuctionCen.flush();
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());

            //sets up bank account initially
//            UserAccount myAccount = new UserAccount(agent.agentName);
//            System.out.println("trying to write object to bank..");
//            outBank.writeObject(myAccount);
//            outBank.flush();
//            agent.bankAccount = (AcctKey) inBank.readObject();
//
//            System.out.println("read in acct key");
//            System.out.println(agent.bankAccount.getAccountNumber());
//            System.out.println(agent.bankAccount.getKey());

            //outAuctionCen.writeObject(agent.agentName);
            System.out.println("trying to write object to ac...");
            Integer tempIntObj = 123;
            outAuctionCen.writeObject(tempIntObj);
            outAuctionCen.flush();

            System.out.println("trying to read object from ac...");
            agent.biddingKey = (Integer) inAuctionCen.readObject();
            //agent.inquireBankBalance(agentBankSocket, inBank, outBank);
            System.out.println(agent.biddingKey.toString());
            outAuctionCen.writeObject("hey");
            Set<Registration> auctionHouses = (Set<Registration>) inAuctionCen.readObject();
            agent.printListOfAuctionHouses(auctionHouses);
            while(true)
            {

            }
            //agent.pollUserInput(agentBankSocket, inBank, outBank, auctionCentralHostName, auctionCenPortNumber);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            System.err.println("Don't know about host ");
            System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * pollUserInput: the main while loop for the agent to interact with the servers. Different prompts
     * allow the user to display his balance or see a list of auction houses to join to start bidding on items.
     * @param bankSocket from main when we create our bank socket
     * @param bankIn bank input stream
     * @param bankOut bank output stream
     * @param acHostName auction central host name
     * @param auctionCenPortNumber auction central port number given from user on start up
     */
    private void pollUserInput(Socket bankSocket, ObjectInputStream bankIn, ObjectOutputStream bankOut, String acHostName,
                               int auctionCenPortNumber) throws UnknownObjectException, IOException, ClassNotFoundException
    {
        Scanner sc = new Scanner(System.in);
        String input = "";
        while(!input.equals("Exit"))
        {
            System.out.println("Please enter the key corresponding to what you want to do\n" +
                    "(1) see list of auction houses \n" +
                    "($) see current account balance\n" +
                    "(Exit) to exit");
            input = sc.next();
            if(input.equals("1"))
            {
                askAcForAh(acHostName, auctionCenPortNumber);
            }
            else if(input.equals("$"))
            {
                inquireBankBalance(bankSocket, bankIn, bankOut);
            }
            else if(!input.equals("Exit"))
            {
                System.out.println("Please enter a valid input.");
            }

        }
    }

    /**
     * askAcForAh (ask auction central for auction houses): this method will communicate with the auction central over a
     * socket to get the list of auction houses. once the auction central returns the list of auction houses, we will
     * ask the agent which house they are wanting to join, we will then join that house to start bidding on items.
     * @param acHostName auction central host name
     * @param acPortNumber auction central port number
     */
    private void askAcForAh(String acHostName, int acPortNumber) throws UnknownObjectException, IOException
    {
        try
        (
            Socket agentAuctionCentralSocket = new Socket(acHostName, acPortNumber);
            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());
        )
        {
            String requestForAuctionHouses = "AuctionHouses";
            outAuctionCen.writeObject(requestForAuctionHouses);
            outAuctionCen.flush();

            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)inAuctionCen.readObject();
            //printListOfAuctionHouses(listOfAuctionHouses);

            System.out.println("Which auction house would you like to join?");
            Scanner sc = new Scanner(System.in);
            int auctionHouseNum = sc.nextInt();

            joinAuctionHouse(listOfAuctionHouses, auctionHouseNum, acHostName);

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
    private void joinAuctionHouse(ArrayList<Registration> listOfAuctionHouses, int auctionHouseNum, String hostname)
            throws IOException, ClassNotFoundException
    {
        Registration auctionHouse = listOfAuctionHouses.get(auctionHouseNum);
        try
        (
                Socket auctionHouseSocket = new Socket("FILLIN", 1234);
                ObjectOutputStream outAuctionHouse = new ObjectOutputStream(auctionHouseSocket.getOutputStream());
                ObjectInputStream inAuctionHouse = new ObjectInputStream(auctionHouseSocket.getInputStream());
        )
        {

            String requestForAuctionItems = "List";
            outAuctionHouse.writeObject(requestForAuctionItems);
            outAuctionHouse.flush();

            ArrayList<AuctionItem> listOfAuctionItems = (ArrayList<AuctionItem>) inAuctionHouse.readObject();
            printListOfAuctionItems(listOfAuctionItems);

            System.out.println("Which auction item would you like to bid on?");
            Scanner sc = new Scanner(System.in);
            int itemNumber = sc.nextInt();
            AuctionItem itemBiddingOn = listOfAuctionItems.get(itemNumber);
            System.out.println(itemBiddingOn.getCurrentBid());

            System.out.println("How much would you like to bid?");
            int bidAmount = sc.nextInt();
            Bid agentBidOnItem = new Bid(bankAccount.getKey(), itemBiddingOn);
            agentBidOnItem.setBidAmount(bidAmount);

            outAuctionHouse.writeObject(agentBidOnItem);
        }
    }

    private void inquireBankBalance(Socket bankSocket, ObjectInputStream bankIn, ObjectOutputStream bankOut)
            throws IOException, ClassNotFoundException
    {
        bankOut.writeObject("Inquire");
        bankOut.writeObject(bankAccount.getKey());

        String balance = (String) bankIn.readObject();
        System.out.println(balance + "\n");
    }

    private void printListOfAuctionItems(ArrayList<AuctionItem> auctionItems)
    {
        int counter = 0;
        for(AuctionItem ai : auctionItems)
        {
            counter++;
            System.out.println(counter + ". " + ai);
        }
    }

    private void printListOfAuctionHouses(Set<Registration> auctionHouses)
    {
        int counter = 0;
        for(Registration ah : auctionHouses)
        {
            counter++;
            System.out.println(counter + ". " + ah.getHouseName());
        }
    }


}
