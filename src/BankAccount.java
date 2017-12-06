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
    private int fundsInHold = 0;
    private Integer accountNumber;

    /**
     * BankAccount: constructor setsup a new account by creating a 9 digit account number and 3 digit bank key.
     */
    BankAccount()
    {
        generateAccountNumber();
        generatebankKey();
    }

    /**
     * getBankKey: returns the bankKey
     * @return getBankKey returns an Integer.
     */
    public Integer getBankKey()
    {
        return bankKey;
    }

    /**
     * getAccountNumber: returns the account number
     * @return getAccountNumber returns an Integer.
     */
    public Integer getAccountNumber()
    {
        return accountNumber;
    }

    /**
     * getBalance: returns the current balance
     * @return getBalance returns a Double
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
    private void updateBalance(String update, int amount)
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
    void newAccountNumber()
    {
        generateAccountNumber();
    }

    /**
     * newBankKey: Generates a new bank key. Used when there is a duplicate bank key
     */
    void newBankKey()
    {
        generatebankKey();
    }

    /**
     * setHoldBalance: Takes in a amount and sets the account's fundsInHold to that amount and deducts that amount from
     * the account's balance.
     * @param amount - Amount of money to be placed on hold.
     * @return setHoldBalance returns a boolean
     */
    boolean setHoldBalance(int amount)
    {
        if(balance >= amount)
        {
            this.fundsInHold += amount;

            updateBalance("withdraw", amount);
            return true;
        }

        return false;
    }

    /**
     * getHoldBalance: returns the amount in fundsInHold
     * @return getHoldBalance returns an int.
     */
    private int getHoldBalance()
    {
        return fundsInHold;
    }

    /**
     * deductHoldAmount: Checks if the current balance of fundsInHold is greater or equal to the amount passed in. If it is
     * it deducts the amount from fundsInHold.
     * @param amount - Amount of money to be deducted from account.
     * @return deductHoldAmount returns a boolean.
     */
    boolean deductHoldAmount(int amount)
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
     * @param amount - Amount of money to be released from a hold.
     * @return clearHold returns a boolean
     */
    boolean clearHold(int amount)
    {
        if(fundsInHold >= amount)
        {
            updateBalance("deposit", amount);
            this.fundsInHold -= amount;

            return true;
        }

        return false;
    }

    /**
     * inquiry: Prints the available balance and fundsInHold if there are fundsInHold
     * @return inquiry returns a String.
     */
    String inquiry()
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
