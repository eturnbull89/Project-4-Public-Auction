import java.io.Serializable;

class Registration implements Serializable
{
    private final String houseName;

    private final int houseSocket;

    Registration(String name, int socket)
    {
        this.houseName = name;

        this.houseSocket = socket;
    }

    String getHouseName()
    {
        return this.houseName;
    }

    int getHouseSocket()
    {
        return this.houseSocket;
    }
}
