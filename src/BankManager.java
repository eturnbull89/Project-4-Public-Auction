import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Adam Spanswick
 * BankManger sets up a server connection for both a agent and the auction central on different ports. To use this class
 * run main.
 */
public class BankManager
{
    /**
     * main: sets up a server connection for a agent and auction central and creates a new BankServerCommuncation object for
     * each to proccess their requests.
     * @param args
     */
    public static void main(String[] args)
    {
        Bank bank = new Bank();
        int numArgs = 2;

        if(numArgs != args.length)
        {
            System.out.println("Incorrect number of arguments");
        }

        try
        {
            //Auction Central Connection
            ServerSocket auctionCentralServerConnect = new ServerSocket(Integer.parseInt(args[0]));
            Socket clientAuctionCentral = auctionCentralServerConnect.accept();

            ObjectOutputStream auctionCentralOut = new ObjectOutputStream(clientAuctionCentral.getOutputStream());
            auctionCentralOut.flush();
            ObjectInputStream auctionCentralIn = new ObjectInputStream(clientAuctionCentral.getInputStream());

            //Handle objects from Auction Central
            BankServerCommunication commWithAuctionCentral = new BankServerCommunication(bank, clientAuctionCentral, auctionCentralOut, auctionCentralIn);

            //Agent Connection
            ServerSocket agentServerConnect = null;
            Socket clientAgent = null;

            try
            {
                agentServerConnect = new ServerSocket(Integer.parseInt(args[1]));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            while(true)
            {
                try
                {
                    clientAgent = agentServerConnect.accept(); //if there are new agent requests, we will create a new agent communication thread
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }

                //New thread for every agent
                BankServerCommunication readingForAgent = new BankServerCommunication(bank, clientAgent, null, null);
            }
        }
        catch(IOException ee)
        {
            ee.printStackTrace();
        }
    }
}