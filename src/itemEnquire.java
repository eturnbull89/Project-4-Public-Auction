import java.io.Serializable;

class itemEnquire implements Serializable
{
    private final int id;

    private final int serialNumber;

    itemEnquire(int itemId, int serialNumber)
    {
        this.id = itemId;

        this.serialNumber = serialNumber;
    }

    int getId()
    {
        return this.id;
    }

    int getSerialNumber()
    {
        return this.serialNumber;
    }
}
