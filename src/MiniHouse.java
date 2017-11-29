import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

class MiniHouse extends Thread
{
    //Socket used to communicate with an agent.
    private Socket agentSocket = null;

    //Socket used to communicate with auction central.
    private Socket centralSocket = null;

    private ObjectInputStream centralIn;

    private ObjectOutputStream centralOut;

    //List of items for sale by this auction house.
    private ArrayList<AuctionItem> items = null;

    //The key used by the auction house, used to update the current bid amount on an item.
    private final Integer houseKey;

    //Constructor for a mini auction house.
    MiniHouse(Socket agent, Socket central, ArrayList<AuctionItem> items, Integer houseKey, ObjectOutputStream out,
              ObjectInputStream in)
    {
        //set the agent socket variable.
        this.agentSocket = agent;

        //Set the central socket variable.
        this.centralSocket = central;

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
                    //Create a bid to pass back to the agent.
                    Bid passedBid = bidProtocol(centralIn, centralOut, (Bid) passed);

                    System.out.println("Bid protocol finished");

                    //Write the created bid back to the agent.
                    outFromHouse.writeObject(passedBid);

                    System.out.println("Passing back the bid");

                    //Implement a timer here if the bid was accepted.
                }

                //If the object is a string.
                else if(passed instanceof String)
                {
                    //Store the message in a string and set its font to lower case.
                    String message = ((String) passed).toLowerCase();

                    System.out.println(message);

                    //If the string is equal to exit, set the listening variable to false.
                    if(message.equals("exit"))
                    {
                        listening = false;
                    }

                    //If the string is equal to list return the item list to the agent.
                    else if(message.equals("list"))
                    {
                        outFromHouse.writeObject(items);
                    }

                    //If the message is something else return an error message for agent to process.
                    else
                    {
                        outFromHouse.writeObject("error");
                    }

                }
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
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

        //Idex of the item based on the items id.
        int itemIndex = item.getItemId();

        //If the agents bid amount is greater then the current bid create a new hold.
        if(items.get(itemIndex).getCurrentBid() < agentBid.getBidAmount())
        {

            //Create a transaction to pass to auction central.
            AuctionTransaction hold = new AuctionTransaction(agentKey, bidAmount, -1);
            System.out.println("Created auction transaction");

            try
            {
                //Write the Transaction object to central
                toCentral.writeObject(hold);

                System.out.println("Wrote the auction transaction to auction central");

                //Get centrals confirmation of hold.
                holdConfirm = (Boolean) fromCentral.readObject();

                System.out.println("Recived confirmation of hold");

            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            //If we were able to place a hold on the agents account and the currentBid is still less then the
            //agents bid. I did this extra check to make sure that the agents bid was still the highest bid after
            //the possible time delay waiting for central to transmit its hold confirmation not sure if it is needed.
            if(holdConfirm && items.get(itemIndex).getCurrentBid() < agentBid.getBidAmount())
            {
                //Transaction that will be used to release the previous agents funds.
                AuctionTransaction release;

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

                    //Update the highestBidderKey to the agents key
                    items.get(itemIndex).setHighestBidKey(agentKey);

                    //Set release to a new transaction.
                    release = new AuctionTransaction(items.get(itemIndex).getPreviousBidderKey(),
                                              items.get(itemIndex).getPreviousBid(), 1);
                }

                try
                {
                    //Send the Transaction to auction central.
                    toCentral.writeObject(release);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //Update the bid status on the bid.
                agentBid.setBidStatus("acceptance");
            }

            //If we were able to confirm a hold but the current bid changed in the time it took to get a confirmation
            else if(holdConfirm && items.get(itemIndex).getCurrentBid() > agentBid.getBidAmount())
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

                //Set the bid status to pass.
                agentBid.setBidStatus("pass");
            }

            //If we were not able to place a hold on the agents account.
            else
            {
                agentBid.setBidStatus("rejection");
            }
        }

        //The bid amount was lower or equal to the current bid amount.
        else
        {
            agentBid.setBidStatus("pass");
        }

        return agentBid;
    }
}
