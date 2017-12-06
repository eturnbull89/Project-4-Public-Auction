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
 * Transaction is used to request a hold, release a hold, or withdraw money on an agents bank account. It has three
 * variables with their own getters, each variable is set in Transactions constructor. bankKey is an Integer that is
 * used by bank to access an agents account. amount is the value that is going to be put on hold, released from its
 * current hold, or to be withdrawn from the agents account. request is used to indicate what kind of transaction is
 * taking place.
 */
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

    /**
     * @return getBankKey returns an Integer.
     * getBankKey is used to value stored in the bankKey field.
     */
    Integer getBankKey()
    {
        return this.bankKey;
    }

    /**
     * @return getAmount returns an int.
     * getAmount returns the value stored in amount.
     */
    int getAmount()
    {
        return this.amount;
    }

    /**
     * @return getRequest returns an int.
     * getRequest returns the value stored in request.
     */
    int getRequest()
    {
        return this.request;
    }

}
