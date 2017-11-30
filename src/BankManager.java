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
        try
        {
            //Auction Central Connection
            ServerSocket auctionCentralServerConnect = new ServerSocket(1026);
            Socket clientAC = auctionCentralServerConnect.accept();

            ObjectOutputStream auctionCentralOut = new ObjectOutputStream(clientAC.getOutputStream());
            auctionCentralOut.flush();
            ObjectInputStream auctionCentralIn = new ObjectInputStream(clientAC.getInputStream());

            //Handle objects from Auction Central
            BankServerCommunication commWithAuctionCentral = new BankServerCommunication(bank, clientAC, auctionCentralOut, auctionCentralIn);

            ServerSocket agentServerConnect = null;
            Socket clientAgent = null;

            try
            {
              agentServerConnect = new ServerSocket(1031);
            }    //try to setup server socket on port
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

                //new thread for agent client
                BankServerCommunication readingForAgent = new BankServerCommunication(bank, clientAgent, null, null);
            }
        }
        catch(IOException ee)
        {
            ee.printStackTrace();
        }
    }
}

