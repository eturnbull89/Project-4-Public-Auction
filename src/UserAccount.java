import java.io.Serializable;

/**
 * Adam Spanswick
 * UserAccount class is used by a Agent to create a Bank account. The agent sets the name field and then sends the object
 * to the Bank where the Bank reads the name and sets up a new account for that agent.
 */
public class UserAccount implements Serializable
{
  private String accountName;

  /**
   * Constructor sets the accountName to name
   * @param name is the agent's name
   */
  public UserAccount(String name)
  {
    accountName = name;
  }

  /**
   * Method returns the accountName string
   * @return the string for the account name
   */
  public String getAccountName()
  {
    return accountName;
  }
}
