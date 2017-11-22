import java.io.Serializable;

class AuctionItem implements Serializable
{
    //Used to indicate which house the item is stored at?
    private final int auctionHouseId;

    private final String name;

    //Used to id the item in question
    private final int itemId;

    private final int minimumBid;

    private volatile int currentBid;

    private final Integer auctionKey;

    private Integer highestBidderKey;

    private int previousBid;

    private Integer previousBidderKey;


    AuctionItem(int houseId, String itemName, int id, int minimumBid, int key)
    {
        this.auctionHouseId = houseId;

        this.name = itemName;

        this.itemId = id;

        this.minimumBid = minimumBid;

        this.auctionKey = key;

        this.currentBid = 0;
    }

    int getAuctionHouseId()
    {
        return this.auctionHouseId;
    }

    int getItemId()
    {
        return this.itemId;
    }

    int getMinimumBid()
    {
        return  this.minimumBid;
    }

    int getCurrentBid()
    {
        return this.currentBid;
    }

    synchronized void setCurrentBid(int amount, int auctionKey)
    {
        if(auctionKey == this.auctionKey)
        {
            this.currentBid = amount;
        }
    }

    String getName()
    {
        return this.name;
    }



}
