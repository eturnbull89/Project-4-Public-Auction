import java.io.Serializable;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Eric Turnbull  | eturnbull@unm.edu
 *          Zach Fleharty  |
 *          Tristin Glunt  | tglunt@unm.edu
 *          Adam Spanswick |
 * =============================================
 *
 * Confirmation is used to confirm an auction houses registration with auction central. A confirmation has two fields,
 * publicId that is used to id an auctionHouse and auctionKey that is used by an auctionHouse to set the highest bid on
 * an AuctionItem. These two fields are set in Confirmations constructor. Each field also has its own getter.
 */
class Confirmation implements Serializable
{
    private final int publicId;

    private final Integer auctionKey;

    /**
     * @param id - An int used to represent an auction houses public id.
     * @param key - An Integer that represents the key assigned by auction central.
     * Confirmation is a constructor used to set a confirmations two fields, publicId and auctionKey. publicId is
     * an auction houses public id number. auctionKey is an Integer assigned by auction central that allows an auction
     * house to set bids on an auction item.
     */
    Confirmation(int id, int key)
    {
        this.publicId = id;

        this.auctionKey = key;
    }

    /**
     * @return getPublicId returns an int.
     * getPublicId is used to get the value stored in the publicId field.
     */
    int getPublicId()
    {
        return publicId;
    }

    /**
     * @return getAuctionKey returns an int.
     * getAuctionKey is used to get the value stored in the auctionKey field.
     */
    int getAuctionKey()
    {
        return auctionKey;
    }
}
