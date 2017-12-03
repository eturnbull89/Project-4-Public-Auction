import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * BankServerCommunication is used to differentiate between communication with a agent and with the auction central. It
 * reads the incoming object and based on their types takes the proper action.
 */
public class BankServerCommunication implements Runnable
{
    Bank bank;
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket client;
    Object inObj;

    /**
     * BankServerCommunication: Constructor sets the object input and output streams and starts a new thread for every
     * BankServerCommunication object created.
     * @param bank
     * @param client
     * @param out
     * @param in
     */
    public BankServerCommunication(Bank bank, Socket client, ObjectOutputStream out, ObjectInputStream in)
    {
        this.out = out;
        this.in = in;

        if(out == null && in == null)   //an ugly indicator that this is an agent thread
        {
            try
            {
                this.out = new ObjectOutputStream(client.getOutputStream());    //create new streams for each agent thread, without this
                this.in = new ObjectInputStream(client.getInputStream());       //agents will get stuck when the bank servers input stream is already in use
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        this.client = client;
        this.bank = bank;
        Thread bsc = new Thread(this);
        bsc.start();
    }

    /**
     * run: reads incoming objects from the ObjectInputStream. If the object is a UserAccount run creates a new bank account
     * and passes the bankKey, accountNumber and balance back to the agent. If the object is a Transaction from auction central
     * run checks a int, which either places a amount in hold on a agent's account, releases the funds back to the agent's
     * balance (lost a auction) or withdraws the amount from the agents account (won the auction). Finally, if the object is
     * a string it means a agent is inquiring about their balance and then reads in the next object which should be a integer, bankKey,
     * then gets the account from the bank and calls the inquire method in BankAccount to get the balance and funds in hold.
     */
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                out.flush();
                System.out.println("Waiting for a incoming object");
                inObj = in.readObject();
                System.out.println("Success");

                if (inObj instanceof UserAccount)
                {
                    UserAccount acct = (UserAccount) inObj;
                    System.out.println(acct.getAccountName()); //Test
                    bank.createNewAccount(acct.getAccountName());

                    AcctKey userKey = new AcctKey(bank.getBankKey(acct.getAccountName()), bank.getAccount(bank.getBankKey(acct.getAccountName())).getAccountNumber(),
                            bank.getAccount(bank.getBankKey(acct.getAccountName())).getBalance());
                    System.out.println(userKey.getKey() + "   " + userKey.getAccountNumber()); //Test

                    out.writeObject(userKey);
                }
                else if (inObj instanceof Transaction)
                {
                    Transaction newTrans = (Transaction) inObj;

                    System.out.println("Auction Central Bank Key: ");
                    System.out.println(newTrans.bankKey);

                    if (newTrans.request == -1) //Place amount in hold
                    {
                        out.writeObject(bank.getAccount(newTrans.bankKey).setHoldBalance(newTrans.amount));
                    }
                    else if (newTrans.request == 1) //Release the funds in hold back to user's account
                    {
                        out.writeObject(bank.getAccount(newTrans.bankKey).clearHold(newTrans.amount));
                    }
                    else if (newTrans.request == 0) //Withdraw the funds in hold from the user's account
                    {
                        System.out.println("Withdrawing from auction won");
                        out.writeObject(bank.getAccount(newTrans.bankKey).deductHoldAmount(newTrans.amount));
                    }
                }
                else if (inObj instanceof String)
                {
                    inObj = in.readObject();

                    if (inObj instanceof Integer)
                    {
                        System.out.println("Bank key received");
                        Integer key = (Integer) inObj;

                        out.writeObject(bank.getAccount(key).inquiry());
                    }
                }
                else if(inObj instanceof Integer)
                {
                    Integer key = (Integer) inObj;

                    inObj = in.readObject();

                    Integer bid = (Integer) inObj;

                    if(bank.getAccount(key).getBalance() >= bid)
                    {
                        out.writeObject(true);
                    }
                    else
                    {
                        out.writeObject(false);
                    }
                }
            }
        }
        catch(IOException e)
        {
            System.out.println("IOException");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("ClassNotFoundException");
        }
    }
}
