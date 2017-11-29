package AuctionCentral.SerializedObjects;

import java.io.Serializable;

public class AuctionTransaction implements Serializable
{
    public Integer bidKey;
    public int amount;
    public int request;

    /**request tells the type of transaction this object is.
     * -1 is for placing a hold
     * 1 is for releasing a hold
     * 0 is for withdrawing money*/
    AuctionTransaction(Integer key, int amount, int request)
    {
        this.bidKey = key;
        this.amount = amount;
        this.request = request;
    }
}
