import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Eric Turnbull  | eturnbull@unm.edu
 *          Zach Fleharty  |
 *          Tristin Glunt  | tglunt@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * MiniHouse is used to allow an Agent to communicate with an AuctionHouse. MiniHouse extends Thread so that multiple
 * agents can be processed at once. Each MiniHouse has a number of variables that are set in its constructor.
 * agentSocket is used to communicate back and forth with an agent via a socket connection. centralIn and centralOut
 * are object input and output streams that allow communication with auction central. items is an array list of
 * AuctionItems that each auction house has available for auction. winnerList is an ArrayList that contains items
 * that have finished being bid on and contain the winners information. houseKey is a secretKey that is used for
 * updating an AuctionItems fields. MiniHouse has a defined run method that processes each agents request via different
 * objects. bidProtocol is used to process an Agents bid request. countdown is used to start the 30 second timer after
 * a successful bid has been placed and then process if the agent is the winner. itemExists is used to check if an
 * AuctionItem is still available for bidding. findIndex is used to find an AuctionItems current index value. winner is
 * used to check if an Agent won the bid on an AuctionItem. closeProtocol is used to shutdown an auction house early.
 */
class MiniHouse extends Thread
{
    private final Socket agentSocket;

    private final ObjectInputStream centralIn;

    private final ObjectOutputStream centralOut;

    private ArrayList<AuctionItem> items = null;

    private ArrayList<AuctionItem> winnerList = new ArrayList<>();

    private final Integer houseKey;

    /**
     * @param agent - A Socket used to communicate with an Agent.
     * @param items - An ArrayList of AuctionItems that contain what an auction house has for auction.
     * @param houseKey - An integer used to represent an AuctionHouses secret key.
     * @param out - An object output stream to auction central.
     * @param in - An object input stream from auction central.
     * MiniHouse is a constructor used to set the values of agentSocket, items, houseKey, centralIn, and centralOut to
     * the passed values.
     */
    MiniHouse(Socket agent, ArrayList<AuctionItem> items, Integer houseKey, ObjectOutputStream out, ObjectInputStream in)
    {
        this.agentSocket = agent;

        this.items = items;

        this.houseKey = houseKey;

        this.centralIn = in;

        this.centralOut = out;
    }

    /**
     * run is used to define what happens when a MiniHouse is started. Input and output streams are created from the
     * agentSocket. A boolean listening is set to true that will only change when an agent has indicated they want to
     * exit. While listening is true a MiniHouse tries to read an object passed from an agent. If the object passed is
     * a Bid it sends the passed bid to bidProtocol to be processed, then it sends back the updated Bid to the agent. If
     * the status of the bid was acceptance, then countdown is called to start the 30 second count down on an
     * AuctionItem. If an ItemEnquire is sent it sets a boolean to the value returned by itemExists and writes the
     * boolean to the agent. If a WinnerInquire is sent it writes back the boolean value returned by winner(). If a
     * string is sent it checks if the string equals some presets. If the string is exit then it sets the value of
     * listening to false to end the while loop. If the string is "list" it sends the agent the items arrayList. If
     * the input does not match either it returns "error" to indicate bad input. If for some reason the auction house
     * needs to close early their is a runtime hook that calls closeProtocol that creates new AuctionTransactions to
     * release any holds on active AuctionItems.
     */
    public void run()
    {
        try
        {
            ObjectOutputStream outFromHouse = new ObjectOutputStream(agentSocket.getOutputStream());

            outFromHouse.flush();

            centralOut.flush();

            ObjectInputStream inFromAgent = new ObjectInputStream(agentSocket.getInputStream());

            boolean listening = true;

            while(listening)
            {
                Object passed = inFromAgent.readObject();

                if(passed instanceof Bid)
                {
                    Bid passedBid = bidProtocol(centralIn, centralOut, (Bid) passed);

                    outFromHouse.writeObject(passedBid);

                    if(passedBid.getBidStatus().equals("acceptance"))
                    {
                       countdown(passedBid);
                    }
                }

                else if(passed instanceof ItemEnquire)
                {
                    boolean inList = itemExists(((ItemEnquire) passed).getSerialNumber());

                    outFromHouse.writeObject(inList);
                }

                else if(passed instanceof WinnerInquire)
                {
                    int serialNumber = ((WinnerInquire) passed).getAuctionItem().getItemSerialNum();

                    int bidKey = ((WinnerInquire) passed).getBiddingKey();

                    outFromHouse.writeObject(winner(serialNumber, bidKey));
                }

                else if(passed instanceof String)
                {
                    String message = ((String) passed).toLowerCase();

                    switch (message)
                    {
                        case "exit":

                            listening = false;

                            break;

                        case "list":

                            outFromHouse.writeObject(items);

                            outFromHouse.reset();

                            break;

                        default:

                            outFromHouse.writeObject("error");

                            break;
                    }

                }

                Runtime.getRuntime().addShutdownHook(new Thread(() -> closingProtocol(centralOut)));
            }
        }

        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Exit caused");
        }
    }

    /**
     * @param fromCentral - Object input stream used to receive objects from Auction central.
     * @param toCentral - Object output stream used to send objects to Auction central.
     * @param agentBid - A Bid object that holds the an Agents bid on a given AuctionItem.
     * @return bidProtocol returns a Bid object.
     * bidProtocol is used to process a bid request from an agent. It starts by setting up various variables that are
     * used throughout bidProtocol. holdConfirm is used to hold a boolean that represents if a hold was successfully
     * placed on an agents account. agentKey is used to hold the bidKey of the agent, passed in the bid. bidAmount is
     * used to hold the amount of money an agent wants to bid on an object. item is the AuctionItem an agent wants to
     * bid on. stillListed is used to check if the passed item is still available for auction. If an item is
     * stillListed then it sets up the next round of variables to continue processing an agents bid. itemIndex is used
     * to lookup current index of an AuctionItem in the items list. previousBidder is used to check if an agent is
     * the previous bidder or the first bidder, this is used to prevent an agent from placing multiple bids in before
     * a different bid has taken place. highestBid is used to check if the value an agent wants to bid is greater then
     * both the minimumBid amount and the current highestBid amount. If an agent is not the previousBidder and their
     * bid is the highestBid then a new hold AuctionTransaction is created using the agents bidKey and bidAmount. The
     * transaction is then written to auction central and the auction house waits for confirmation of the hold. If the
     * hold is successfully placed and the highestBid did not change in the time it took for a confirmation to occur
     * then the highestBidderKey and highestBid fields of an AuctionItem in both the items list and the item passed
     * are set with the Agents bidKey and bidAmount. If the previousBidderKey and previousBid fields are not null it
     * then sets these to the previous Agents bidKey and bidAmount and another AuctionTransaction is created to release
     * the previousBidders funds. Finally the bidStatus in the bid is set to Acceptance to indicate a successful bid
     * was placed. If the highestBid amount changed in the time it took to get confirmation of the hold another
     * AuctionTransaction is created to release the agents funds from the hold and written to AuctionCentral. Then the
     * highestBid amount in the item passed via the bid is updated to the current highestBid in the items list and the
     * bidStatus in the bid is set to reject to indicate the agents bid was overtaken. If for some reason the funds from
     * an Agents account could not be put on hold the bidStatus is set to Rejection to indicate such. If the agent was
     * the previousBidder then the bid status is updated to pass to prevent the agent from making another bid until
     * their bid is overtaken. If the Agent was not the previousBidder but their bid wasn't high enough right away the
     * bidStatus is set to reject to inform the agent to place a higher bid. If the item is not stillListed the bid
     * status is set to over to indicate the bidding on the item has finished. At the end of any case the updated bid
     * is returned to be returned to the agent.
     */
    private Bid bidProtocol(ObjectInputStream fromCentral, ObjectOutputStream toCentral, Bid agentBid)
    {
        Boolean holdConfirm = false;

        Integer agentKey = agentBid.getAgentBidKey();

        int bidAmount = agentBid.getBidAmount();

        AuctionItem item = agentBid.getItemBiddingOn();

        boolean stillListed = itemExists(item.getItemSerialNum());

        if(stillListed)
        {
            int itemIndex = findIndex(item.getItemSerialNum());

            boolean previousBidder = items.get(itemIndex).getHighestBidderKey() == null ||
                    !(items.get(itemIndex).getHighestBidderKey().equals(agentBid.getAgentBidKey()));

            boolean highestBid = items.get(itemIndex).getHighestBid() < agentBid.getBidAmount() &&
                                 items.get(itemIndex).getMinimumBid() < agentBid.getBidAmount();

            if (highestBid && previousBidder)
            {

                AuctionTransaction hold = new AuctionTransaction(agentKey, bidAmount, -1);

                try
                {
                    toCentral.writeObject(hold);

                    holdConfirm = (Boolean) fromCentral.readObject();

                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }

                if (holdConfirm && items.get(itemIndex).getHighestBid() < agentBid.getBidAmount())
                {
                    AuctionTransaction release = null;

                    synchronized (items.get(itemIndex))
                    {
                        items.get(itemIndex).setPreviousBid(items.get(itemIndex).getHighestBid(), houseKey);

                        items.get(itemIndex).setPreviousBidderKey(items.get(itemIndex).getHighestBidderKey(), houseKey);

                        items.get(itemIndex).setHighestBid(bidAmount, houseKey);

                        item.setHighestBid(bidAmount, houseKey);

                        items.get(itemIndex).setHighestBidKey(agentKey, houseKey);

                        if (items.get(itemIndex).getPreviousBidderKey() != null)
                        {
                            release = new AuctionTransaction(items.get(itemIndex).getPreviousBidderKey(),
                                    items.get(itemIndex).getPreviousBid(), 1);
                        }
                    }

                    try
                    {
                        if (release != null)
                        {
                            toCentral.writeObject(release);
                        }

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    agentBid.setBidStatus("acceptance");
                }

                else if (holdConfirm && items.get(itemIndex).getHighestBid() > agentBid.getBidAmount())
                {
                    Transaction releaseAgent = new Transaction(agentKey, bidAmount, 1);

                    try
                    {
                        toCentral.writeObject(releaseAgent);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    AuctionItem higherBidItem = items.get(itemIndex);

                    agentBid.getItemBiddingOn().setHighestBid(higherBidItem.getHighestBid(), houseKey);

                    agentBid.setBidStatus("reject");
                }

                else
                {
                    agentBid.setBidStatus("rejection");
                }
            }

            else if(!previousBidder)
            {
                agentBid.setBidStatus("pass");
            }

            else
            {
                AuctionItem higherBidItem = items.get(itemIndex);

                agentBid.getItemBiddingOn().setHighestBid(higherBidItem.getHighestBid(), houseKey);

                agentBid.setBidStatus("reject");
            }
        }

        else
        {
            agentBid.setBidStatus("Over");
        }

        return agentBid;
    }

    /**
     * @param agentBid - A Bid object that contains information about an agents bid on an AuctionItem.
     * countdown is used to create a timer that ends the auction on an item after 30 seconds. It starts by creating a
     * new timer object and then schedules this timer to run after 30 seconds have passed. In the inner run method it
     * starts by finding the index value of the item the agent wants to bid on in the passed bid. It then checks if the
     * amount passed in the bid is the same as the amount stored in the highestBid variable of the item in items list.
     * If the values match it adds the AuctionItem to the winners list and removes the item from the public list items.
     * It then creates a new AuctionTransaction to withdraw the fund from the agents account and passes the transaction
     * to auctionCentral. There are commented out print statements that are used for testing to see if the contents of
     * the lists change.
     */
    private void countdown(Bid agentBid)
    {
        Timer bidTimer = new Timer();

        bidTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                int itemIndex = findIndex(agentBid.getItemBiddingOn().getItemSerialNum());

                AuctionItem item = items.get(itemIndex);

                if(agentBid.getBidAmount() == item.getHighestBid())
                {
                    //Add the item to the winners list.
                    winnerList.add(item);

                    AuctionTransaction withdraw = new AuctionTransaction(item.getHighestBidderKey(),
                                                                         item.getHighestBid(), 0);

                    try
                    {
                        centralOut.writeObject(withdraw);

                        centralOut.reset();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    items.remove(itemIndex);

                    System.out.println("wrote to winner, updated list");
                }
            }
        }, 30*1000);
    }

    /**
     * @param serialNumber - An integer value used to id Auction items.
     * @return itemExists returns a boolean.
     * itemExists is used to check if an AuctionItem is still present in the items list. It loops through each Auction
     * item in the items list and compares its serialNumber with the serialNumber passed. If the two match it returns
     * true, otherwise it returns false.
     */
    private boolean itemExists(int serialNumber)
    {

        for (AuctionItem item : items)
        {
            if (item.getItemSerialNum() == serialNumber)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @param serialNumber - An integer value used to id Auction items.
     * @return findIndex returns an int.
     * findIndex is used to find the current index of an Auction item in the items list. It loops through each item in
     * the items list and compares that items serialNumber with the serialNumber passed. If the two values match,
     * findIndex returns the current value of i. If the item was not present it returns an index of zero.
     */
    private int findIndex(int serialNumber)
    {
        for(int i = 0; i < items.size(); i++)
        {
            if(items.get(i).getItemSerialNum() == serialNumber)
            {
                return i;
            }
        }

        return 0;
    }

    /**
     * @param serialNumber - An AuctionItems serial number.
     * @param bidKey - An Agents bid key.
     * @return winner returns a boolean.
     * winner is used to check if an Agent has won the bid on an AuctionItem. It loops through each item in the
     * winnerList and it compares the given serialNumber with each items serialNumber and the highestBidderKey in the
     * item with the bidKey passed. If both match it returns true to indicate that the Agent has won the bid on the
     * item.
     */
    private boolean winner(int serialNumber, int bidKey)
    {
        for (AuctionItem aWinnerList : winnerList)
        {
            if (aWinnerList.getItemSerialNum() == serialNumber && aWinnerList.getHighestBidderKey() == bidKey)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @param toCentral - An object output stream that allows an auction house to communicate with auction central.
     * closingProtocol is used when an auction house needs to shut down early. When closingProtocol is called it loops
     * through each auctionItem in the items list. If the highestBidderKey field is not null, i.e. the item has not yet
     * been bid on, it creates a new AuctionTransaction that informs auction central to release the hold on the given
     * key for the given amount. It then writes the AuctionTransaction to auctionCentral via toCentral object stream.
     * It has a simple print statement that occurs after the for loop to indicate the method executed.
     */
    private void closingProtocol(ObjectOutputStream toCentral)
    {

        for (AuctionItem item : items)
        {
            if (item.getHighestBidderKey() != null)
            {

                Integer key = item.getHighestBidderKey();

                int amount = item.getHighestBid();

                AuctionTransaction release = new AuctionTransaction(key, amount, 1);

                try
                {
                    toCentral.writeObject(release);
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Early close caught, closing protocol completed.");
    }
}
