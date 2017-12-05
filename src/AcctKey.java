import java.io.Serializable;

/**
 * Adam Spanswick
 * The AcctKey class stores the agent's unique account key, unique account number and their balance. It is used in communication
 * between the Agent and the Bank.
 */
public class AcctKey implements Serializable
{
  private Integer acctKey;
  private Integer acctNum;
  private Double balance;

  /**
   * Constructor sets the acctKey to Key, the accountNum to accountNumber and the balance to balance
   * @param key is the account key
   * @param accountNumber is the account number
   * @param balance is the balance
   */
  public AcctKey(Integer key, Integer accountNumber, Double balance)
  {
    this.acctNum = accountNumber;
    this.acctKey = key;
    this.balance = balance;
  }

  /**
   * Method returns the acctKey
   * @return the accountKey
   */
  public Integer getKey()
  {
    return acctKey;
  }

  /**
   * Method returns the acctNum
   * @return the account number
   */
  public Integer getAccountNumber() { return acctNum; }

  /**
   * Method returns the balance
   * @return the account balance
   */
  public Double getBalance() { return balance; }
}
