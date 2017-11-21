import java.io.Serializable;
import java.util.Hashtable;

/**
 * Account is abstract so Agents don't have any access to setters,
 * only getters for the account
 */
public abstract class Account implements Serializable
{
    private Bank bank;
    private double accountNumber;
    private double balance;
    private Integer bankKey;   //is this right? Agent as a value and return a String as a key?

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

    public Integer getBankKey()
    {
        return bankKey;
    }

    public double getAccountNumber()
    {
        return accountNumber;
    }
}
