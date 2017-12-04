import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Zach Fleharty  |
 *          Eric Turnbull  | eturnbull@unm.edu
 *          Tristin Glunt  | tglunt@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * AuctionTransaction is used to request holds and withdrawals on an agents account by an auction house. The bidKey
 * field is used to store an Agents bidKey that auctionCentral keeps track of. amount is used to indicate the amount
 * of money that needs to be placed into a hold, released from a hold, or withdrawn. request is used to indicate
 * what kind of transaction is taking place. Each field has its own getter.
 */
class AuctionTransaction implements Serializable
{
    private final Integer bidKey;

    private final int amount;

    private final int request;

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

    /**
     * @return getBidKey returns an integer.
     * getBidKey is used to get the value stored in bidKey.
     */
    Integer getBidKey()
    {
        return this.bidKey;
    }

    /**
     * @return getAmount returns an int.
     * getAmount is used to get the value stored in amount.
     */
    int getAmount()
    {
        return this.amount;
    }

    /**
     * @return getRequest returns an int.
     * getRequest is used to return the value stored in request.
     */
    int getRequest()
    {
        return this.request;
    }
}
