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
 * Registration is used to register an auction house with auction central. It has three fields that are sent in its
 * constructor. houseName is a string used to display the name of an auction house, for example the list that is
 * currently created contains items related to art so the name usually given is ArtHouse. houseSocket is used to
 * indicate what socket the auction house was created at. houseHost is a string used to indicate where an auction
 * house is hosting from, i.e. its ip address. Each of these fields has its own getter.
 */
class Registration implements Serializable
{
    private final String houseName;

    private final int houseSocket;

    private final String houseHost;

    /**
     * @param name - The name of the auction house as a string.
     * @param socket - The socket value the auction house is hosting at.
     * @param hostName - A string containing the host name an auction house is at.
     * Registration is used to set a Registration objects fields.
     */
    Registration(String name, int socket, String hostName)
    {
        this.houseName = name;

        this.houseSocket = socket;

        this.houseHost = hostName;
    }

    /**
     * @return getHouseName returns a String.
     * getHouseName is used to get string contained in houseName.
     */
    String getHouseName()
    {
        return this.houseName;
    }

    /**
     * @return getHouseSocket returns an int.
     * getHouseSocket is used to get the value stored in houseSocket.
     */
    int getHouseSocket()
    {
        return this.houseSocket;
    }

    /**
     * @return getHostName returns a string.
     * getHostName is used to the string stored in houseHost.
     */
    String getHostName()
    {
        return this.houseHost;
    }
}
