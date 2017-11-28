package AuctionCentral.SerializedObjects;

import java.io.Serializable;

/**
 * Created by tristin on 11/21/2017.
 */
public class Bid implements Serializable
{
    private final AuctionItem itemBiddingOn;
    private final Integer agentBidKey;
    private int bidAmount;
    private String bidStatus;

    //added in so the auction central can tell the type of request the bid is
    //This might have been the intention of bidStatus but I wasn't sure
    //whether the money needs to be placed on hold or withdrawn
    //possibly use to check other cases if others may exist between the auction central and house
    /**request tells the type of transaction this object is.
     * -1 is for placing a hold
     * 0 is for withdrawing money*/
    private int Request;

    private Integer oldAgentBidKey = null;
    private Integer oldAgentBidAmount = null;

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

    public int getRequest() {
        return Request;
    }

    public void setRequest(int request) {
        Request = request;
    }

    public Integer getOldAgentBidAmount() {
        return oldAgentBidAmount;
    }

    public void setOldAgentBidAmount(Integer oldAgentBidAmount) {
        this.oldAgentBidAmount = oldAgentBidAmount;
    }

    public Integer getOldAgentBidKey() {
        return oldAgentBidKey;
    }

    public void setOldAgentBidKey(Integer oldAgentBidKey) {
        this.oldAgentBidKey = oldAgentBidKey;
    }
}
