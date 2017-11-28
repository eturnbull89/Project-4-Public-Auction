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
            ServerSocket auctionCentralServerConnect = new ServerSocket(1026); //Auction Central Connection
            Socket clientAC = auctionCentralServerConnect.accept();

            ObjectOutputStream auctionCentralOut = new ObjectOutputStream(clientAC.getOutputStream());
            auctionCentralOut.flush();
            ObjectInputStream auctionCentralIn = new ObjectInputStream(clientAC.getInputStream());

            //Agent Connection
            ServerSocket agentServerConnect = new ServerSocket(1031); //Agent Connection
            Socket clientAgent = agentServerConnect.accept();

            ObjectOutputStream agentOut = new ObjectOutputStream(clientAgent.getOutputStream());
            agentOut.flush();
            ObjectInputStream agentIn = new ObjectInputStream(clientAgent.getInputStream());

            //Handle objects from a Agent
            BankServerCommunication readingForAgent = new BankServerCommunication(bank, clientAgent, agentOut, agentIn);

            //Handle objects from Auction Central
            BankServerCommunication commWithAuctionCentral = new BankServerCommunication(bank, clientAC, auctionCentralOut, auctionCentralIn);
        }
        catch(IOException ee)
        {
            ee.printStackTrace();
        }
    }
}


//import java.io.*;
//        import java.net.*;
//
//public class BankManager
//{
//    public static void main(String[] args)
//    {
//        Bank bank = new Bank();
//        Object inObj;
//
//        try
//        {
//            ServerSocket server = new ServerSocket(1026);
//
//            Socket client = server.accept();
//
//            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
//            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
//            out.flush();
//
//            while (true)
//            {
//                out.flush();
//                System.out.println("trying to read object");
//                inObj = in.readObject();
//                System.out.println("read object");
//
//                if (inObj instanceof UserAccount)
//                {
//                    UserAccount acct = (UserAccount) inObj;
//                    System.out.println(acct.getAccountName());
//                    bank.createNewAccount(acct.getAccountName());
//
//                    AcctKey userKey = new AcctKey(bank.getBankKey(acct.getAccountName()), bank.getAccount(bank.getBankKey(acct.getAccountName())).getAccountNumber());
//                    System.out.println(userKey.getKey() + "   " + userKey.getAccountNumber());
//
//                    out.writeObject(userKey);
//
////          in.close();
////          out.close();
////          client.close();
//                }
//                else if (inObj instanceof Transaction)
//                {
//                    Transaction newTrans = (Transaction) inObj;
//
//                    if (newTrans.request == -1) //Place amount in hold
//                    {
//                        out.writeObject(bank.getAccount(newTrans.bankKey).setHoldBalance(newTrans.amount));
//                    } else if (newTrans.request == 1) //Release the funds in hold back to user's account
//                    {
//                        out.writeObject(bank.getAccount(newTrans.bankKey).clearHold());
//                    } else if (newTrans.request == 0) //Withdraw the funds in hold from the user's account
//                    {
//                        out.writeObject(bank.getAccount(newTrans.bankKey).deductHoldAmount(newTrans.amount));
//                    }
//
////          in.close();
////          out.close();
////          client.close();
//                } else if (inObj instanceof String)
//                {
//                    inObj = in.readObject();
//
//                    if (inObj instanceof Integer)
//                    {
//                        System.out.println("bank key received");
//                        Integer key = (Integer) inObj;
//
//                        out.writeObject(bank.getAccount(key).inquiry());
//                    }
//
////          in.close();
////          out.close();
////          client.close();
//                }
//            }
//        }
//        catch(IOException ee)
//        {
//            ee.printStackTrace();
//        } catch(ClassNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//    }
//}

