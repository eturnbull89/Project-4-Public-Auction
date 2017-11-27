package AuctionCentral;

import java.io.Serializable;

class Registration implements Serializable
{
    private final String houseName;

    private final int houseSocket;

    private final String houseHost;

    private int SecretKey;


    Registration(String name, int socket, String hostName)
    {
        this.houseName = name;

        this.houseSocket = socket;

        this.houseHost = hostName;
    }

    String getHouseName()
    {
        return this.houseName;
    }

    int getHouseSocket()
    {
        return this.houseSocket;
    }

    String getHostName()
    {
        return this.houseHost;
    }
}
