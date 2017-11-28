package AuctionCentral;

import AuctionCentral.SerializedObjects.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TalkToBank
{
    Socket bankSocket; //Socket Connected to the bank, not sure if socket is needed

    ObjectOutputStream out; //output stream to send data to the bank

    ObjectInputStream in; //input stream to recieve data from the bank

    public TalkToBank(Socket bankSocket){

        this.bankSocket = bankSocket;

        try
        {
            out = new ObjectOutputStream(bankSocket.getOutputStream());

            out.flush();

            in = new ObjectInputStream(bankSocket.getInputStream());

        }catch (IOException e){
            System.out.println("cant talk to bank.");
            e.printStackTrace();
        }
    }

    public boolean RequestFromBank(Transaction trans){
        boolean result = false;

        try
        {

            out.writeObject(trans);

            //expects the bank to return a boolean value denoting the success of the transaction
            result = (boolean) in.readObject();

        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
