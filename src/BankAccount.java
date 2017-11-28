import java.io.Serializable;
import java.util.Random;

/**
 * Adam Spanswick
 * BankAccount extends account so it has all of the getters, but also has setters for bankKey and holdBalance.
 * This version of the account will be used by the Bank.
 */
public class BankAccount extends Account implements Serializable
{
    private Double balance = 100d;
    private Integer bankKey;
    private String name = null;
    private int fundsInHold = 0;
    private Integer accountNumber;

    /**
     * BankAccount: constructor setsup a new account by creating a 9 digit account number and 3 digit bank key.
     */
    public BankAccount()
    {
        generateAccountNumber();
        generatebankKey();
    }

    /**
     * getBankKey: returns the bankKey
     * @return
     */
    public Integer getBankKey()
    {
        return bankKey;
    }

    /**
     * getAccountNumber: returns the account number
     * @return
     */
    public Integer getAccountNumber()
    {
        return accountNumber;
    }

    /**
     * getBalance: returns the current balance
     * @return
     */
    public Double getBalance()
    {
        return balance;
    }

    /**
     * updateBalance: updates the balance based off a string passed in. If the string is "withdraw" it deducts the amount
     * from the account's balance. If the string is "deposit" it adds the amount to the account's balance. Anything else
     * is not a account function.
     * @param update how to update the account
     * @param amount the number to be added or subtracted from the balance
     */
    public void updateBalance(String update, int amount)
    {
        if(update.toLowerCase().equals("withdraw"))
        {
            this.balance -= amount;
        }
        else if(update.toLowerCase().equals("deposit"))
        {
            this.balance += amount;
        }
        else
        {
            System.out.println("Improper account action: enter deposit for a deposit or withdraw to withdraw");
        }
    }

    /**
     * newAccountnumber: Generates a new account number. Used when there is a duplicate account number
     */
    public void newAccountNumber()
    {
        generateAccountNumber();
    }

    /**
     * newBankKey: Generates a new bank key. Used when there is a duplicate bank key
     */
    public void newBankKey()
    {
        generatebankKey();
    }
    /**
     * setHoldBalance: Takes in a amount and sets the account's fundsInHold to that amount and deducts that amount from
     * the account's balance.
     * @param amount
     * @return
     */
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

    /**
     * getHoldBalance: returns the amount in fundsInHold
     * @return
     */
    public int getHoldBalance()
    {
        return fundsInHold;
    }

    /**
     * deductHoldAmount: Checks if the current balance of fundsInHold is greater or equal to the amount passed in. If it is
     * it deducts the amount from fundsInHold.
     * @param amount
     * @return
     */
    public boolean deductHoldAmount(int amount)
    {
        if(fundsInHold >= amount)
        {
            fundsInHold -= amount;
            return true;
        }
        return false;
    }

    /**
     * clearHold: Takes a specific amount and deducts it from the fundsInHold and puts that amount back into the account's
     * balance by calling updateBalance
     * @param amount
     * @return
     */
    public boolean clearHold(int amount)
    {
        updateBalance("deposit", amount);
        this.fundsInHold -= amount;

        return true;
    }

    /**
     * inquiry: Prints the available balance and fundsInHold if there are fundsInHold
     * @return
     */
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

    /**
     * generateBankKey: Creates a random 3 digit number and sets the account's bankKey
     */
    private void generatebankKey()
    {
        Random rand = new Random();
        Integer n = rand.nextInt(999) + 100;

        this.bankKey = n;
    }

    /**
     * generateAccountNumber: Creates a random 9 digit number and sets this as the account's account number
     */
    private void generateAccountNumber()
    {
        Random rand = new Random();
        Integer n = rand.nextInt(999999999) + 100000000;

        this.accountNumber = n;
    }
}
