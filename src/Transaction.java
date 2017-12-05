import java.io.Serializable;

class Transaction implements Serializable
{

    private Integer bankKey;
    private int amount;
    private int request;

    /**request tells the type of transaction this object is.
     * -1 is for placing a hold
     * 1 is for releasing a hold
     * 0 is for withdrawing money*/
    Transaction(Integer key, int amount, int request)
    {
        this.amount = amount;
        this.request = request;
        bankKey = key;
    }

    Integer getBankKey()
    {
        return this.bankKey;
    }

    int getAmount()
    {
        return this.amount;
    }

    int getRequest()
    {
        return this.request;
    }

}
