import java.io.Serializable;

class AuctionItem implements Serializable
{
    private final int auctionHouseId;

    private final int itemId;

    private final int minimumBid;

    private int currentBid;

    private final int auctionKey;

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

    void setCurrentBid(int amount)
    {
        this.currentBid = amount;
    }

}
