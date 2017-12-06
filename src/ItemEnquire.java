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
 * ItemEnquire is used by an Agent to check if an item is still being auction. An ItemEnquire object has the variable
 * serialNumber. This is the serial number of the AuctionItem that the agent wants to enquire about. ItemEnquire has a
 * constructor that is used to set serialNumber and a getter for serialNumber.
 */
class ItemEnquire implements Serializable
{
    private final int serialNumber;

    /**
     * @param serialNumber - An int used to represent an items serial number.
     * ItemEnquire has no return value.
     * ItemEnquire is a constructor that is used to set the serialNumber field.
     */
    ItemEnquire(int serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    /**
     * @return getSerialNumber returns an int.
     * getSerialNumber is used to get the value stored in serialNumber.
     */
    int getSerialNumber()
    {
        return this.serialNumber;
    }
}
