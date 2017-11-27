/**
 * Created by adam on 11/24/17.
 */
public class AcctKey
{
  private Integer acctKey;

  //********************************************************************************************************************
  //Parameters:
  //  1. Integer key is the unique account key
  //
  //Constructor sets the acctKey to Key
  //********************************************************************************************************************
  public AcctKey(Integer key)
  {
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
}
