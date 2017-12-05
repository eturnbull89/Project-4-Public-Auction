import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.rmi.activation.UnknownObjectException;
import java.util.*;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  |
 *          Adam Spanswick |
 * =============================================
 *
 * Agent: The client interface for the Public Auction.
 *
 *
 */

public class Agent
{
    private String agentName = "";
    private AcctKey bankAccount;
    private Integer biddingKey;
    private AuctionItem lastItemBid;

    private ObjectInputStream inBank;
    private ObjectOutputStream outBank;
    private ObjectInputStream inAuctionCen;
    private ObjectOutputStream outAuctionCen;
    private ObjectInputStream inCurrentAuctionHouse;
    private ObjectOutputStream outCurrentAuctionHouse;

    private ArrayList<Bid> currentBids = new ArrayList<>();
    private ArrayList<AuctionItem> listOfAuctionItems;
    private ArrayList<AuctionItem> wonItems = new ArrayList<>();

    /**
     * reads in the arguments from the command line when the program is launched. these arguments are
     * respectively listed below, and if you don't input the correct number the program will close and ask
     * you to please relaunch with the correct arguments. If you enter a bad ip add. we have a connectException
     * in our methods to setup the sockets with the servers, and the program will close. Otherwise if the input
     * is great, main sets up the Bank and Auction central sockets, registers with each, and starts
     * asking for user input.
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

        agent.agentName = args[ 4];

        agent.registerWithBank(bankHostName, bankPortNumber);
        agent.registerWithAuctionCentral(auctionCentralHostName, auctionCenPortNumber);
        agent.pollUserInput();
        System.out.println("Thank you for playing, here are your won items:");
        agent.printListOfAuctionItems(agent.wonItems, 1);
        System.out.println("Agent exiting...");
        System.exit(1);
    }

    /**
     * registerWithBank: setup the sockets with the bankHostName and bankPortNumber,
     * then create the output and input streams. Setup an account with the bank, will
     * get the bank key back.
     * @param bankHostName - host name that bank is located at.
     * @param bankPortNumber - port number that bank is hosting at.
     */
    private void registerWithBank(String bankHostName, int bankPortNumber)
    {
        try
        {
            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
            outBank.reset();
            inBank = new ObjectInputStream(agentBankSocket.getInputStream());

            UserAccount myAccount = new UserAccount(agentName);
            outBank.writeObject(myAccount);
            outBank.reset();
            bankAccount = (AcctKey) inBank.readObject();

            System.out.println("Read in account number and agent key:");
            System.out.println("Bank number: " + bankAccount.getAccountNumber());
            System.out.println("Agent Key: " + bankAccount.getKey() + "\n");

            System.out.println("Inquiring on balance: ");
            inquireBankBalance();
        }
        catch(ConnectException e)
        {
            System.out.println("No proper server setup.");
            System.exit(1);
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * registerWithAuctionCentral: setup the sockets with the auction central host name and port number,
     * then create the output and input streams. Register with the auction central that we're wanting to bid,
     * get the bidding key back from the auction central
     * @param acHostName - host name that auction central is located at.
     * @param acPortNum - port number that auction central is hosting at.
     */
    private void registerWithAuctionCentral(String acHostName, int acPortNum)
    {
        try
        {
            Socket agentAuctionCentralSocket = new Socket(acHostName, acPortNum);
            outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            outAuctionCen.reset();
            inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());

            outAuctionCen.writeObject(bankAccount.getKey());
            outAuctionCen.reset();
            biddingKey = (Integer) inAuctionCen.readObject();
            System.out.println(biddingKey.toString());
        }

        catch(IOException | ClassNotFoundException e)
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
        while(!input.toLowerCase().equals("exit"))
        {
            System.out.println((char) 27 + "[33m\nMain Menu" + (char) 27 + "[0m");
            System.out.println("Please enter one of the following options\n" +
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
            else if(!input.toLowerCase().equals("exit"))
            {
                System.out.println("Please enter a valid input.");
            }
        }
    }

    /**
     * askAcForAh (ask auction central for auction houses): this method will communicate with the auction central over a
     * socket to get the list of auction houses. once the auction central returns the list of auction houses, we will
     * ask the agent which house they are wanting to join, we will then join that house to start bidding on items.
     *
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
                    if(!checkBidWithBank(1))
                    {
                        if(!checkBidStillGoing(lastItemBid))
                        {
                            System.out.println("\nYou have no funds available.");
                            System.out.println("Thank you for playing, here are your won items:");
                            printListOfAuctionItems(wonItems, 1);
                            System.exit(1);
                        }
                    }

                    System.out.println((char) 27 + "[33m\nMain Menu\\AuctionHouses" + (char) 27 + "[0m");
                    listOfAuctionHouses = requestListOfAuctionHouses();
                    printListOfAuctionHouses(listOfAuctionHouses);

                    System.out.println("Which auction house would you like to join? Or type Exit to quit");
                    Scanner sc = new Scanner(System.in);
                    String input = sc.next();
                    if(input.toLowerCase().equals("exit"))
                    {
                        return;
                    }
                    else if(isNumeric(input))
                    {
                        int auctionHouseNum = Integer.parseInt(input)-1;
                        if(auctionHouseNum < listOfAuctionHouses.size())
                        {
                            joinAuctionHouse(listOfAuctionHouses, auctionHouseNum);
                        }
                        else
                        {
                            System.out.println("Please enter a valid auction house number");
                        }
                    }
                    else
                    {
                        System.out.println("Please enter a valid input");
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
     * joinAuctionHouse: joins an auction house given the list from auction central, displays the items
     * given from the auction house joined. Checks if the users last item bidding on is still going and if they have
     * any funds left, if not, shuts down the client. Otherwise, lets the user decide which item they would like
     * to bid on and enters them into bidding for that item.
     * @param listOfAuctionHouses - An array list of the active auction houses.
     * @param auctionHouseNum - Number that allows an auction house to be chosen from by the user.
     * @throws IOException - Thrown if I/O could not be established.
     * @throws ClassNotFoundException - Thrown if the used class does not exist or is not serialized.
     */
    private void joinAuctionHouse(ArrayList<Registration> listOfAuctionHouses, int auctionHouseNum)
            throws IOException, ClassNotFoundException
    {
        Registration auctionHouse = listOfAuctionHouses.get(auctionHouseNum);
        setCurrentAuctionHouseStreams(auctionHouse);

        updateListOfAuctionItems();

        while(!listOfAuctionItems.isEmpty())
        {
            if(!checkBidWithBank(1))
            {
                if(!checkBidStillGoing(lastItemBid))
                {
                    System.out.println("\nYou have no funds available.");
                    System.out.println("Thank you for playing, here are your won items:");
                    printListOfAuctionItems(wonItems, 1);
                    System.exit(1);
                }
            }
            updateListOfAuctionItems();       //get the new list of items whenever we exit out of an item

            System.out.println((char) 27 + "[33m\nMain Menu\\AuctionCentral\\" + auctionHouse.getHouseName()
                                + (char) 27 + "[0m");
            printListOfAuctionItems(listOfAuctionItems, 0);
            System.out.println("Which auction item would you like to bid on? Or type Exit to leave auction house");

            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            if(input.toLowerCase().equals("exit"))
                return;
            else if(isNumeric(input))
            {
                int itemNumber = Integer.parseInt(input) - 1;
                if(itemNumber < listOfAuctionItems.size())
                {
                    AuctionItem itemBiddingOn = listOfAuctionItems.get(itemNumber);
                    bidOnAuctionItem(itemBiddingOn, auctionHouse.getHouseName());
                }
                else
                {
                    System.out.println("Enter a valid number.");
                }
            }
            else
            {
                System.out.println("Enter a valid input.");
            }
        }
    }

    /**
     * bidOnAuctionItem: The most involved prompt for the userr. bidOnAutionItem controls the flow of the users inputs
     * for bidding on an item, this function communicates with the auction house heavily passing back and forth
     * the bid for an item. Whenever a user enters this method, we always check that they haven't already bid on the
     * item with checkBid(). If not, then we create a new bid. The while loop below operates until the bid is Over (time
     * runs out for bidding) or the user enters exit. After every bid, we ask the AuctionHouse for the list
     * of current auction items, ensuring we have the most up to date items with the highest bid and items
     * available to bid on. Whenever a bid
     * @param itemBiddingOn - The auction item that the agent wants to bid on.
     * @param auctionHouseName - The name of an auction house.
     */
    private void bidOnAuctionItem(AuctionItem itemBiddingOn, String auctionHouseName)
    {
        try
        {
            Scanner sc = new Scanner(System.in);

            System.out.println("Minimum bid: " + itemBiddingOn.getMinimumBid());

            Bid agentBidOnItem = checkBid(itemBiddingOn);

            while(!agentBidOnItem.getBidStatus().toLowerCase().equals("over"))
            {
                System.out.println((char) 27 + "[33m\nMain Menu\\AuctionCentral\\" + auctionHouseName + "\\" + itemBiddingOn.getName()
                        + (char) 27 + "[0m");

                //set highest bid to the current bid of the item we're bidding on by going through our updated list
                //of auction items
                AuctionItem matchingItem = getMatchingItem(itemBiddingOn);
                int highestBid = matchingItem.getHighestBid();

                inquireBankBalance();
                System.out.println("Current highest bid on " + itemBiddingOn.getName() + " : " + highestBid);

                System.out.println("How much would you like to bid? Or type Exit to stop bidding on " + itemBiddingOn.getName());
                String bidInput = sc.next();


                if(!checkBidStillGoing(itemBiddingOn))
                {

                    inquireWinner(itemBiddingOn);
                    updateListOfAuctionItems();

                    return;

                }
                else if(bidInput.toLowerCase().equals("exit"))
                {
                    return;
                }
                else if(isNumeric(bidInput))
                {

                    int bidAmount = Integer.parseInt(bidInput);

                    if(!checkBidWithBank(bidAmount))
                    {
                        //TODO need to send AuctionHouse that the bid was failed still, so we can still know that the bid is over.
                        System.out.print((char) 27 + "[31mYou do not have those funds available, enter a lower bid."
                                        + (char) 27 + "[0m");
                        continue;

                    }

                    if (bidAmount < highestBid)
                    {

                        System.out.print((char) 27 + "[31mYour bid was rejected. Try bidding higher than the current bid."
                                            + (char) 27 + "[0m");

                    }
                    else if(bidAmount > bankAccount.getBalance())
                    {

                        System.out.print((char) 27 + "[31mYou do not have the available funds for that bid"
                                        + (char) 27 + "[0m");

                    }
                    else
                    {

                        agentBidOnItem.setBidAmount(bidAmount);

                        //write our bid to the auction house
                        outCurrentAuctionHouse.writeObject(agentBidOnItem);
                        outCurrentAuctionHouse.reset();

                        //read in the bid back from the auction house, NOTE: only do this after sending auction house a bid
                        agentBidOnItem = (Bid) inCurrentAuctionHouse.readObject();
                        lastItemBid = agentBidOnItem.getItemBiddingOn();

                        if(agentBidOnItem.getBidStatus().toLowerCase().equals("pass"))
                        {

                            System.out.print((char) 27 + "[31mYour bid was passed, you're already winning the auction!"
                                            + (char) 27 + "[0m");
                            agentBidOnItem.setBidAmount(agentBidOnItem.getItemBiddingOn().getHighestBid());

                        }
                        else if(agentBidOnItem.getBidStatus().toLowerCase().equals("acceptance"))
                        {

                            System.out.print((char) 27 + "[32m******You're currently winning the auction******"
                                            + (char) 27 + "[0m");

                        }
                        else if(agentBidOnItem.getBidStatus().toLowerCase().equals("reject"))
                        {

                            System.out.print((char) 27 + "[31mYour bid was rejected. Try bidding higher than the current bid."
                                            + (char) 27 + "[0m");

                        }
                    }
                }
                else
                {
                    System.out.println("Please either enter Exit to stop bidding, or a number to bid.");
                }

                //after every iteration of the while loop request the list of auction items to ensure we have the right one
                updateListOfAuctionItems();

            }
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * inquireWinner: asks the auction houses if they won the auction for a given item.
     * @param item - An auction item.
     */
    private void inquireWinner(AuctionItem item)
    {
        try
        {
            WinnerInquire winnerInquire = new WinnerInquire(item, biddingKey);

            outCurrentAuctionHouse.writeObject(winnerInquire);
            outCurrentAuctionHouse.reset();

            boolean amWinner = (Boolean) inCurrentAuctionHouse.readObject();
            if (amWinner)
            {
                wonItems.add(item);
                System.out.print((char) 27 + "[32mCongrats, you won the bid for " + item.getName() + "!" + (char) 27 + "[0m");
            }
            else
            {
                System.out.print((char) 27 + "[31mSorry, you lost the bid for " + item.getName() + "." + (char) 27 + "[0m");
            }
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * checkBid: check if a bid for the passed in auction item has already been created, if so, return that bid.
     * otherwise
     * @param itemBiddingOn - An auction item that an agent is bidding on.
     * @return checkBid returns a Bid object.
     */
    private Bid checkBid(AuctionItem itemBiddingOn)
    {
        Bid agentBidOnItem;

        if(currentBids.isEmpty())
        {
            agentBidOnItem = new Bid(biddingKey, itemBiddingOn);
            currentBids.add(agentBidOnItem);
        }
        else if(getBidOnSameItem(itemBiddingOn.getItemSerialNum()) != null)
        {
            agentBidOnItem = getBidOnSameItem(itemBiddingOn.getItemSerialNum());
        }
        else
        {
            agentBidOnItem = new Bid(biddingKey, itemBiddingOn);
            currentBids.add(agentBidOnItem);
        }
        return agentBidOnItem;
    }

    /**
     * checkBidStillGoing: enquires with the auction house on if an items still being bid on
     * @param item - An auction item that an agent is bidding on.
     * @return checkBidStillGoing returns a boolean
     */
    private boolean checkBidStillGoing(AuctionItem item)
    {
        ItemEnquire itEnq = new ItemEnquire(item.getItemSerialNum());

        try
        {
            outCurrentAuctionHouse.writeObject(itEnq);
            outCurrentAuctionHouse.reset();

            return (Boolean) inCurrentAuctionHouse.readObject();
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * requestListOfAuctionItems:
     * send a string over the auction house out stream. read in the input from auction house,
     * set the input as the list of auction items.
     * update the list of auction items in this auction house
     */
    private void updateListOfAuctionItems()
    {
        try
        {
            String requestForAuctionItems = "list";
            outCurrentAuctionHouse.writeObject(requestForAuctionItems);
            outCurrentAuctionHouse.reset();

            listOfAuctionItems = (ArrayList<AuctionItem>) inCurrentAuctionHouse.readObject();

        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * inquireBankBalance:
     * @throws IOException - I/O to bank could not be established.
     * @throws ClassNotFoundException - Exception caused by a class not existing or not serialized.
     */
    private void inquireBankBalance()
            throws IOException, ClassNotFoundException
    {
        outBank.writeObject("Inquire");
        outBank.writeObject(bankAccount.getKey());
        outBank.reset();

        String balance = (String) inBank.readObject();
        System.out.println(balance);
    }

    /**
     * getBidOnSameItem loops through our arraylist of bids and gets the
     * bid on the same item if it exists
     * @param itemSRN the items serial number
     * @return getBidOnSameItem returns a Bid object
     */
    private Bid getBidOnSameItem(int itemSRN)
    {
        for(Bid bid : currentBids)
        {
            if(bid.getItemBiddingOn().getItemSerialNum() == itemSRN)
            {
                return bid;
            }
        }
        return null;
    }

    /**
     * checkBidWithBank: verifies the bid is valid with the bank,
     * mainly used to close an agent with no funds
     * @param bid - Amount that checks if a bid amount is valid.
     * @return checkBidWithBank returns a boolean
     */
    private boolean checkBidWithBank(int bid)
    {
        try
        {
            Integer myBid = bid;
            outBank.writeObject(bankAccount.getKey());
            outBank.writeObject(myBid);
            outBank.reset();

            return (Boolean) inBank.readObject();
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * setCurrentAuctionHouseStream:
     * take the auctionHouse registration and create the auction house socket and communication streams
     * @param auctionHouse the auction house we're connecting to
     */
    private void setCurrentAuctionHouseStreams(Registration auctionHouse)
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
            outAuctionCen.reset();

            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)inAuctionCen.readObject();

            return listOfAuctionHouses;
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * getMatchingItem: loops through our list of auction items and gets the one that matches
     * the passed in item
     * @param item - The auction item
     * @return getMatchingItem returns an AuctionItem
     */
    private AuctionItem getMatchingItem(AuctionItem item)
    {
        for(AuctionItem ai : listOfAuctionItems)
        {
            if(item.getName().equals(ai.getName()))
                return ai;
        }
        return null;
    }

    private void printListOfAuctionItems(ArrayList<AuctionItem> auctionItems, int test)
    {
        int counter = 0;

        for(AuctionItem ai : auctionItems)
        {
            counter++;
            if(test == 0)
                System.out.println(counter + ". " + ai.getName() + " | Current Bid: " + ai.getHighestBid());
            else if(test == 1)
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

    private boolean isNumeric(String s)
    {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

}
