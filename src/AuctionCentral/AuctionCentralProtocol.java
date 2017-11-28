package AuctionCentral;

import AuctionCentral.SerializedObjects.Bid;
import AuctionCentral.SerializedObjects.Confirmation;
import AuctionCentral.SerializedObjects.Registration;
import AuctionCentral.SerializedObjects.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AuctionCentralProtocol
{

    //Used to tell the status of a bid.
    private final int PENDING = -1;//Whether the bid pends confirmation from bank
    private final int WITHDRAW = 0;//The transaction completed and the money needs to be withdrawn
    private final int RELEASE = 1;//Release money hold

    private TalkToBank bankConnection;

    private Random keyGenerator = new Random(1000);
    /*Maps a bid key to bank key's. Map is added to when an agent registers with AuctionCentral.AuctionCentral and bid key is
    * created by this class then passed back to the agent.*/
    //TalkToAuctionHouse uses this hashMap, that's the reason it's package-protected and
    //not private. NOTE: multiple threads may be reading from and writing to hash map
    //both actions should be synchronized and performed atomically
    static HashMap<Integer, Integer> BidKeyToBankKey = new HashMap<>();

    /*Holds the registration objects from all AuctionHouses registered with this auctionCentral.
    * Registration holds, 1)house name 2)port number 3)host name*/
    static HashMap<Registration, Integer> HouseToSecretKey = new HashMap<>();


    public AuctionCentralProtocol(TalkToBank bankSocket)
    {
        bankConnection = bankSocket;
    }

    public void CommunicateWithHouse(Registration houseReg, ObjectInputStream in, ObjectOutputStream out)
    {
        Integer secretKey = keyGenerator.nextInt(100);
        //TODO: check if key was already generated

        HouseToSecretKey.put(houseReg, secretKey); //add house and secret key to mapping
        //Create public Id
        int publicID = keyGenerator.nextInt(100); //not sure what this is. Sets to random integer for now

        //Create confirmation and send back
        Confirmation houseConfirmation = new Confirmation(publicID, secretKey);

        //Send out confirmation and communicate with house
        try
        {
            out.writeObject(houseConfirmation);// send confirmation back to house

            while (true)
            {
                Object fromHouse;
                while (true)
                {
                    out.flush();
                    fromHouse = in.readObject();
                    if (fromHouse instanceof Bid)
                    {

                        Bid ClientBid = (Bid) fromHouse;
                        //TODO: make Grabbing the key Atomic

                        //get Bank key for transaction
                        Integer key = BidKeyToBankKey.get(ClientBid.getAgentBidKey());

                        //create transaction
                        Transaction trans = new Transaction(key, ClientBid.getBidAmount(), ClientBid.getRequest());

                        //send transaction to bank and
                        //wait for bankServer response on the success of the transaction
                        boolean result = bankConnection.RequestFromBank(trans);

                        //if response from bank true release old hold
                        if (result && ClientBid.getOldAgentBidKey() != null)
                        {
                            //create Transaction
                            //TODO: make Grabbing the key Atomic
                            Integer oldKey = BidKeyToBankKey.get(ClientBid.getOldAgentBidKey());
                            int amountToRelease = ClientBid.getOldAgentBidAmount();
                            Transaction releaseHold = new Transaction(oldKey, amountToRelease, RELEASE);


                            /*Right now nothing checks for the bank to return any status on releasing
                            a hold. I don't believe the situation should occur where the bank can not
                            release a hold. Maybe if the account does not have the given amount on hold
                            but i'm not sure why this would happen*/
                            //send Transaction to bank
                            bankConnection.RequestFromBank(releaseHold);
                        }
                        out.writeObject(result);
                    } else
                    {
                        out.writeObject(false);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void CommunicateWithAgent(Integer agentBankKey, ObjectInputStream in, ObjectOutputStream out)
    {
        //add bidkeyToBankKey mapping
        System.out.println("New agent sent bankKey: " + agentBankKey);
        Integer bidKey = keyGenerator.nextInt(100); //create bidding key for agent
        System.out.println("Agents BidKey: " + bidKey);

        //TODO: check if bidkey previously generated

        BidKeyToBankKey.put(bidKey, agentBankKey);


        try
        {
            out.writeObject(bidKey);
            Object fromAgent;
            while (true)
            {

                System.out.println("waiting for agent communication");
                fromAgent = in.readObject();

                if (fromAgent instanceof String)
                {
                    System.out.print(fromAgent);

                    //create arraylist of registrations
                    ArrayList<Registration> keySet = new ArrayList<>();
                    for (Registration e : HouseToSecretKey.keySet()) keySet.add(e);

                    out.writeObject(keySet);
                    out.flush();
                }
            }
        }catch(IOException|ClassNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }

    }
}