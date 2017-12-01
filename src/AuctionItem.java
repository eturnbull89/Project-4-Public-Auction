import java.io.Serializable;
import java.util.Random;

class AuctionItem implements Serializable
{
    //Used to indicate which house the item is stored at?
    private final int auctionHouseId;

    //Name of the given auction house, is passed to auction house as a command line argument.
    private final String name;

    //Used to id the item in question, is the items index in its list
    private final int itemId;

    private final int itemSerialNum;

    //The initial minimum bid amount, this is initially given in the sale list.
    private final int minimumBid;

    //Field to keep track of an items current bid amount, is volatile to check if current bid is updated by
    //between agent bids.
    private volatile int currentBid;

    //Field to hold an auctionHouses auction key, used to update an items current bid amount.
    private final Integer auctionKey;

    //Field that will hold the bidderKey of the agent with the highest bid.
    private Integer highestBidderKey;

    //Field that will hold the previous highest bid.
    private int previousBid;

    //Field that will hold the previous highest bidder key.
    private Integer previousBidderKey;

    //***********************************
    //int houseId - an int that is used to represent the auction houses id.
    //String itemName - a String that identifies an item.
    //int id - an items id value, is the value of the items array index for look up.
    //int minimumBid - the value that an items minimum bid should be set to.
    //Integer key - the key that auction key will be set to.
    //AuctionItem is a constructor used to set the values of an auction items variables. It sets an auctionHouseId to
    //the value of houseId passed to it, the name field to the itemName string passed to it, etc. It also sets the
    //value of current bid to zero to indicate no bids are currently on it.
    //***********************************
    AuctionItem(int houseId, String itemName, int id, int minimumBid, Integer key, int serial)
    {
        //Set the auctionHouseId variable.
        this.auctionHouseId = houseId;

        //Set the items name field
        this.name = itemName;

        //Set the item id field.
        this.itemId = id;

        //Set the items minimum bid amount.
        this.minimumBid = minimumBid;

        //Set the items auction key variable
        this.auctionKey = key;

        //Set the current bid amount to zero.
        this.currentBid = 0;

        this.itemSerialNum = serial;
    }

    //***********************************
    //getAuctionHouseId returns an int.
    //getAuctionHouseId returns the auctionHouseId that this item is tied to.
    //***********************************
    int getAuctionHouseId()
    {
        return this.auctionHouseId;
    }

    //***********************************
    //getItemId returns an int.
    //getItemId returns this items id.
    //***********************************
    int getItemId()
    {
        return this.itemId;
    }

    //***********************************
    //getMinimumBid returns an int.
    //getMinimumBid returns an items minimum bid amount.
    //***********************************
    int getMinimumBid()
    {
        return  this.minimumBid;
    }

    //***********************************
    //getCurrentBid returns an int.
    //getCurrentBid returns the current bid amount of an item.
    //***********************************
    int getCurrentBid()
    {
        return this.currentBid;
    }

    //***********************************
    //int amount - Value that current bid should be set to.
    //Integer auctionKey - An auction houses auction key.
    //setCurrentBid has no return value.
    //setCurrentBid is used to set an items current bid amount. It starts by checking that the auction key passed to it
    //is the same auction key stored in the item. If it is it sets the current bid field to the amount passed to it. It
    //is synchronized to prevent multiple threads from changing the value at once.
    //***********************************
    void setCurrentBid(int amount, Integer auctionKey)
    {
        //Check if the auction key passed is the same as the auction key the item was created with.
        if(auctionKey.equals(this.auctionKey))
        {
            //Set the current bid amount to the amount passed.
            this.currentBid = amount;
        }
    }

    //***********************************
    //getName returns a string.
    //getName returns an items name field.
    //***********************************
    String getName()
    {
        return this.name;
    }

    //***********************************
    //Integer key - The key that highestBidKey will be set to.
    //setHighestBidKey has no return value.
    //setHighestBidKey sets the value of highestBidderKey to the key value passed to it.
    //***********************************
    void setHighestBidKey(Integer key)
    {
        this.highestBidderKey = key;
    }

    //***********************************
    //getHighestBidderKey returns an Integer.
    //getHighestBidderKey returns the value of highestBidderKey
    //***********************************
    Integer getHighestBidderKey()
    {
        return this.highestBidderKey;
    }

    //***********************************
    //Integer key - The key value that previousBidderKey will be set to.
    //setPreviousBidderKey has no return value.
    //setPreviousBidderKey is used to set the value of previousBidderKey to the value passed to it.
    //***********************************
    void setPreviousBidderKey(Integer key)
    {
        this.previousBidderKey = key;
    }

    //***********************************
    //getPreviousBidderKey returns an Integer
    //getPreviousBidderKey is used to get the value of previousBidderKey.
    //***********************************
    Integer getPreviousBidderKey()
    {
        return this.previousBidderKey;
    }

    //***********************************
    //int amount - An int that previousBid will be set to.
    //setPreviousBid has no return value.
    //setPreviousBid is used to the value of previousBid to the amount passed to it.
    //***********************************
    void setPreviousBid(int amount)
    {
        this.previousBid = amount;
    }

    //***********************************
    //getPreviousBid returns an int
    //getPreviousBid is used to get the value of previousBid.
    //***********************************
    int getPreviousBid()
    {
        return this.previousBid;
    }

    int getItemSerialNum()
    {
        return this.itemSerialNum;
    }
}
