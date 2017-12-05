import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
    int numOfArgs = 2;

    if(args.length != numOfArgs)
    {
      System.out.println("Incorrect number of arguments");
      System.exit(-1);
    }

    //First Command line argument is the port for Auction Central to connect to
    int auctionCentralPort = Integer.parseInt(args[0]);
    //Second Command line argument is the port for the Agent to connect to
    int agentPort = Integer.parseInt(args[1]);

    try
    {
      //Auction Central Connection
      try
      {
        ServerSocket auctionCentralServerConnect = new ServerSocket(auctionCentralPort);
        while (true)
        {
          try
          {
            Socket clientAuctionCentral = auctionCentralServerConnect.accept();

            ObjectOutputStream auctionCentralOut = new ObjectOutputStream(clientAuctionCentral.getOutputStream());
            auctionCentralOut.flush();
            ObjectInputStream auctionCentralIn = new ObjectInputStream(clientAuctionCentral.getInputStream());

            //Handle objects from Auction Central
            BankServerCommunication commWithAuctionCentral = new BankServerCommunication(bank, clientAuctionCentral, auctionCentralOut, auctionCentralIn);
          }
          catch (SocketException e)
          {
            e.printStackTrace();
          }
        }
      }
      catch (IOException e)
      {
        System.out.println("Lost Connection to Auction Central");
      }

      //Agent Connection
      ServerSocket agentServerConnect = null;
      Socket clientAgent = null;

      try
      {
        agentServerConnect = new ServerSocket(agentPort);
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
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}