import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by tristin on 11/27/2017.
 */
public class BankServerCommunication implements Runnable
{
    Bank bank;
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket client;
    Object inObj;

    public BankServerCommunication(Bank bank, Socket client, ObjectOutputStream out, ObjectInputStream in)
    {
        this.out = out;
        this.in = in;
        this.client = client;
        this.bank = bank;
        Thread bsc = new Thread(this);
        bsc.start();
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                out.flush();
                System.out.println("trying to read object");
                inObj = in.readObject();
                System.out.println("read object");

                if (inObj instanceof UserAccount)
                {
                    UserAccount acct = (UserAccount) inObj;
                    System.out.println(acct.getAccountName());
                    bank.createNewAccount(acct.getAccountName());

                    AcctKey userKey = new AcctKey(bank.getBankKey(acct.getAccountName()), bank.getAccount(bank.getBankKey(acct.getAccountName())).getAccountNumber());
                    System.out.println(userKey.getKey() + "   " + userKey.getAccountNumber());

                    out.writeObject(userKey);

//          in.close();
//          out.close();
//          client.close();
                }
                else if (inObj instanceof Transaction)
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

//          in.close();
//          out.close();
//          client.close();
                }
                else if (inObj instanceof String)
                {
                    inObj = in.readObject();

                    if (inObj instanceof Integer)
                    {
                        System.out.println("bank key received");
                        Integer key = (Integer) inObj;

                        out.writeObject(bank.getAccount(key).inquiry());
                    }

//          in.close();
//          out.close();
//          client.close();
                }
            }
        }
        catch(IOException e)
        {

        }
        catch (ClassNotFoundException e)
        {

        }
    }
}
