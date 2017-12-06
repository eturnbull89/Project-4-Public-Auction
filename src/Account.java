import java.io.Serializable;
import java.util.Hashtable;

/**
 * BankAccount is abstract so Agents don't have any access to setters,
 * only getters for the account
 */
public abstract class Account implements Serializable
{
    private Integer accountNumber;
    private Double balance;
    private Integer bankKey;

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
