import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Eric Turnbull  | eturnbull@unm.edu
 *          Zach Fleharty  |
 *          Tristin Glunt  | tglunt@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * AuctionItem is used to create items that will be bid on by Agents. Each AuctionItem has a number of variables that
 * are used a different points in the program. name is used to identify an item to an Agent when they are bidding.
 * itemSerialNum is a number that is used to make each item identifiable when searching for it. minimumBid is the value
 * set at the start that indicates the lowest amount an Agent can initially bid. highestBid is used to keep track of an
 * items highest bid amount, is volatile to check if highest bid is updated by between agent bids. auctionKey is used
 * to hold an auctionHouses auction key, used to update an items highest bid amount. highestBidderKey is used to hold
 * bidderKey of the agent with the highest bid. previousBid is used to hold the previous highest bid amount.
 * previousBidderKey is used to hold the previous highest bidder key. All but auctionKey have a getter to get the values
 * stored. highestBid, previousBid, highestBidderKey, previousBidderKey also have setters but to set the values in them
 * a key must be passed that matches the key given to the item at creation.
 */
class AuctionItem implements Serializable
{
    private final String name;

    private final int itemSerialNum;

    private final int minimumBid;

    private volatile int highestBid;

    private final Integer auctionKey;

    private Integer highestBidderKey;

    private int previousBid;

    private Integer previousBidderKey;

    /**
     * @param itemName - A String that identifies an item.
     * @param minimumBid  - The value that an items minimum bid should be set to.
     * @param key - The key that auction key will be set to.
     * @param serial - An items Id number.
     * AuctionItem is a constructor that is used to set an AuctionItems fields.
     */
    AuctionItem(String itemName, int minimumBid, Integer key, int serial)
    {
        this.name = itemName;

        this.minimumBid = minimumBid;

        this.auctionKey = key;

        this.highestBid = 0;

        this.itemSerialNum = serial;
    }

    /**
     * @return getMinimumBid returns an int.
     * getMinimumBid returns an items minimum bid amount.
     */
    int getMinimumBid()
    {
        return  this.minimumBid;
    }

    /**
     * @return getHighestBid returns an int.
     * getHighestBid returns the current bid amount of an item.
     */
    int getHighestBid()
    {
        return this.highestBid;
    }

    /**
     * @param amount - Value that current bid should be set to.
     * @param auctionKey - An auction houses auction key.
     * setHighestBid is used to set an items highest bid amount. It starts by checking that the auction key passed to it
     * is the same auction key stored in the item. If it is, it sets the highest bid field to the amount passed to it.
     */
    void setHighestBid(int amount, Integer auctionKey)
    {
        if(auctionKey.equals(this.auctionKey))
        {
            this.highestBid = amount;
        }
    }

    /**
     * @return getName returns a string.
     * getName returns an items name field.
     */
    String getName()
    {
        return this.name;
    }

    /**
     * @param key - The key that highestBidKey will be set to.
     * @param auctionKey - An auction houses auction key.
     * setHighestBidKey sets the value of highestBidderKey to the key value passed to it. It checks if the auction key
     * stored in the item is the same as the key that is passed to it. If so it then updates the highestBidderKey field
     * to the key passed to it.
     */
    void setHighestBidKey(Integer key, Integer auctionKey)
    {
        if(auctionKey.equals(this.auctionKey))
        {
            this.highestBidderKey = key;
        }
    }

    /**
     * @return getHighestBidderKey returns an Integer.
     * getHighestBidderKey returns the value of highestBidderKey
     */
    Integer getHighestBidderKey()
    {
        return this.highestBidderKey;
    }

    /**
     * @param key - The key value that previousBidderKey will be set to.
     * @param auctionKey - An auction houses auction key.
     * setPreviousBidderKey is used to set the value of previousBidderKey to the value passed to it. It checks that
     * the auctionKey passed is the same as the auction key stored in the item. If so it changes the value of
     * previousBidderKey to value passed.
     */
    void setPreviousBidderKey(Integer key, Integer auctionKey)
    {
        if(auctionKey.equals(this.auctionKey))
        {
            this.previousBidderKey = key;
        }
    }

    /**
     * @return getPreviousBidderKey returns an Integer
     * getPreviousBidderKey is used to get the value of previousBidderKey.
     */
    Integer getPreviousBidderKey()
    {
        return this.previousBidderKey;
    }

    /**
     * @param amount - An int that previousBid will be set to.
     * @param auctionKey - An auction houses auction key.
     * setPreviousBid is used to the value of previousBid to the amount passed to it.
     */
    void setPreviousBid(int amount, Integer auctionKey)
    {
        if(auctionKey.equals(this.auctionKey))
        {
            this.previousBid = amount;
        }
    }

    /**
     * @return getPreviousBid returns an int
     * getPreviousBid is used to get the value of previousBid.
     */
    int getPreviousBid()
    {
        return this.previousBid;
    }

    /**
     * @return getItemSerialNum returns an int.
     * getItemSerialNum is used to get the value store in itemSerialNum.
     */
    int getItemSerialNum()
    {
        return this.itemSerialNum;
    }
}
