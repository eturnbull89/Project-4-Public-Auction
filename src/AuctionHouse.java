import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class AuctionHouse
{
    //Server host name
    private String hostName;

    //Port number for this auction house
    private int housePort;

    //Name of this auction house
    private String houseName;

    //Port number of auction central
    private int centralPort;

    //Confirmation object, holds registration info set by auction central.
    private Confirmation houseReg;

    //Socket connection with auction central
    private Socket centralSocket;

    public static void main(String[] args) throws IOException
    {
        //Check that initial arguments number 4, if not throw an error message and exit
        if(args.length != 4)
        {
            System.err.println("Please retry and input the hostName, the auction house port number, auction house name, " +
                    "and auction central port number");
            System.exit(1);
        }

        //Create a new auction house and set needed variables.
        AuctionHouse house = new AuctionHouse();

        //Server host name, first argument
        house.hostName = args[0];

        //This auction houses port number, second argument
        house.housePort = Integer.parseInt(args[1]);

        //This auction houses given name, third argument
        house.houseName = args[2];

        //The port number for auction central, forth argument
        house.centralPort = Integer.parseInt(args[3]);

        //Register with auction central and set houseReg field
        house.register(house.hostName, house.housePort, house.centralPort);
    }

    private void register(String hostName, int housePort, int centralPort) throws IOException
    {
        Registration centralReg = new Registration(houseName, housePort, hostName);

        try(
                //Create the socket to talk to the auction central
                Socket centralSocket = new Socket(hostName, centralPort);

                //Create an object output stream from this auction house.
                ObjectOutputStream outFromHouse = new ObjectOutputStream(centralSocket.getOutputStream());

                //Create an object input stream from auction central
                ObjectInputStream inFromCentral = new ObjectInputStream(centralSocket.getInputStream())
                )
        {
            //Set the centralSocket field to the created socket for use later.
            this.centralSocket = centralSocket;

            //Write to auction central the created registration.
            outFromHouse.writeObject(centralReg);

            try
            {
                //Receive back the registration object and set houseReg.
                this.houseReg = (Confirmation) inFromCentral.readObject();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                //Close the object input stream from auction central.
                inFromCentral.close();

                //Close the object output stream from this auction house.
                outFromHouse.close();
            }

        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
