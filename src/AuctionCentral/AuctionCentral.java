package AuctionCentral;

import javax.sound.sampled.Port;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class AuctionCentral
{
    //port number to use for the auctionCentral and bank
    private static final int Port = 1027;
    private static final int bankPort = 1026;


    //Socket over which this class communicates to the bank
    private static TalkToBank BankChannel;


    /*Maps a bid key to bank key's. Map is added to when an agent registers with AuctionCentral.AuctionCentral and bid key is
    * created by this class then passed back to the agent.*/
    //TalkToAuctionHouse uses this hashMap, that's the reason it's package-protected and
    //not private. NOTE: multiple threads may be reading from and writing to hash map
    //both actions should be synchronized and performed atomically
    static HashMap<Integer,Integer> BidKeyToBankKey = new HashMap<>();

    /*Holds the registration objects from all AuctionHouses registered with this auctionCentral.
    * Registration holds, 1)house name 2)port number 3)host name*/
    static HashMap<Registration,Integer> HouseToSecretKey = new HashMap<>();

    private static ArrayList<talkToAuctionHouse> houseSockets = new ArrayList<>();

    private static ArrayList<TalkToAgent> agentSockets = new ArrayList<>();

    public static void main(String args[]){


        int numOfArgs = 1;
        if(args.length != numOfArgs){ //check for correct number of arguments
            System.out.println("incorrect number of arguments");
            System.exit(-1);
        }

        String HostName = args[0]; //Hostname of Bank Server

        //Establish a connection to the bank based on the hostName and bankPort
        try
        {
            System.out.println("connection to bank...");
            Socket BankConnection = new Socket(HostName, bankPort);
            System.out.println("connected to bank!");
            BankChannel = new TalkToBank(BankConnection);
        } catch (IOException e){
            e.printStackTrace();
        }


        Random ran = new Random(); //used to generate random keys for houses and agents


        //begin server
        try
        {
            ServerSocket AuctionCentralServer = new ServerSocket(Port);

            //Wait for Connections and instantiate them where appropriate
            while(true){
                Socket clientSocket = AuctionCentralServer.accept();

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());


                Object fromClient = in.readObject(); //read data from unknown client
                if(fromClient instanceof Registration){ //new client is an auctionHouse

                    Registration houseReg = (Registration)fromClient;

                    Integer secretKey = ran.nextInt(100);
                    //TODO: check if key was already generated



                    HouseToSecretKey.put(houseReg,secretKey); //add house and secret key to mapping
                    //Create public Id
                    int publicID = ran.nextInt(100); //not sure what this is. Sets to random integer for now

                    //Create confirmation and send back
                    Confirmation houseConfirmation = new Confirmation(publicID,secretKey);
                    out.writeObject(houseConfirmation);

                    //Create new TalkToHouse object and communicate with this house on a sepearate thread
                    talkToAuctionHouse newHouseCommunication = new talkToAuctionHouse(clientSocket,BankChannel);
                    houseSockets.add(newHouseCommunication);

                }else if(fromClient instanceof Integer){
                    //add bidkeyToBankKey mapping
                    Integer agentBankKey = (Integer)fromClient;
                    System.out.println("New agent sent bankKey: " + agentBankKey);
                    Integer bidKey = ran.nextInt(100); //create bidding key for agent
                    System.out.println("Agents BidKey: " + bidKey);
                    //TODO: check if bidkey previously generated

                    BidKeyToBankKey.put(bidKey,agentBankKey);

                    /**Creates fake Registration!!!**/
                    Registration fake = new Registration("first house",1024,"house");
                    HouseToSecretKey.put(fake,23);

                    /***Delete this!!*/
                    //Initiate a talk to agent method on a new thread
                    TalkToAgent newAgent = new TalkToAgent(bidKey,clientSocket,in,out);
                    agentSockets.add(newAgent); //add new agentSocket to list of agentSockets
                }
            }
        } catch (IOException|ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
