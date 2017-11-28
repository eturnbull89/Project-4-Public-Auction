import java.io.Serializable;
import java.util.Hashtable;

/**
 * Bankunt is abstract so Agents don't have any access to setters,
 * only getters for the account
 */
public abstract class Account implements Serializable
{
//    private Bank bank;
    private Integer accountNumber;
    private Double balance;
    private Integer bankKey;   //is this right? Agent as a value and return a String as a key?

    //ideally, it would be nice if the account had access to the bank, not sure how to make happen though
    //without servers passing references
//    public Bankunt(Bank bank)
//    {
//        this.bank = bank;
//    }

    private void updateAccount()
    {
        return;
    }

    public Double getBalance()
    {
        return balance;
    }

    public Integer getBankKey()
    {
        return bankKey;
    }

    public Integer getAccountNumber()
    {
        return accountNumber;
    }
}
