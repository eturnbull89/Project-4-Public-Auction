import java.io.Serializable;

public class Transaction implements Serializable
{

    public Integer bankKey;
    public int amount;
    public int request;

    /**request tells the type of transaction this object is.
     * -1 is for placing a hold
     * 1 is for releasing a hold
     * 0 is for withdrawing money*/
    public Transaction(Integer key, int amount, int request)
    {
        this.amount = amount;
        this.request = request;
        bankKey = key;
    }

}
