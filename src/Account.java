import java.util.Hashtable;

/**
 * Created by tristin on 11/18/2017.
 */
public abstract class Account
{
    private Bank bank;
    private double accountNumber;
    private double balance;
    private String bankKey = "";   //is this right? Agent as a value and return a String as a key?

    //ideally, it would be nice if the account had access to the bank, not sure how to make happen though
    //without servers passing references
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

    public String getBankKey()
    {
        return bankKey;
    }

    public double getAccountNumber()
    {
        return accountNumber;
    }
}
