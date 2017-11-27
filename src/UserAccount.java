/**
 * Created by adam on 11/24/17.
 */
public class UserAccount
{
  private String accountName;

  //********************************************************************************************************************
  //Parameters:
  //  1. String name is the name of the account holder
  //
  //Constructor sets the accountName to name
  //********************************************************************************************************************
  public UserAccount(String name)
  {
    accountName = name;
  }

  //********************************************************************************************************************
  //Parameters: none
  //
  //Method returns the accountName string
  //********************************************************************************************************************
  public String getAccountName()
  {
    return accountName;
  }
}
