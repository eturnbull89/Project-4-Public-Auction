
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**Zach Fleharty
 * Sets up AuctionCentralServer and waits to listen to multiple clients*/
public class AuctionCentralServer
{
    //I'm not sure if this will come in useful but it may be useful to keep
    //a reference to all executing threads on hand
    private static ArrayList<Thread> runningThreads = new ArrayList<>();

    //args[0]: bank hostname
    //args[1]: bank portNumber
    //args[2]: this server port number
    public static void main(String args[]){
        int numOfArgs = 3;

        if(args.length != numOfArgs){
            System.out.println("incorrect number of arguments");
            System.exit(-1);
        }

        //Use arguments to connect to bank and set up port
        String bankHost = args[0];
        int bankPort = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);

        AuctionCentralProtocol ACP; //Protocol which communicates with clients

        //establish connection with bank
        try
        {
            System.out.println("connecting to bank...");
            Socket BankSocket = new Socket(bankHost, bankPort);
            TalkToBank BankConnection = new TalkToBank(BankSocket);
            System.out.println("connected to bank!");

            ACP = new AuctionCentralProtocol(BankConnection);


            //wait for clients to connect

            ServerSocket ACServer = new ServerSocket(serverPort);

            //Wait for Connections and instantiate them where appropriate
            while (true)
            {
                //Creates new Socket with a new connected client
                Socket clientSocket = ACServer.accept();

                //Set up communication channels with the new Socket
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();

                //Client sends first object either Registration(House) or BankKey(agent)
                Object fromClient = in.readObject();
                if(fromClient instanceof Registration)
                {
                    //Create a new thread to communicate with this house
                    System.out.println("Got registration");
                    Runnable talkToHouse = () -> ACP.CommunicateWithHouse(((Registration)fromClient),in,out);
                    Thread newThread = new Thread(talkToHouse); //initiate new thread

                    runningThreads.add(newThread); //add newThread to the list of running threads
                    newThread.start();

                }
                else if(fromClient instanceof Integer)
                {
                    //create a new thread to communicate with this Agent
                    Runnable talkToAgent = () -> ACP.CommunicateWithAgent(((Integer)fromClient), clientSocket, in, out);
                    Thread newThread = new Thread(talkToAgent);

                    runningThreads.add(newThread); //add newThread to the list of running threads
                    newThread.start();
                }
                else
                {
                    System.out.println("Returning auction houses to agent...");
                    Runnable talkToAgent = () -> ACP.generalAgentCom(fromClient, out, in);
                    Thread Runner = new Thread(talkToAgent);
                    runningThreads.add(Runner);
                    Runner.start();
                }
            }
        }catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
