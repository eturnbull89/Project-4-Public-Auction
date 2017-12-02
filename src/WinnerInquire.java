/**
 * Created by tristin on 12/1/2017.
 */
public class WinnerInquire
{
    private final AuctionItem auctionItem;

    private final int biddingKey;

    public WinnerInquire(AuctionItem auctionItem, int biddingKey)
    {
        this.auctionItem = auctionItem;

        this.biddingKey = biddingKey;
    }


    public int getBiddingKey()
    {
        return biddingKey;
    }

    public AuctionItem getAuctionItem()
    {
        return auctionItem;
    }

}
