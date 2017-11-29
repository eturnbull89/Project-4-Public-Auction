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
    private String agentName = "";
    private AcctKey bankAccount;
    private Integer biddingKey;

    /**
     *
     */

    public static void main(String[] args) throws IOException, UnknownObjectException, ClassNotFoundException
    {
        if(args.length != 5)
        {
            System.err.println("Please retry and input the bank hostName, auction central hostname, portNumber of the bank, portNumber "
                                + "of the auction central and your user name.");
            System.exit(1);
        }

        Agent agent = new Agent();

        String bankHostName = args[0];  //bank host name will be first argument
        String auctionCentralHostName = args[1];
        int bankPortNumber = Integer.parseInt(args[2]); //port number for bank will be 3rd arg
        int auctionCenPortNumber = Integer.parseInt(args[3]);
        agent.agentName = args[4];

        try
        {
            Socket agentAuctionCentralSocket = new Socket(auctionCentralHostName, auctionCenPortNumber);
            ObjectOutputStream outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            outAuctionCen.flush();
            ObjectInputStream inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());

            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            ObjectOutputStream outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
            outBank.flush();
            ObjectInputStream inBank = new ObjectInputStream(agentBankSocket.getInputStream());

            agent.registerWithBank(outBank, inBank);
            agent.registerWithAuctionCentral(outAuctionCen, inAuctionCen);
            agent.pollUserInput(inBank, outBank, outAuctionCen, inAuctionCen);
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

    private void registerWithBank(ObjectOutputStream out, ObjectInputStream in)
    {
        try
        {
            UserAccount myAccount = new UserAccount(agentName);
            out.writeObject(myAccount);
            out.flush();
            bankAccount = (AcctKey) in.readObject();

            System.out.println("Read in account number and agent key:");
            System.out.println("Bank number: " + bankAccount.getAccountNumber());
            System.out.println("Agent Key: " + bankAccount.getKey() + "\n");

            System.out.println("Inquiring on balance: ");
            inquireBankBalance(in, out);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void registerWithAuctionCentral(ObjectOutputStream out, ObjectInputStream in)
    {
        try
        {
            out.writeObject(bankAccount.getKey());
            out.flush();
            biddingKey = (Integer) in.readObject();
            System.out.println(biddingKey.toString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * pollUserInput: the main while loop for the agent to interact with the servers. Different prompts
     * allow the user to display his balance or see a list of auction houses to join to start bidding on items.
     * @param bankIn bank input stream
     * @param bankOut bank output stream
     * @param out auction central host name
     * @param in auction central port number given from user on start up
     */
    private void pollUserInput(ObjectInputStream bankIn, ObjectOutputStream bankOut,
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
                inquireBankBalance(bankIn, bankOut);
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
     * @param outAuctionCen auction central host name
     * @param inAuctionCen auction central port number
     */
    private void askAcForAh(ObjectOutputStream outAuctionCen, ObjectInputStream inAuctionCen) throws UnknownObjectException, IOException
    {
        try
        {
            String requestForAuctionHouses = "AuctionHouses";
            outAuctionCen.writeObject(requestForAuctionHouses);
            outAuctionCen.flush();

            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)inAuctionCen.readObject();
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
        Registration auctionHouse = listOfAuctionHouses.get(auctionHouseNum - 1);
        try
        (
                Socket auctionHouseSocket = new Socket(auctionHouse.getHostName(), auctionHouse.getHouseSocket());
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

    private void inquireBankBalance(ObjectInputStream bankIn, ObjectOutputStream bankOut)
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
            System.out.println(counter + ". " + ai.getName());
        }
    }

    private void printListOfAuctionHouses(ArrayList<Registration> auctionHouses)
    {
        int counter = 0;

        for(Registration ah : auctionHouses)
        {
            counter++;
            System.out.println(counter + ". " + ah.getHouseName());
        }
    }


}
