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

    //List of items for sale by this auction house.
    private ArrayList<AuctionItem> items = null;

    //The key used by the auction house, used to update the current bid amount on an item.
    private final Integer houseKey;

    //Constructor for a mini auction house.
    MiniHouse(Socket agent, Socket central, ArrayList<AuctionItem> items, Integer houseKey)
    {
        //set the agent socket variable.
        this.agentSocket = agent;

        //Set the central socket variable.
        this.centralSocket = central;

        //Set the items variable.
        this.items = items;

        //Set the houseKey variable to the auction house key.
        this.houseKey = houseKey;
    }

    public void run()
    {
        try
        {
            //Create an object output stream to the agent.
            ObjectOutputStream outFromHouse = new ObjectOutputStream(agentSocket.getOutputStream());

            //Create an object output stream to auction central.
            ObjectOutputStream toCentral = new ObjectOutputStream(centralSocket.getOutputStream());

            //Create an object input stream from an agent.
            ObjectInputStream inFromAgent = new ObjectInputStream(agentSocket.getInputStream());

            //Create an object input stream from auction central.
            ObjectInputStream fromCentral = new ObjectInputStream(centralSocket.getInputStream());

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
                    Bid passedBid = bidProtocol(fromCentral, toCentral, (Bid) passed);

                    //Write the created bid back to the agent.
                    outFromHouse.writeObject(passedBid);

                    //Implement a timer here if the bid was accepted.
                }

                //If the object is a string.
                else if(passed instanceof String)
                {
                    //Store the message in a string and set its font to lower case.
                    String message = inFromAgent.readObject().toString().toLowerCase();

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

        //If the agents bid amount is greater then the current bid create a new hold.
        if(item.getCurrentBid() < agentBid.getBidAmount())
        {
            //Create a transaction to pass to auction central.
            Transaction hold = new Transaction(agentKey, bidAmount, -1);

            try
            {
                //Write the Transaction object to central
                toCentral.writeObject(hold);

                //Get centrals confirmation of hold.
                holdConfirm = (Boolean) fromCentral.readObject();

            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            //If we were able to place a hold on the agents account.
            if(holdConfirm)
            {
                //Update the items current bid amount.
                item.setCurrentBid(bidAmount, houseKey);

                //Update the bid status on the bid.
                agentBid.setBidStatus("acceptance");
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
