import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  |
 *          Adam Spanswick |
 * =============================================
 */

public class Bid implements Serializable
{
    private final AuctionItem itemBiddingOn;
    private final Integer agentBidKey;
    private int bidAmount;
    private String bidStatus = "NotOver";

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
