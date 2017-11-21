/**
 * Created by tristin on 11/20/2017.
 */
public class BankAccount extends Account
{
    private Bank bank;
    private double accountNumber;
    private double balance;
    private String bankKey = "";   //is this right? Agent as a value and return a String as a key?

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

    public void setBankKey(String bankKey)
    {
        this.bankKey = bankKey;
    }
}
