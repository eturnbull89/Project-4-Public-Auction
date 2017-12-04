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
 * itemEnquire is used by an Agent to check if they have won the bid on an item. An itemEnquire object has the variable
 * serialNumber. This is the serial number of the AuctionItem that the agent wants to enquire about. itemEnquire has a
 * constructor that is used to set serialNumber and a getter for serialNumber.
 */
class itemEnquire implements Serializable
{
    private final int serialNumber;

    /**
     * @param serialNumber - An int used to represent an items serial number.
     * itemEnquire has no return value.
     * itemEnquire is a constructor that is used to set the serialNumber field.
     */
    itemEnquire(int serialNumber)
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
