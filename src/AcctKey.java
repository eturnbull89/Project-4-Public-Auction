import java.io.Serializable;

/**
 * Created by adam on 11/24/17.
 */
public class AcctKey implements Serializable
{
  private Integer acctKey;
  private Integer acctNum;

  //********************************************************************************************************************
  //Parameters:
  //  1. Integer key is the unique account key
  //
  //Constructor sets the acctKey to Key
  //********************************************************************************************************************
  public AcctKey(Integer key, Integer accountNumber)
  {
    this.acctNum = accountNumber;
    this.acctKey = key;
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
}
