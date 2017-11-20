import java.io.Serializable;

class Registration implements Serializable
{
    //The public id for the auction house, this should probably be the auction house port number
    private final int publicId;

    //Initially set as an int can change if needed.
    private final int auctionKey;

    //Constructor for a Registration object, takes two ints id and key
    Registration(int id, int key)
    {
       this.publicId = id;

       this.auctionKey = key;
    }

    //Getter for id
    int getPublicId()
    {
        return publicId;
    }

    //Getter for auction key
    int getAuctionKey()
    {
        return auctionKey;
    }
}
