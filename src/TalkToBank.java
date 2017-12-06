import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class TalkToBank
{

    ObjectOutputStream out; //output stream to send data to the bank

    ObjectInputStream in; //input stream to receive data from the bank

    TalkToBank(Socket bankSocket)
    {
        try
        {
            out = new ObjectOutputStream(bankSocket.getOutputStream());

            out.flush();

            in = new ObjectInputStream(bankSocket.getInputStream());

        }
        catch (IOException e)
        {
            System.out.println("Can't talk to bank.");
            e.printStackTrace();
        }
    }

    boolean RequestFromBank(Transaction trans)
    {
        boolean result = false;

        try
        {

            out.writeObject(trans);

            //expects the bank to return a boolean value denoting the success of the transaction
            result = (boolean) in.readObject();

        }

        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
