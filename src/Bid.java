/**
 * Created by tristin on 11/21/2017.
 */
public class Bid
{
    private final AuctionItem itemBiddingOn;
    private final Integer agentBidKey;
    private int bidAmount;
    private String bidStatus;

    public Bid(Integer agentBidKey, AuctionItem itemBiddingOn)
    {
        this.itemBiddingOn = itemBiddingOn;
        this.agentBidKey = agentBidKey;
    }

    public int getBidAmount()
    {
        return bidAmount;
    }

    public void setBidAmount(int bidAmount)
    {
        this.bidAmount = bidAmount;
    }

    public String getBidStatus()
    {
        return bidStatus;
    }

    public void setBidStatus(String bidStatus)
    {
        this.bidStatus = bidStatus;
    }

    public Integer getAgentBidKey()
    {
        return agentBidKey;
    }

    public AuctionItem getItemBiddingOn()
    {
        return itemBiddingOn;
    }
}
