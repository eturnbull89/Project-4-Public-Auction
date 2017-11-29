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
        String bankHostName = "localhost";
        int bankPortNumber = 1031;

        String auctionCentralHostName = "localhost"; //AC host name will be 2nd arg
        int auctionCenPortNumber = 1027;  //port number for auction central will be fourth

        try
        {
            //Connect to Auction Central
            Socket agentAuctionCentralSocket = new Socket(auctionCentralHostName, auctionCenPortNumber);
            System.out.println("trying to write object to bank..4");
            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            outAuctionCen.flush();
            System.out.println("trying to write object to bank..5");
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());
            System.out.println("trying to write object to bank..6");

            //Connect to the Bank
            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            System.out.println("Trying to write object to bank..1");
            ObjectOutputStream outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
            System.out.println("Trying to write object to bank..2");
            outBank.flush();
            ObjectInputStream inBank = new ObjectInputStream(agentBankSocket.getInputStream());
            System.out.println("Trying to write object to bank..3");

            //sets up first bank account
            UserAccount myAccount = new UserAccount(agent.agentName);
            System.out.println("Trying to write object to bank..");
            outBank.writeObject(myAccount);
            outBank.flush();
            System.out.println("Trying to read object from bank..");
            agent.bankAccount = (AcctKey) inBank.readObject();

            System.out.println("Read in account number and agent key:");
            System.out.println("Bank number: " + agent.bankAccount.getAccountNumber());
            System.out.println("Agent Key: " + agent.bankAccount.getKey() + "\n");

            //Test

            //2nd Bank Account
//            UserAccount myAccount2 = new UserAccount("Jim");
//            System.out.println("Trying to write object to bank..");
//            outBank.writeObject(myAccount2);
//            outBank.flush();
//            System.out.println("Trying to read object from bank..");
//            agent.bankAccount = (AcctKey) inBank.readObject();
//
//            System.out.println("Read in account number and agent key:");
//            System.out.println("Bankunt number: " + agent.bankAccount.getAccountNumber());
//            System.out.println("Agent Key: " + agent.bankAccount.getKey());

            System.out.println("Inquiring on balance: ");
            agent.inquireBankBalance(agentBankSocket, inBank, outBank);

            //Test

            //outAuctionCen.writeObject(agent.agentName);
            System.out.println("Trying to write object to auction central...");
            Integer tempIntObj = 123;
            outAuctionCen.writeObject(tempIntObj);
            outAuctionCen.flush();

            System.out.println("Trying to read object from auction central...");
            agent.biddingKey = (Integer) inAuctionCen.readObject();
            System.out.println("Bidding key: " + agent.biddingKey.toString());

            agent.pollUserInput(agentBankSocket, inBank, outBank, outAuctionCen, inAuctionCen);
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
     * @param out auction central host name
     * @param in auction central port number given from user on start up
     */
    private void pollUserInput(Socket bankSocket, ObjectInputStream bankIn, ObjectOutputStream bankOut,
                               ObjectOutputStream out, ObjectInputStream in) throws UnknownObjectException, IOException, ClassNotFoundException
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
                askAcForAh(out, in);
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
     * @param out auction central host name
     * @param in auction central port number
     */
    private void askAcForAh(ObjectOutputStream out, ObjectInputStream in) throws UnknownObjectException, IOException
    {
        try
        {
            String requestForAuctionHouses = "AuctionHouses";
            out.writeObject(requestForAuctionHouses);
            out.flush();

            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)in.readObject();
            printListOfAuctionHouses(listOfAuctionHouses);

            System.out.println("Which auction house would you like to join?");
            Scanner sc = new Scanner(System.in);
            int auctionHouseNum = sc.nextInt();

            joinAuctionHouse(listOfAuctionHouses, auctionHouseNum);

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
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void joinAuctionHouse(ArrayList<Registration> listOfAuctionHouses, int auctionHouseNum)
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
        System.out.println("Printing list of auction items");
        int counter = 0;
        for(AuctionItem ai : auctionItems)
        {
            counter++;
            System.out.println(counter + ". " + ai);
        }
    }

    private void printListOfAuctionHouses(ArrayList<Registration> auctionHouses)
    {
        System.out.println("Printing list of auction houses");
        int counter = 0;
        for(Registration ah : auctionHouses)
        {
            System.out.println("printing auction houses!");
            counter++;
            System.out.println(counter + ". " + ah.getHouseName());
        }
    }


}
