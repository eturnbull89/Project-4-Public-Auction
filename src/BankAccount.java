import java.io.Serializable;
import java.util.Random;

/**
 * BankAccount extends account so it has all of the getters, but also has setters for the account now.
 * This version of the account will be used by the Bank.
 */
public class BankAccount extends Account implements Serializable
{
    private Double balance = 100d;
    private Integer bankKey;
    private String name = null;
    private int fundsInHold = 0;
    private Integer accountNumber;

    public BankAccount()
    {
        generateAccountNumber();
        generatebankKey();
    }

    public void setAccountNumber(Integer accountNumber)
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

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns a Integer object that is the unique bank key
    //********************************************************************************************************************
    public Integer getBankKey()
    {
        return bankKey;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns a Integer object that is the account number.
    //********************************************************************************************************************
    public Integer getAccountNumber()
    {
        return accountNumber;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns a string that is the account holders name
    //********************************************************************************************************************
    public String getName()
    {
        return name;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns a int that is the current account balance.
    //********************************************************************************************************************
    public Double getBalance()
    {
        return balance;
    }

    //********************************************************************************************************************
    //Parameters:
    //  1. String update- is either withdraw or deposit
    //  2. int newBalance- is what will be added or decucted from the account balance
    //
    //Method returns void
    //Method checks if the update string is withdraw or deposit. If it is withdraw it deducts the newBalance from the
    //current balance. If it is deposit it adds the newBalance to the current balance
    //********************************************************************************************************************
    public void updateBalance(String update, int newBalance)
    {
        if(update.toLowerCase().equals("withdraw"))
        {
            this.balance -= newBalance;
        }
        else if(update.toLowerCase().equals("deposit"))
        {
            this.balance += newBalance;
        }
        else
        {
            System.out.println("Improper account action: enter deposit for a deposit or withdraw to withdraw");
        }
    }

    //********************************************************************************************************************
    //Parameters:
    //  1. int amount- the amount of the account balance that will be placed in hold
    //
    //Method returns void
    //Method sets the amount bidded to fundsInHold and updates the current account balance.
    //********************************************************************************************************************
    public boolean setHoldBalance(int amount)
    {
        if(balance >= amount)
        {
            this.fundsInHold = amount;

            updateBalance("withdraw", amount);
            return true;
        }

        return false;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns a int that is the amount in hold.
    //********************************************************************************************************************
    public int getHoldBalance()
    {
        return fundsInHold;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns void
    //Method deducts the amount in hold from the account balaance. Represents when a auction is won.
    //********************************************************************************************************************
    public boolean deductHoldAmount(int amount)
    {
        if(fundsInHold >= amount)
        {
            fundsInHold -= amount;
            return true;
        }
        return false;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns void
    //Method adds the funds in hold back into the account balance and resets funds in hold to 0.
    //
    //
    //Change to specific amount
    //********************************************************************************************************************
    public boolean clearHold()
    {
        updateBalance("deposit", fundsInHold);
        this.fundsInHold = 0;

        return true;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns void
    //Method checks if there are funds in hold, if there are it prints the available balance and the funds in hold. If not
    //it prints only the available balance.
    //********************************************************************************************************************
    public String inquiry()
    {
        String messgae = "";

        if(fundsInHold != 0)
        {
            messgae += "Available Balance: " + getBalance() +"\n";
            messgae += "Funds in hold: " + getHoldBalance();
        }
        else
        {
            messgae += "Available Balance: " + getBalance();
        }
        return messgae;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method sets the bank key to a random 3 digit number
    //Need to add a check so no duplicated can be generated
    //********************************************************************************************************************
    private void generatebankKey()
    {
        Random rand = new Random();
        Integer n = rand.nextInt(999) + 100;

        this.bankKey = n;
    }

    //********************************************************************************************************************
    //Parameters: none
    //
    //Method returns void
    //Method sets the account number to a random 9 digit number.
    //Need to add a check so there are no duplicate account numbers
    //********************************************************************************************************************
    private void generateAccountNumber()
    {
        Random rand = new Random();
        Integer n = rand.nextInt(999999999) + 100000000;

        this.accountNumber = n;
    }
}
