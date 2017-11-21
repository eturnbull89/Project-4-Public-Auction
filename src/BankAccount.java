import java.io.Serializable;

/**
 * BankAccount extends account so it has all of the getters, but also has setters for the account now.
 * This version of the account will be used by the Bank.
 */
public class BankAccount extends Account implements Serializable
{
    private Bank bank;
    private double accountNumber;
    private double balance;
    private Integer bankKey;

    public BankAccount(Bank bank)
    {
        super(bank);
    }

    public void setAccountNumber(double accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance)
    {
        this.balance = balance;
    }

    public void setBankKey(Integer bankKey)
    {
        this.bankKey = bankKey;
    }
}
