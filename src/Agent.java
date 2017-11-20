import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.UnknownObjectException;

/**
 * Created by tristin on 11/18/2017.
 */
public class Agent
{
    private String agentName;   //to have 100% unique agent IDs I think the names would have to be made from a server
    private Account bankAccount;    //given from a bank
    private String biddingKey;
    //private AuctionCentral
    //private AuctionHouse

    /**
     * So an agent needs to be it's own client, so it will have it's own Main method and not a
     * constructor. The client will somehow know the hostname of the Bank server and it will be the
     * first argument when the Agent(client) program is launched, it will first pass the Bank the bankAccount
     * object, letting the Bank know it wants to be registered, the Bank then returns the bankAccount object
     * with the correctly filled out fields.
     *
     * I'm not sure how a server can return an entire object, only Strings at this point. So we may need the server
     * to return just the info of the bank account and then parse the data into the bankAccount object.
     * I also think that the AgentName may have to be given from the Bank(server), otherwise the agentNames can't be
     * truly unique.
     */

    public static void main(String[] args) throws IOException, UnknownObjectException, ClassNotFoundException
    {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Agent agent = new Agent();

        try(
            Socket agentSocket = new Socket(hostName, portNumber);  //setup socket
            ObjectOutputStream out = new ObjectOutputStream(agentSocket.getOutputStream()); //setup object output first
            ObjectInputStream in = new ObjectInputStream(agentSocket.getInputStream()); //setup object input next
        )
        {   //sets up bank account initially
            out.writeObject(agent.bankAccount); //write the bank account to the object for the Bank to read
            out.flush();
            agent.bankAccount = (Account) in.readObject(); //read from the socket the bank account from the bank
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

        /*
            idea: while loop here to ask to read input from user,
            can read if the user wants to talk to auctionHouse,
            will then call AutionHouse method that talks to AuctionHouse server
         */
    }


}
