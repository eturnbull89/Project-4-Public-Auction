package AuctionCentral;

import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class talkToAuctionHouse extends Thread {

    //Used to tell the status of a bid.
    private final int PENDING = -1;//Whether the bid pends confirmation from bank
    private final int WITHDRAW = 0;//The transaction completed and the money needs to be withdrawn
    private final int RELEASE = 1;//Release money hold

    private Socket HouseSocket;

    private TalkToBank bankConnection;

    public talkToAuctionHouse(Socket HouseSocket, TalkToBank bankConnection) {
        this.HouseSocket = HouseSocket;
        this.bankConnection = bankConnection;
        this.start();
    }

    @Override
    public void run() {

        try {
            //establish communication with the house
            ObjectOutputStream out = new ObjectOutputStream(HouseSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(HouseSocket.getInputStream());

            Object fromHouse;
            while (true) {
                fromHouse = in.readObject();
                if (fromHouse instanceof Bid) {
                    Bid ClientBid = (Bid) fromHouse;
                    //TODO: make Grabbing the key Atomic
                    Integer key = AuctionCentral.BidKeyToBankKey.get(ClientBid.getAgentBidKey());

                    //create transaction
                    Transaction trans = new Transaction(key, ClientBid.getBidAmount(), ClientBid.getRequest());

                    //send transaction to bank
                    //wait for bankServer response on the success of the transaction
                    boolean result = bankConnection.RequestFromBank(trans);

                    //if response from bank true release old hold
                    if (result && ClientBid.getOldAgentBidKey() != null) {
                        //create Transaction
                        //TODO: make Grabbing the key Atomic
                        Integer oldKey = AuctionCentral.BidKeyToBankKey.get(ClientBid.getOldAgentBidKey());
                        int amountToRelease = ClientBid.getOldAgentBidAmount();
                        Transaction releaseHold = new Transaction(oldKey, amountToRelease, RELEASE);


                        /*Right now nothing checks for the bank to return any status on releasing
                        * a hold. I don't believe the situation should occur where the bank can not
                        * release a hold. Maybe if the account does not have the given amount on hold
                        * but i'm not sure why this would happen*/
                        //send Transaction to bank
                        bankConnection.RequestFromBank(releaseHold);
                    }
                    out.writeObject(result);
                } else {
                    out.writeObject(false);
                }
            }
        }catch(IOException|ClassNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
