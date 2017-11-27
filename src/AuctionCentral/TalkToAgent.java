package AuctionCentral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

public class TalkToAgent extends Thread {

    private Socket AgentSocket;

    private Integer biddingKey;


    private ObjectOutputStream out;
    private ObjectInputStream in;

    TalkToAgent(Integer biddingKey, Socket AgentSocket,ObjectInputStream input, ObjectOutputStream output){
        this.AgentSocket = AgentSocket;
        this.biddingKey = biddingKey;

        try {
            out = output;
            out.flush();
            in = input;
            out.writeObject(biddingKey);
        }catch (IOException e){
            e.printStackTrace();
        }
        this.start();
    }
    @Override
    public void run() {
        Object fromAgent;
        while(true){
            try{
                System.out.println("waiting for agent communication");
                fromAgent = in.readObject();
                if (fromAgent instanceof String) {
                    System.out.print(fromAgent);
                    Set keySet = AuctionCentral.HouseToSecretKey.keySet();

                    out.writeObject(AuctionCentral.HouseToSecretKey.keySet());
                    out.flush();
                }

            }catch (IOException|ClassNotFoundException e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
