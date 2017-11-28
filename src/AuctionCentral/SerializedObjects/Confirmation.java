package AuctionCentral.SerializedObjects;

import java.io.Serializable;

public class Confirmation implements Serializable
{
    //The public id for the auction house, this should probably be the auction house port number
    private final int publicId;

    //Initially set as an int can change if needed.
    private final Integer auctionKey;

    //Constructor for a Confirmation object, takes two ints id and key
    public Confirmation(int id, int key)
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
