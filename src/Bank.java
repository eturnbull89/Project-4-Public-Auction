import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

//********************************************************************************************************************
//Adam Spanswick
//
//The Bank class sets up and keeps a record of all accounts that have been created by users. To use this class call the
//constructor with the name of the account holder and Bank will create a new unique account.
//********************************************************************************************************************
public class Bank implements Serializable
{

  //********************************************************************************************************************
//Adam Spanswick
//
//The Bank class sets up and keeps a record of all accounts that have been created by users. To use this class call the
//constructor with the name of the account holder and Bank will create a new unique account.
//********************************************************************************************************************
  private HashMap<Integer, BankAccount> accountList = new HashMap<>();
  private HashMap<String, Integer> bankKeys = new HashMap<>();
  private ArrayList<Account> printList = new ArrayList<>();

  public void createNewAccount(String name)
  {
    newAccount(name);
  }

  //********************************************************************************************************************
  //Parameters:
  //  1. Integer key is the users key for their acount
  //
  //Method returns the Account that corresponds to the key given
  //********************************************************************************************************************
  public BankAccount getAccount(Integer key)
  {
    return accountList.get(key);
  }

  //********************************************************************************************************************
  //Parameters:
  //  1. int amouunt is the amount that will be deducted or put on hold
  //  2. Integer key looks up the account for the amount to be applied to
  //
  //Method retrieves the correct account and puts the amount passed in "on hold" in the user account then checks if the
  //request is for winning a auction, if it is it deducts the amount from the account balance. If it is not a auction won
  //then it clears the amount from hte funds in hold.
  //********************************************************************************************************************
  public void processRequest(int amount, Integer key)
  {
    BankAccount temp = accountList.get(key);

    temp.setHoldBalance(amount);

    if (wonAuction())
    {
      //Transfer fundsinHold to auction house
      temp.deductHoldAmount(amount);
    } else
    {
      temp.clearHold();
    }
  }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method returns true if a auction is won flase if not.
  //********************************************************************************************************************
  public boolean wonAuction()
  {
    return true;
  }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method prints the name and account numbers of all the accounts in the bank
  //********************************************************************************************************************
//  public void printAcctList()
//  {
//    for (Account acct : printList)
//    {
//      System.out.println("Name: " + acct.getName() + ", Account Number: " + acct.getAccountNumber());
//    }
//  }

  public Integer getBankKey(String name)
  {
    return bankKeys.get(name);
  }

  //********************************************************************************************************************
  //Parameters:
  //  1. String name is the name of the account holder
  //
  //Method creates a new account and adds it to the list of accounts
  //********************************************************************************************************************
  private void newAccount(String name)
  {
    BankAccount newAcct = new BankAccount();

    accountList.put(newAcct.getBankKey(), newAcct);
    bankKeys.put(name, newAcct.getBankKey());
  }
}


