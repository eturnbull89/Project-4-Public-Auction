
import com.sun.org.apache.regexp.internal.RE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AuctionCentralProtocol
{

    //Used to tell the status of a bid.
    private final int PENDING = -1;//Whether the bid pends confirmation from bank
    private final int WITHDRAW = 0;//The transaction completed and the money needs to be withdrawn
    private final int RELEASE = 1;//Release money hold

    //object handles sending transactions to the bank and recieving data from bank
    private TalkToBank bankConnection;

    private Random keyGenerator = new Random(1000);

    /*Maps a bid key to bank key's. Map is added to when an agent registers with AuctionCentral.AuctionCentral and bid key is
    * created by this class then passed back to the agent.*/
    //TalkToAuctionHouse uses this hashMap, that's the reason it's package-protected and
    //not private. NOTE: multiple threads may be reading from and writing to hash map
    //both actions should be synchronized and performed atomically
    static HashMap<Integer, Integer> BidKeyToBankKey = new HashMap<>();


    //Used as a lock for when data is written to and read from the BidKeyToBankKey mapping
    //Multiple threads will access this data
    public static Object keyLock = new Object();



    /*Holds the registration objects from all AuctionHouses registered with this auctionCentral.
    * Registration holds, 1)house name 2)port number 3)host name*/
    static HashMap<Registration, Integer> HouseToSecretKey = new HashMap<>();

    /**Handles communication between the Clients,
     * @param bankSocket Used to communicate to the bank when AuctionHouses send requests to place money holds
     * */
    public AuctionCentralProtocol(TalkToBank bankSocket)
    {
        bankConnection = bankSocket;
    }

    /**Executes protocol for communicating with a house.
     * AuctionHouse sends the first object, Registration
     * A secretKey and PublicID are created for the Auction house.
     * The secretKey and PublicID are added to a confirmation object and sent back to the AuctionHouse letting it know
     * that it has registered. The method then enters an infinite loop which waits for the AucitonHouse to request
     * placing funds in hold.
     * Loop exits when the connection is lost to the AuctionHouse.
     *
     * When a request for placing funds in hold is received, an AuctionTransaction object, a new Transaction object is
     * made. The AuctionTransaction has the bidKey, amountBid and the type of request whether the money should be placed
     * on hold, withdrawn or a hold should be released. the BidKeyToBankKey hashMap is used to get the Agents bankKey
     * and is added to the Transaction Object along with the amount and type of request. The Transaction is sent to bank
     * and waits for the bank to send back a boolean confirmation. The confirmation is sent back to the auctionHouse
     * as a boolean.
     *
     * @param houseReg The registration object first sent by the AuctionHouse.
     *
     * @param out ObjectOutputStream used to send objects to the AuctionHouse
     *
     * @param in ObjectInputStream used to recieve objects from the AuctionHouse
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void CommunicateWithHouse(Registration houseReg, ObjectInputStream in, ObjectOutputStream out)
    {
        Integer secretKey;

        //Assign secretKey to a value not already generated
        while(HouseToSecretKey.containsValue((secretKey = keyGenerator.nextInt(100)))){}
        HouseToSecretKey.put(houseReg, secretKey);


        int publicID = keyGenerator.nextInt(100); //not sure what this is. Sets to random integer for now


        AuctionCentralServer.debug("created secretKey and publicID" + secretKey + " " + publicID);


        Confirmation houseConfirmation = new Confirmation(publicID, secretKey);

        //Send out confirmation and communicate with house
        try
        {

            out.writeObject(houseConfirmation);
            AuctionCentralServer.debug("Sent confirmation back to house" + publicID);


            while (true)
            {
                Object fromHouse;
                out.flush();
                fromHouse = in.readObject();

                if (fromHouse instanceof AuctionTransaction)
                {
                    AuctionCentralServer.debug("Transaction recived. ");
                    AuctionTransaction ClientBid = (AuctionTransaction) fromHouse;

                    //Grabbing the key Atomic
                    //get Bank key for transaction
                    Integer key;
                    synchronized (keyLock)
                    {
                        key = BidKeyToBankKey.get(ClientBid.bidKey);
                    }

                    AuctionCentralServer.debug("Bidkey " + ClientBid.bidKey + " to bank key " + key);

                    Transaction trans = new Transaction(key, ClientBid.amount, ClientBid.request);
                    
                    AuctionCentralServer.debug("sending transaction to bank");

                    Boolean result = bankConnection.RequestFromBank(trans);

                    out.writeObject(result);
                    out.reset();

                    AuctionCentralServer.debug("Result sent");
                }
                else
                    {
                        out.writeObject(false);
                    }
            }
        } catch (IOException | ClassNotFoundException e)
        {
            AuctionCentralServer.debug("lost connection to house");
            HouseToSecretKey.remove(houseReg,secretKey);

            AuctionCentralServer.debug("houses Left Now:");
            int i = 1;
            for(Registration r: HouseToSecretKey.keySet()){
                AuctionCentralServer.debug(i + ") " + r.getHouseName());
                i++;
            }
        }
    }


    /**Executes protocol for communicating with an Agent
     * Agent sends the first object, Bank key.
     * A bid Key is generated for the Agent and the mapping from the bid key to bank key is added to the hashMap
     * BidKeyToBankKey. The bidKey is sent back to the Agent and the method enters an infinite loop which exits when
     * the connection to the agent is lost.
     *
     * A wait is initiated in the while loop waiting for the agent to send a string. Regardless of the string
     * A list of Registered houses is sent back to the Agent as an ArrayList of Registrations
     * Registration holds the houses port number, host name and name of the AuctionHouse so the agent can connect
     * with any registered Auction House.
     *
     * @param agentBankKey First Object sent by Agent, added to mapping BidKeyToBankKey after bid key gets generated.
     *
     * @param out ObjectOutputStream used to send Objects out to the agent
     *
     * @param in ObjectInputStream used to receive objects from the agent
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void CommunicateWithAgent(Integer agentBankKey, ObjectInputStream in,  ObjectOutputStream out)
    {
        /*First add bidkeyToBankKey mapping
        * Generate BidKey
        * then Add to mapping and send back bidKey to agent*/

        Integer bidKey;
        ArrayList<Registration> houses = new ArrayList<>();
        ArrayList<Registration> housesToRemove = new ArrayList<>();

        AuctionCentralServer.debug("New agent sent bankKey: " + agentBankKey);

        //generate a bidkey that has not already been generated
        while(BidKeyToBankKey.containsKey((bidKey = keyGenerator.nextInt(100)))){}

        AuctionCentralServer.debug("Agents BidKey: " + bidKey);


        //Synchronized so that only one thread can read or write to the Mapping at a time
        synchronized (keyLock)
        {
            BidKeyToBankKey.put(bidKey, agentBankKey); //add to mapping
        }

        try
        {
            AuctionCentralServer.debug("sent bidkey to agent: " + bidKey );



            out.flush();
            out.writeObject(bidKey);
            Object fromAgent;
            while (true)
            {

                AuctionCentralServer.debug("waiting for agent communication");


                out.flush();
                out.reset();
                fromAgent = in.readObject();

                if (fromAgent instanceof String)
                {

                    //Add new registered houses to list of houses
                    for (Registration e : HouseToSecretKey.keySet()){
                        if(!houses.contains(e))
                            houses.add(e);
                    }

                    //remove disconnected or closed houses
                    for(Registration e: houses){
                        if(!HouseToSecretKey.containsKey(e)){
                            housesToRemove.add(e);
                        }
                    }
                    houses.removeAll(housesToRemove);
                    housesToRemove.clear();

                    AuctionCentralServer.debug("sent house list to agent");
                    out.writeObject(houses);
                    out.reset();
                }
            }
        }catch(IOException e){
            System.out.println("lost connection to agent");
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}