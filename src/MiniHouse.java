import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class MiniHouse extends Thread
{
    //Socket used to communicate with an agent.
    private Socket agentSocket = null;

    private ObjectInputStream centralIn;

    private ObjectOutputStream centralOut;

    //List of items for sale by this auction house.
    private ArrayList<AuctionItem> items = null;

    //List of auction items that contain the winners info.
    private ArrayList<AuctionItem> winnerList = new ArrayList<>();

    //The key used by the auction house, used to update the current bid amount on an item.
    private final Integer houseKey;

    //Constructor for a mini auction house.
    MiniHouse(Socket agent, ArrayList<AuctionItem> items, Integer houseKey, ObjectOutputStream out,
              ObjectInputStream in)
    {
        //set the agent socket variable.
        this.agentSocket = agent;

        //Set the items variable.
        this.items = items;

        //Set the houseKey variable to the auction house key.
        this.houseKey = houseKey;

        this.centralIn = in;

        this.centralOut = out;
    }

    public void run()
    {
        try
        {
            //Create an object output stream to the agent.
            ObjectOutputStream outFromHouse = new ObjectOutputStream(agentSocket.getOutputStream());

            //Needed flush on output stream
            outFromHouse.flush();

            //Needed flush on output stream
            centralOut.flush();

            //Create an object input stream from an agent.
            ObjectInputStream inFromAgent = new ObjectInputStream(agentSocket.getInputStream());

            //Boolean to keep listening for a clients input until they exit.
            boolean listening = true;

            //while listening is true continue to processes an agents requests.
            while(listening)
            {
                Object passed = inFromAgent.readObject();

                //If the object that is read is a bid handel bidding procedure.
                if(passed instanceof Bid)
                {
                    /*System.out.println("passed bid info:");
                    System.out.println(((Bid) passed).getItemBiddingOn().getName());
                    System.out.println(((Bid) passed).getBidAmount());
                    System.out.println(((Bid) passed).getBidStatus());*/

                    //Create a bid to pass back to the agent.
                    Bid passedBid = bidProtocol(centralIn, centralOut, (Bid) passed);
                    //System.out.println("Current bid passing back to Agent " + passedBid.getItemBiddingOn().getCurrentBid());

                    System.out.println("passed bid status: "+ passedBid.getBidStatus());

                    //Write the created bid back to the agent.
                    outFromHouse.writeObject(passedBid);

                    //System.out.println("Passing back the bid");
                    System.out.println("items size: "+items.size());

                    if(passedBid.getBidStatus().equals("acceptance"))
                    {
                       countdown(passedBid);
                       printArrayList(this.items);
                    }
                }

                else if(passed instanceof itemEnquire)
                {
                    boolean inList = itemExists(((itemEnquire) passed).getSerialNumber());

                    outFromHouse.writeObject(inList);
                }

                else if(passed instanceof WinnerInquire)
                {
                    int serialNumber = ((WinnerInquire) passed).getAuctionItem().getItemSerialNum();

                    int bidKey = ((WinnerInquire) passed).getBiddingKey();

                    outFromHouse.writeObject(winner(serialNumber, bidKey));

                    System.out.println("winner inquire wrote back.");
                }

                //If the object is a string.
                else if(passed instanceof String)
                {
                    //Store the message in a string and set its font to lower case.
                    String message = ((String) passed).toLowerCase();

                    //If the message is something else return an error message for agent to process.
                    switch (message)
                    {
                        case "exit":
                            listening = false;
                            break;

                        //If the string is equal to list return the item list to the agent.
                        case "list":
                            outFromHouse.writeObject(items);
                            outFromHouse.reset();
                            break;

                        //If the message is something else return an error message for agent to process.
                        default:
                            outFromHouse.writeObject("error");
                            break;
                    }

                }
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Exit caused");
        }
    }

    private Bid bidProtocol(ObjectInputStream fromCentral, ObjectOutputStream toCentral, Bid agentBid)
    {
        //Boolean used to confirm if auction central was able to place a hold on the agents account.
        Boolean holdConfirm = false;

        //The agents bid key, used by auction central
        Integer agentKey = agentBid.getAgentBidKey();

        //The amount the agent wishes to bid.
        int bidAmount = agentBid.getBidAmount();

        //The item the agent is bidding on.
        AuctionItem item = agentBid.getItemBiddingOn();

        boolean stillListed = itemExists(item.getItemSerialNum());

        if(stillListed)
        {
            //Index of the item based on the items id.
            int itemIndex = findIndex(item.getItemSerialNum());

            boolean bidderKeysMatch = false;

            if(items.get(itemIndex).getHighestBidderKey() != null)
            {
                bidderKeysMatch = items.get(itemIndex).getHighestBidderKey().equals(agentBid.getAgentBidKey());
            }

            boolean previousBidder = items.get(itemIndex).getHighestBidderKey() == null ||
                    !(items.get(itemIndex).getHighestBidderKey().equals(agentBid.getAgentBidKey()));

            /*System.out.println("AH Items List SN: " + items.get(itemIndex).getItemSerialNum() +
                    "\nBid item SN: " + item.getItemSerialNum());

            System.out.println("item being bid on: " +item.getName());*/

            //If the agents bid amount is greater then the current bid create a new hold.
            if (items.get(itemIndex).getCurrentBid() < agentBid.getBidAmount() && previousBidder)
            {

                //Create a transaction to pass to auction central.
                AuctionTransaction hold = new AuctionTransaction(agentKey, bidAmount, -1);
                //System.out.println("Created auction transaction");

                try
                {
                    //Write the Transaction object to central
                    toCentral.writeObject(hold);

                    //System.out.println("Wrote the auction transaction to auction central");

                    //Get centrals confirmation of hold.
                    holdConfirm = (Boolean) fromCentral.readObject();

                    //System.out.println("Received confirmation of hold");

                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }

                //If we were able to place a hold on the agents account and the currentBid is still less then the
                //agents bid. I did this extra check to make sure that the agents bid was still the highest bid after
                //the possible time delay waiting for central to transmit its hold confirmation not sure if it is needed.
                if (holdConfirm && items.get(itemIndex).getCurrentBid() < agentBid.getBidAmount())
                {
                    //Transaction that will be used to release the previous agents funds.
                    AuctionTransaction release = null;

                    //Synchronize on the item in the items list to update the necessary fields before another agents thread
                    //can change them as well. This might be a problem later not sure yet.
                    synchronized (items.get(itemIndex))
                    {
                        //Set the previousBid amount to the current bid amount.
                        items.get(itemIndex).setPreviousBid(items.get(itemIndex).getCurrentBid());

                        //Set the previousBidderKey to current highestBidderKey value.
                        items.get(itemIndex).setPreviousBidderKey(items.get(itemIndex).getHighestBidderKey());

                        //Update the items current bid amount.
                        items.get(itemIndex).setCurrentBid(bidAmount, houseKey);

                        //Update the current bid amount in the item.
                        item.setCurrentBid(bidAmount, houseKey);

                        //Update the highestBidderKey to the agents key
                        items.get(itemIndex).setHighestBidKey(agentKey);

                        if (items.get(itemIndex).getPreviousBidderKey() != null)
                        {
                            //Set release to a new transaction.
                            release = new AuctionTransaction(items.get(itemIndex).getPreviousBidderKey(),
                                    items.get(itemIndex).getPreviousBid(), 1);
                        }
                    }

                    try
                    {
                        if (release != null)
                        {
                            //Send the Transaction to auction central.
                            toCentral.writeObject(release);
                        }

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    //Update the bid status on the bid.
                    agentBid.setBidStatus("acceptance");
                }

                //If we were able to confirm a hold but the current bid changed in the time it took to get a confirmation
                else if (holdConfirm && items.get(itemIndex).getCurrentBid() > agentBid.getBidAmount())
                {
                    //Create a new transaction to release the agents previous hold.
                    Transaction releaseAgent = new Transaction(agentKey, bidAmount, 1);

                    try
                    {
                        //Send the transaction to central.
                        toCentral.writeObject(releaseAgent);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    AuctionItem higherBidItem = items.get(itemIndex);

                    agentBid.getItemBiddingOn().setCurrentBid(higherBidItem.getCurrentBid(), houseKey);


                    //Set the bid status to pass.
                    agentBid.setBidStatus("reject");
                }

                //If we were not able to place a hold on the agents account.
                else
                {
                    agentBid.setBidStatus("rejection");
                }
            }

            //If the agent was the previous bidder and tried to bid again.
            else if(!previousBidder)
            {
                agentBid.setBidStatus("pass");
            }

            //The bid amount was lower or equal to the current bid amount and the agent is not the previous bidder
            else
            {
                AuctionItem higherBidItem = items.get(itemIndex);

                agentBid.getItemBiddingOn().setCurrentBid(higherBidItem.getCurrentBid(), houseKey);

                agentBid.setBidStatus("reject");
            }
        }

        else
        {
            //Update the bid status on the bid.
            agentBid.setBidStatus("Over");
        }

        return agentBid;
    }

    private void countdown(Bid agentBid)
    {
        Timer bidTimer = new Timer();

        int itemIndex = findIndex(agentBid.getItemBiddingOn().getItemSerialNum());

        AuctionItem item = items.get(itemIndex);

        bidTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if(agentBid.getBidAmount() == item.getCurrentBid())
                {
                    //Add the item to the winners list.
                    winnerList.add(item);
                    AuctionTransaction withdraw = new AuctionTransaction(item.getHighestBidderKey(), item.getCurrentBid(), 0);

                    try {
                        centralOut.writeObject(withdraw);
                        centralOut.reset();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //remove the item from the public listing.
                    items.remove(itemIndex);

                    System.out.println("wrote to winner, updated list:");

                    printArrayList(items);

                    System.out.println("winners list:");

                    printArrayList(winnerList);
                }
            }
        }, 30*1000); //make sure 30s
    }

    private void printArrayList(ArrayList<AuctionItem> ar)
    {
        System.out.println("current items in list: ");

        for(AuctionItem item : ar)
        {
            System.out.print(item.getName());
            System.out.print(", items serial number = " + item.getItemSerialNum());
            System.out.println(", items id number = "+ item.getItemId());
        }
    }

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
}
