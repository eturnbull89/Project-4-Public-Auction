import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  | eturnbull@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * WinnerInquire is used by an Agent to check if they have won the bid on an item. A WinnerInquire object has the variable
 * auctionItem. This is the AuctionItem that the agent wants to enquire about. ItemEnquire has a
 * constructor that is used to set the auction item and the Agents bidding key.
 */

class WinnerInquire implements Serializable
{
    private final AuctionItem auctionItem;

    private final int biddingKey;

    /**
     * WinnerInquire: constructor
     * @param auctionItem item we want to know if we won or not
     * @param biddingKey bidding key for the agent creating the object
     */
    WinnerInquire(AuctionItem auctionItem, int biddingKey)
    {
        this.auctionItem = auctionItem;

        this.biddingKey = biddingKey;
    }


    int getBiddingKey()
    {
        return biddingKey;
    }

    AuctionItem getAuctionItem()
    {
        return auctionItem;
    }

}
