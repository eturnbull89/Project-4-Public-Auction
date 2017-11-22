import java.io.Serializable;

class AuctionItem implements Serializable
{
    private final int auctionHouseId;

    private final int itemId;

    private final int minimumBid;

    private volatile int currentBid;

    private final Integer auctionKey;

    private Integer highestBidderKey;

    private int previousBid;

    private Integer previousBidderKey;


    AuctionItem(int houseId, int id, int minimumBid, int key)
    {
        this.auctionHouseId = houseId;

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



}
