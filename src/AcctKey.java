import java.io.Serializable;

/**
 * Created by adam on 11/24/17.
 */
public class AcctKey implements Serializable
{
  private Integer acctKey;
  private Integer acctNum;
  private Double balance;

  //********************************************************************************************************************
  //Parameters:
  //  1. Integer key is the unique account key
  //
  //Constructor sets the acctKey to Key
  //********************************************************************************************************************
  public AcctKey(Integer key, Integer accountNumber, Double balance)
  {
    this.acctNum = accountNumber;
    this.acctKey = key;
    this.balance = balance;
  }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method returns the acctKey
  //********************************************************************************************************************
  public Integer getKey()
  {
    return acctKey;
  }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method returns the acctNum
  //********************************************************************************************************************
  public Integer getAccountNumber() { return acctNum; }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method returns the balance
  //********************************************************************************************************************
  public Double getBalance() { return balance; }
}
