import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

class MiniHouse extends Thread
{
    //Socket used to communicate with an agent.
    private Socket agentSocket = null;

    //Socket used to communicate with auction central.
    private Socket centralSocket = null;

    //List of items for sale by this auction house.
    private ArrayList<AuctionItem> items = null;

    //Constructor for a mini auction house.
    MiniHouse(Socket agent, Socket central, ArrayList<AuctionItem> items)
    {
        //set the agent socket variable.
        this.agentSocket = agent;

        //Set the central socket variable.
        this.centralSocket = central;

        //Set the items variable.
        this.items = items;
    }

    public void run()
    {
        try
        {
            //Create an object output stream to the agent.
            ObjectOutputStream outFromHouse = new ObjectOutputStream(agentSocket.getOutputStream());

            //Create an object output stream to auction central.
            ObjectOutputStream toCentral = new ObjectOutputStream(centralSocket.getOutputStream());

            //Create an object input stream from an agent.
            ObjectInputStream inFromAgent = new ObjectInputStream(agentSocket.getInputStream());

            //Create an object input stream from auction central.
            ObjectInputStream fromCentral = new ObjectInputStream(centralSocket.getInputStream());

            //Boolean to keep listening for a clients input until they exit.
            boolean listening = true;

            //while listening is true continue to processes an agents requests.
            while(listening)
            {
                //If the object that is read is a bid handel bidding procedure.
                if((inFromAgent.readObject()) instanceof Bid)
                {
                    System.out.println("Handling bid.");
                }

                //If the object is a string.
                else if((inFromAgent.readObject()) instanceof String)
                {
                    //Store the message in a string and set its font to lower case.
                    String message = inFromAgent.readObject().toString().toLowerCase();

                    //If the string is equal to exit, set the listening variable to false.
                    if(message.equals("exit"))
                    {
                        listening = false;
                    }

                    //If the string is equal to list return the item list to the agent.
                    else if(message.equals("list"))
                    {
                        System.out.println("returning list.");
                    }

                    //If the message is something else return an error message for agent to process.
                    else
                    {
                        outFromHouse.writeObject("error");
                    }

                }

            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }


    }

}
