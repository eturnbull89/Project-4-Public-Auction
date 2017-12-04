import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  |
 *          Adam Spanswick |
 * =============================================
 *
 * Bid is used to pass information between an Agent and an AuctionHouse. A bid holds information regarding what item
 * an Agent wants to bid on, how much that agent wants to bid on an item, the agents bidKey that will be used by
 * AuctionCentral to place a hold on an agents account, and a bid status. Bid status is used by an AuctionHouse to
 * inform an agent about their bid. If the status is acceptance it means that the agents bid was successfully placed and
 * the funds have been placed on hold. A status of reject means that an agents bid amount was not high enough to replace
 * the current highest bid. Passed indicates that the agent is already the highest bidder and was not allowed to place
 * another bid. Over indicates that the bidding for an item has ended.
 */

class Bid implements Serializable
{
    private final AuctionItem itemBiddingOn;

    private final Integer agentBidKey;

    private int bidAmount;

    private String bidStatus = "NotOver";

    /**
     * @param agentBidKey - An Integer value used to represent an agents bid key.
     * @param itemBiddingOn - The AuctionItem that a agent wants to bid on.
     * Bid is a constructor that is used to set a bid itemBiddingOn field to the AuctionItem passed to it and the
     * agentBidKey field to the key passed.
     */
    Bid(Integer agentBidKey, AuctionItem itemBiddingOn)
    {
        this.itemBiddingOn = itemBiddingOn;
        this.agentBidKey = agentBidKey;
    }

    /**
     * @return an int.
     * getBidAmount returns the value of bidAmount stored in a bid.
     */
    int getBidAmount()
    {
        return bidAmount;
    }

    /**
     * @param bidAmount - The amount an agent wants to bid on an item.
     * setBidAmount has no return value.
     * setBidAmount is used to set the bidAmount field in a bid to the value that is passed.
     */
    void setBidAmount(int bidAmount)
    {
        this.bidAmount = bidAmount;
    }

    /**
     * @return getBidStatus returns a string.
     * getBidStatus is used for getting the status of a bid, i.e. bidStatus.
     */
    String getBidStatus()
    {
        return bidStatus;
    }

    /**
     * @param bidStatus - String that indicates how an agents bid was handled.
     * setBidStatus has no return value.
     * setBidStatus is used to set the bidStatus field in a bid. This is done on the auction houses end to indicate how
     * an agents bid was handled.
     */
    void setBidStatus(String bidStatus)
    {
        this.bidStatus = bidStatus;
    }

    /**
     * @return getAgentBidKey returns an Integer.
     * getAgentBidKey is used to get the bid key that agent stores in a bid.
     */
    Integer getAgentBidKey()
    {
        return agentBidKey;
    }

    /**
     * @return getItemBiddingOn returns an AuctionItem.
     * getItemBiddingOn returns the item an Agent wants to bid on.
     */
    AuctionItem getItemBiddingOn()
    {
        return itemBiddingOn;
    }
}
