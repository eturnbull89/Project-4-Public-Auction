import java.io.*;
import java.net.*;

public class BankManager
{
  public static void main(String[] args)
  {
    String hostName = "adam-UX360CA";
    Object userAccoun, central;
    Bank bank = new Bank();
    Object inObj;

    try
    {

      ServerSocket server = new ServerSocket(30000);

      Socket client = server.accept();

      ObjectInputStream in = new ObjectInputStream(client.getInputStream());
      ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
      out.flush();

      inObj = in.readObject();

      if (inObj instanceof UserAccount)
      {
        UserAccount acct = (UserAccount) inObj;
        System.out.println(acct.getAccountName());
        bank.createNewAccount(acct.getAccountName());

        AcctKey userKey = new AcctKey(bank.getBankKey(acct.getAccountName()), bank.getAccount(bank.getBankKey(acct.getAccountName())).getAccountNumber());
        System.out.println(userKey.getKey() + "   " + userKey.getAccountNumber());

        out.writeObject(userKey);

        in.close();
        out.close();
        client.close();
      } else if (inObj instanceof Transaction)
      {
        Transaction newTrans = (Transaction) inObj;

        if (newTrans.request == -1) //Place amount in hold
        {
          out.writeObject(bank.getAccount(newTrans.bankKey).setHoldBalance(newTrans.amount));
        } else if (newTrans.request == 1) //Release the funds in hold back to user's account
        {
          out.writeObject(bank.getAccount(newTrans.bankKey).clearHold());
        } else if (newTrans.request == 0) //Withdraw the funds in hold from the user's account
        {
          out.writeObject(bank.getAccount(newTrans.bankKey).deductHoldAmount(newTrans.amount));
        }

        in.close();
        out.close();
        client.close();
      }
        else if (inObj instanceof String)
        {
          String inMessage = (String) inObj;
          System.out.println(inMessage);

          out.writeObject();

          in.close();
          out.close();
          client.close();
        }
    }
    catch (IOException ee)
    {
      ee.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
  }
}
