import java.io.Serializable;

/**
 * Tristin Glunt
 * WinnerInquire is used by Agent to figure out if they won the item.
 */
public class WinnerInquire implements Serializable
{
  private final AuctionItem auctionItem;

  private final int biddingKey;

  /**
   * Constructor sets the auctionItem to auctionItem and sets biddingKey to biddingKEy
   * @param auctionItem the item passed in
   * @param biddingKey the bidding key for the agent
   */
  public WinnerInquire(AuctionItem auctionItem, int biddingKey)
  {
    this.auctionItem = auctionItem;

    this.biddingKey = biddingKey;
  }

  /**
   * Returns the bidding key
   * @return the bidding key
   */
  public int getBiddingKey()
  {
    return biddingKey;
  }

  /**
   * Returns the auction item
   * @return the auction item
   */
  public AuctionItem getAuctionItem()
  {
    return auctionItem;
  }

}
