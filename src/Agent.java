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

    private ObjectInputStream inBank;
    private ObjectOutputStream outBank;
    private ObjectInputStream inAuctionCen;
    private ObjectOutputStream outAuctionCen;
    private ObjectInputStream inCurrentAuctionHouse;
    private ObjectOutputStream outCurrentAuctionHouse;

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

        agent.registerWithBank(bankHostName, bankPortNumber);
        agent.registerWithAuctionCentral(auctionCentralHostName, auctionCenPortNumber);
        agent.pollUserInput();
    }

    private void registerWithBank(String bankHostName, int bankPortNumber)
    {
        try
        {
            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
            outBank.flush();
            inBank = new ObjectInputStream(agentBankSocket.getInputStream());

            UserAccount myAccount = new UserAccount(agentName);
            outBank.writeObject(myAccount);
            outBank.flush();
            bankAccount = (AcctKey) inBank.readObject();

            System.out.println("Read in account number and agent key:");
            System.out.println("Bank number: " + bankAccount.getAccountNumber());
            System.out.println("Agent Key: " + bankAccount.getKey() + "\n");

            System.out.println("Inquiring on balance: ");
            inquireBankBalance();
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

    public void registerWithAuctionCentral(String acHostName, int acPortNum)
    {
        try
        {
            Socket agentAuctionCentralSocket = new Socket(acHostName, acPortNum);
            outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            outAuctionCen.flush();
            inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());

            outAuctionCen.writeObject(bankAccount.getKey());
            outAuctionCen.flush();
            biddingKey = (Integer) inAuctionCen.readObject();
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
     * pollUserInput: the main while loop for the agent to interact with the servers. Depending on the users
     * input, this method will lead to a trail of different prompts.
     */
    private void pollUserInput() throws UnknownObjectException, IOException, ClassNotFoundException
    {
        Scanner sc = new Scanner(System.in);
        String input = "";
        while(!input.equals("Exit"))
        {
            System.out.println("\nMain Menu");
            System.out.println("Please enter the key corresponding to what you want to do\n" +
                    "(1) see list of auction houses \n" +
                    "($) see current account balance\n" +
                    "(Exit) to exit");
            input = sc.next();
            if(input.equals("1"))
            {
                askAcForAh();
            }
            else if(input.equals("$"))
            {
                inquireBankBalance();
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
     */
    private void askAcForAh() throws UnknownObjectException, IOException
    {
        try
        {

            ArrayList<Registration> listOfAuctionHouses = requestListOfAuctionHouses();

            if(listOfAuctionHouses != null)
            {
                while (!listOfAuctionHouses.isEmpty())
                {
                    System.out.println("\nMain Menu\\AuctionCentral");
                    listOfAuctionHouses = requestListOfAuctionHouses();
                    printListOfAuctionHouses(listOfAuctionHouses);

                    System.out.println("Which auction house would you like to join? Or type Exit to quit");
                    Scanner sc = new Scanner(System.in);
                    String input = sc.next();
                    if(input.equals("Exit"))
                    {
                        return;
                    }
                    else
                    {
                        int auctionHouseNum = Integer.parseInt(input);
                        joinAuctionHouse(listOfAuctionHouses, auctionHouseNum);
                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * joinAuctionHouse:
     * @param listOfAuctionHouses
     * @param auctionHouseNum
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void joinAuctionHouse(ArrayList<Registration> listOfAuctionHouses, int auctionHouseNum)
            throws IOException, ClassNotFoundException
    {
        Registration auctionHouse = listOfAuctionHouses.get(auctionHouseNum - 1);
        setCurrentAuctionHouseStreams(auctionHouse);

        ArrayList<AuctionItem> listOfAuctionItems = requestListOfAuctionItems();

        while(!listOfAuctionItems.isEmpty())
        {
            System.out.println("\nMain Menu\\AuctionCentral\\" + auctionHouse.getHouseName());
            printListOfAuctionItems(listOfAuctionItems);
            System.out.println("Which auction item would you like to bid on? Or type Exit to leave auction house");

            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            if(input.equals("Exit"))
                return;
            else
            {
                int itemNumber = Integer.parseInt(input) - 1;
                AuctionItem itemBiddingOn = listOfAuctionItems.get(itemNumber);

                bidOnAuctionItem(itemBiddingOn, auctionHouse.getHouseName(), itemNumber);
            }
        }
    }

    /**
     * bidOnAuctionItem:
     * @param itemBiddingOn
     * @param auctionHouseName
     */
    private void bidOnAuctionItem(AuctionItem itemBiddingOn, String auctionHouseName, int itemNumber)
    {
        try
        {
            Scanner sc = new Scanner(System.in);
            System.out.println("\nMain Menu\\AuctionCentral\\" + auctionHouseName
                    + "\\" + itemBiddingOn.getName());

            System.out.println("Minimum bid: " + itemBiddingOn.getMinimumBid());

            System.out.println("How much would you like to bid?");
            int bidAmount = sc.nextInt();
            Bid agentBidOnItem = new Bid(biddingKey, itemBiddingOn);
            agentBidOnItem.setBidAmount(bidAmount);

            outCurrentAuctionHouse.writeObject(agentBidOnItem);
            agentBidOnItem = (Bid) inCurrentAuctionHouse.readObject();

            while (!agentBidOnItem.getBidStatus().equals("Over"))
            {
                //itemBiddingOn = getUpdatedItem(itemNumber); //get the new list of items after every bid
                //int highestBid = itemBiddingOn.getCurrentBid();
                int highestBid = agentBidOnItem.getItemBiddingOn().getCurrentBid();

                inquireBankBalance();
                System.out.println("Current highest bid on " + itemBiddingOn.getName() + " : " + highestBid);

                System.out.println("How much would you like to bid? Or type Exit to stop bidding on " + itemBiddingOn.getName());
                String bidInput = sc.next();

                if (bidInput.toLowerCase().equals("exit"))
                {
                    return;
                }
                else if(isNumeric(bidInput))
                {
                    System.out.println("Is numeric");
                    bidAmount = Integer.parseInt(bidInput);
                    if (bidAmount < highestBid)
                    {
                        System.out.println("Please enter a higher bid");
                    }
                    else
                    {
                        agentBidOnItem.setBidAmount(bidAmount);
                        outCurrentAuctionHouse.writeObject(agentBidOnItem);
                        agentBidOnItem = (Bid) inCurrentAuctionHouse.readObject();
                    }
                }
                else
                {
                    System.out.println("Please either enter Exit to stop bidding, or a number to bid.");
                }
            }
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

    private AuctionItem getUpdatedItem(int itemNumber)
    {
        ArrayList<AuctionItem> listOfAuctionItems = requestListOfAuctionItems();
        if(listOfAuctionItems != null)
            printListOfAuctionItems(listOfAuctionItems);

        AuctionItem itemBiddingOn = listOfAuctionItems.get(itemNumber);

        return itemBiddingOn;
    }

    /**
     * requestListOfAuctionItems:
     * send a string over the auction house out stream. read in the input from auction house,
     * set the input as the list of auction items.
     * @return the list of auction items in this auction house
     */
    private ArrayList<AuctionItem> requestListOfAuctionItems()
    {
        try
        {
            String requestForAuctionItems = "List";
            outCurrentAuctionHouse.writeObject(requestForAuctionItems);
            outCurrentAuctionHouse.flush();

            ArrayList<AuctionItem> listOfAuctionItems = (ArrayList<AuctionItem>) inCurrentAuctionHouse.readObject();
            return listOfAuctionItems;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void inquireBankBalance()
            throws IOException, ClassNotFoundException
    {
        outBank.writeObject("Inquire");
        outBank.writeObject(bankAccount.getKey());

        String balance = (String) inBank.readObject();
        System.out.println(balance);
    }

    /**
     * setCurrentAuctionHouseStream:
     * take the auctionHouse registration and create the auction house socket and communication streams
     * @param auctionHouse the auction house we're connecting to
     */
    public void setCurrentAuctionHouseStreams(Registration auctionHouse)
    {
        try
        {
            Socket auctionHouseSocket = new Socket(auctionHouse.getHostName(), auctionHouse.getHouseSocket());
            outCurrentAuctionHouse = new ObjectOutputStream(auctionHouseSocket.getOutputStream());
            inCurrentAuctionHouse = new ObjectInputStream(auctionHouseSocket.getInputStream());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * requestListOfAuctionHouses:
     * send a string over the auction central out stream. read in the input from auction central,
     * set the input as the list of auction houses.
     * @return the list of auction houses
     */
    private ArrayList<Registration> requestListOfAuctionHouses()
    {
        try
        {
            String requestForAuctionHouses = "AuctionHouses";
            outAuctionCen.writeObject(requestForAuctionHouses);
            outAuctionCen.flush();

            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)inAuctionCen.readObject();
            return listOfAuctionHouses;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private void printListOfAuctionItems(ArrayList<AuctionItem> auctionItems)
    {
        int counter = 0;

        for(AuctionItem ai : auctionItems)
        {
            counter++;
            System.out.println(counter + ". " + ai.getName() + "  highest bid: " + ai.getCurrentBid());
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

    public boolean isNumeric(String s)
    {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
}
