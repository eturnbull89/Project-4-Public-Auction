import java.io.Serializable;
import java.util.*;

/**
 * Adam Spanswick
 * The Bank class sets up and keeps a record of all accounts that have been created by users. To use this class call the
 * constructor with the name of the account holder and Bank will create a new unique account.
 */
public class Bank implements Serializable
{

  private HashMap<Integer, BankAccount> accountList = new HashMap<>();
  private HashMap<String, Integer> bankKeys = new HashMap<>();
  private ArrayList<Integer> keys = new ArrayList<>();
  private ArrayList<Integer> accountNumbers = new ArrayList<>();

  /**
   * createNewAccount: setups up a new account for a agent by calling newAccount with the name provided.
   * @param name is the name of the account holder
   */
  void createNewAccount(String name)
  {
    newAccount(name);
  }

  /**
   * getAccount: returns the agents account.
   * @param key is the agents ket to lookup their account
   * @return the account associated with the given key
   */
  BankAccount getAccount(Integer key)
  {
    return accountList.get(key);
  }

  /**
   * getBankKey: returns the bank key from the agents name
   * @param name the agents name
   * @return the bank key
   */
  Integer getBankKey(String name)
  {
    return bankKeys.get(name);
  }

  /**
   * newAccount: creates a new account for a agent by creating a new BankAccount object which generates a bank key and
   * account number. The new account is then added to the Bank's account list and the bank key is put into a map with the
   * agents name.
   * @param name - Name of the agent.
   */
  private void newAccount(String name)
  {
    BankAccount newAcct = new BankAccount();

    accountList.put(newAcct.getBankKey(), newAcct);
    bankKeys.put(name, newAcct.getBankKey());
    keys.add(newAcct.getBankKey());
    accountNumbers.add(newAcct.getAccountNumber());

    //Check for duplicates
    checkForDuplicateAccountnumbers(newAcct);
    checkForDuplicateBankKeys(newAcct);
  }

  /**
   * checkForDuplicateAccountnumbers: Checks for duplicate account numbers every time a new account is created. Creates a
   * set that will hold duplicate account numbers if there are any and adds all the account numbers to that set. Then it checks
   * if the size of the set is is less than all the account numbers there is a duplicate and calls a method to generate a new
   * account number.
   */
  private void checkForDuplicateAccountnumbers(BankAccount accountTocheck)
  {
    Set<Integer> duplicates = new HashSet<>();
    duplicates.addAll(accountNumbers);

    if(duplicates.size() < accountNumbers.size())
    {
      handleDuplicateAccountNumbers(accountTocheck);
    }
  }

  /**
   * handleDuplicateAccountNumbers: Creates a new account number for the account with a duplicate account number. Then
   * checks again
   */
  private void handleDuplicateAccountNumbers(BankAccount accountToCheck)
  {
    //Change account number
    accountToCheck.newAccountNumber();

    //Recheck
    checkForDuplicateAccountnumbers(accountToCheck);
  }

  /**
   * checkForDuplicateBankKeys: Checks for duplicate bank keys every time a new account is created. Creates a
   * set that will hold duplicate bank keys if there are any and adds all the bank keys to that set. Then it checks
   * if the size of the set is is less than all the bank keys there is a duplicate and calls a method to generate a new
   * bank key.
   */
  private void checkForDuplicateBankKeys(BankAccount accountTocheck)
  {
    Set<Integer> duplicates = new HashSet<>();
    duplicates.addAll(keys);

    if(duplicates.size() < keys.size())
    {
      handleDuplicateBankKeys(accountTocheck);
    }
  }

  /**
   * handleDuplicateBankKeys: Creates a new bank key for the account with a duplicate bank key. Then checks again.
   */
  private void handleDuplicateBankKeys(BankAccount accountToCheck)
  {
    //Change bank key
    accountToCheck.newBankKey();

    //Recheck
    checkForDuplicateBankKeys(accountToCheck);

  }
}