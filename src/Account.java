import java.util.Hashtable;

/**
 * Created by tristin on 11/18/2017.
 */
public class Account
{
    private Bank bank;
    private double accountNumber;
    private double balance;
    private Hashtable<Agent, String> bankKey = new Hashtable<>();   //is this right? Agent as a value and return a String as a key?

    public Account(Bank bank)
    {
        this.bank = bank;
    }

    private void updateAccount()
    {
        return;
    }

    public double getBalance()
    {
        return balance;
    }

    public Hashtable<Agent, String> getBankKey()
    {
        return bankKey;
    }

    public double getAccountNumber()
    {
        return accountNumber;
    }

    public void setBalance(double balance)
    {
        this.balance = balance;
    }

    public void setAccountNumber(double accountNumber)
    {
        this.accountNumber = accountNumber;
    }
}
