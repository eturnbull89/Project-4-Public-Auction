import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.activation.UnknownObjectException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 * =============================================
 * Project 4 Public Auction - CS 351 UNM
 * @authors Tristin Glunt  | tglunt@unm.edu
 *          Zach Fleharty  |
 *          Eric Turnbull  |
 *          Adam Spanswick |
 * =============================================
 */

public class Agent
{
    private String agentName = "";
    private AcctKey bankAccount;
    private Integer biddingKey;
    private ObjectInputStream inBank;
    private ObjectOutputStream outBank;
    private ObjectInputStream inAuctionCen;
    private ObjectOutputStream outAuctionCen;
    private ObjectInputStream inCurrentAuctionHouse;
    private ObjectOutputStream outCurrentAuctionHouse;

    /**
     *
     */

    public static void main(String[] args) throws IOException, UnknownObjectException, ClassNotFoundException
    {
        if(args.length != 5)
        {
            System.err.println("Please retry and input the bank hostName, auction central hostname, portNumber of the bank, portNumber "
                                + "of the auction central and your user name.");
            System.exit(1);
        }

        Agent agent = new Agent();

        String bankHostName = args[0];  //bank host name will be first argument
        String auctionCentralHostName = args[1];
        int bankPortNumber = Integer.parseInt(args[2]); //port number for bank will be 3rd arg
        int auctionCenPortNumber = Integer.parseInt(args[3]);
        agent.agentName = args[4];

        try
        {
            Socket agentAuctionCentralSocket = new Socket(auctionCentralHostName, auctionCenPortNumber);
            agent.outAuctionCen = new ObjectOutputStream(agentAuctionCentralSocket.getOutputStream());
            agent.outAuctionCen.flush();
            agent.inAuctionCen = new ObjectInputStream(agentAuctionCentralSocket.getInputStream());

            Socket agentBankSocket = new Socket(bankHostName, bankPortNumber);
            agent.outBank = new ObjectOutputStream(agentBankSocket.getOutputStream());
            agent.outBank.flush();
            agent.inBank = new ObjectInputStream(agentBankSocket.getInputStream());

            agent.registerWithBank();
            agent.registerWithAuctionCentral();
            agent.pollUserInput();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            System.err.println("Don't know about host ");
            System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void registerWithBank()
    {
        try
        {
            UserAccount myAccount = new UserAccount(agentName);
            outBank.writeObject(myAccount);
            outBank.flush();
            bankAccount = (AcctKey) inBank.readObject();

            System.out.println("Read in account number and agent key:");
            System.out.println("Bank number: " + bankAccount.getAccountNumber());
            System.out.println("Agent Key: " + bankAccount.getKey() + "\n");

            System.out.println("Inquiring on balance: ");
            inquireBankBalance();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void registerWithAuctionCentral()
    {
        try
        {
            outAuctionCen.writeObject(bankAccount.getKey());
            outAuctionCen.flush();
            biddingKey = (Integer) inAuctionCen.readObject();
            System.out.println(biddingKey.toString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * pollUserInput: the main while loop for the agent to interact with the servers. Different prompts
     * allow the user to display his balance or see a list of auction houses to join to start bidding on items.
     */
    private void pollUserInput() throws UnknownObjectException, IOException, ClassNotFoundException
    {
        Scanner sc = new Scanner(System.in);
        String input = "";
        while(!input.equals("Exit"))
        {
            System.out.println("\n \\Main Menu");
            System.out.println("Please enter the key corresponding to what you want to do\n" +
                    "(1) see list of auction houses \n" +
                    "($) see current account balance\n" +
                    "(Exit) to exit");
            input = sc.next();
            if(input.equals("1"))
            {
                askAcForAh();
            }
            else if(input.equals("$"))
            {
                inquireBankBalance();
            }
            else if(!input.equals("Exit"))
            {
                System.out.println("Please enter a valid input.");
            }

        }
    }

    /**
     * askAcForAh (ask auction central for auction houses): this method will communicate with the auction central over a
     * socket to get the list of auction houses. once the auction central returns the list of auction houses, we will
     * ask the agent which house they are wanting to join, we will then join that house to start bidding on items.
     */
    private void askAcForAh() throws UnknownObjectException, IOException
    {
        try
        {

            ArrayList<Registration> listOfAuctionHouses = requestListOfAuctionHouses();

            if(listOfAuctionHouses != null)
            {
                while (!listOfAuctionHouses.isEmpty())
                {
                    System.out.println("\n Main Menu\\AuctionCentral");
                    listOfAuctionHouses = requestListOfAuctionHouses();
                    printListOfAuctionHouses(listOfAuctionHouses);

                    System.out.println("Which auction house would you like to join? Or type Exit to quit");
                    Scanner sc = new Scanner(System.in);
                    String input = sc.next();
                    if(input.equals("Exit"))
                    {
                        return;
                    }
                    else
                    {
                        int auctionHouseNum = Integer.parseInt(input);
                        joinAuctionHouse(listOfAuctionHouses, auctionHouseNum);
                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * joinAuctionHouse: once a user joins an auction house, will display the list of auction items.
     * We will then ask the agent which item they're interested in bidding for. Once we get the item they're bidding
     * for, we will display how much the current bid for that item is. We will then write to the auction house our
     * bidding key, the item we're bidding on, and the amount we're bidding. The bidding process may be put into it's
     * own method later, and be in a while loop to go until the itemBiddingOn timer runs out.
     * @param listOfAuctionHouses
     * @param auctionHouseNum
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void joinAuctionHouse(ArrayList<Registration> listOfAuctionHouses, int auctionHouseNum)
            throws IOException, ClassNotFoundException
    {
        Registration auctionHouse = listOfAuctionHouses.get(auctionHouseNum - 1);
        try
        {
            System.out.println("\n Main Menu\\AuctionCentral\\" + auctionHouse.getHouseName());
            setCurrentAuctionHouseStreams(auctionHouse);

            ArrayList<AuctionItem> listOfAuctionItems = requestListOfAuctionItems();
            if(listOfAuctionItems != null)
                printListOfAuctionItems(listOfAuctionItems);

            System.out.println("Which auction item would you like to bid on?");
            Scanner sc = new Scanner(System.in);
            int itemNumber = sc.nextInt();
            AuctionItem itemBiddingOn = listOfAuctionItems.get(itemNumber);

            System.out.println("\n Main Menu\\AuctionCentral\\"  + auctionHouse.getHouseName()
                    + "\\" + itemBiddingOn.getName());

            System.out.println(itemBiddingOn.getCurrentBid());

            System.out.println("How much would you like to bid?");
            int bidAmount = sc.nextInt();
            Bid agentBidOnItem = new Bid(bankAccount.getKey(), itemBiddingOn);
            agentBidOnItem.setBidAmount(bidAmount);

            outCurrentAuctionHouse.writeObject(agentBidOnItem);
            agentBidOnItem = (Bid)inCurrentAuctionHouse.readObject();
            while(!agentBidOnItem.getBidStatus().equals("Over"))
            {
                System.out.println("Current Balance: ");
                inquireBankBalance();

                System.out.println("Current highest bid on " + itemBiddingOn.getName() + " : " + itemBiddingOn.getCurrentBid());

                System.out.println("How much would you like to bid? Or type Exit to stop bidding on " + itemBiddingOn.getName());
                String bidInput = sc.next();
                if(bidInput.equals("Exit"))
                    return;
                else
                {
                    bidAmount = Integer.parseInt(bidInput);
                    if (bidAmount < itemBiddingOn.getCurrentBid())
                    {
                        System.out.println("Please enter a higher bid");
                    } else
                    {
                        agentBidOnItem.setBidAmount(bidAmount);
                        outCurrentAuctionHouse.writeObject(agentBidOnItem);
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void inquireBankBalance()
            throws IOException, ClassNotFoundException
    {
        outBank.writeObject("Inquire");
        outBank.writeObject(bankAccount.getKey());

        String balance = (String) inBank.readObject();
        System.out.println(balance + "\n");
    }

    public void setCurrentAuctionHouseStreams(Registration auctionHouse)
    {
        try
        {
            Socket auctionHouseSocket = new Socket(auctionHouse.getHostName(), auctionHouse.getHouseSocket());
            outCurrentAuctionHouse = new ObjectOutputStream(auctionHouseSocket.getOutputStream());
            inCurrentAuctionHouse = new ObjectInputStream(auctionHouseSocket.getInputStream());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public ArrayList<AuctionItem> requestListOfAuctionItems()
    {
        try
        {
            String requestForAuctionItems = "List";
            outCurrentAuctionHouse.writeObject(requestForAuctionItems);
            outCurrentAuctionHouse.flush();

            ArrayList<AuctionItem> listOfAuctionItems = (ArrayList<AuctionItem>) inCurrentAuctionHouse.readObject();
            return listOfAuctionItems;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<Registration> requestListOfAuctionHouses()
    {
        try
        {
            System.out.println("Trying to request for auction houses...");
            String requestForAuctionHouses = "AuctionHouses";
            outAuctionCen.writeObject(requestForAuctionHouses);
            outAuctionCen.flush();

            System.out.println("Trying to read in auction houses...");
            ArrayList<Registration> listOfAuctionHouses = (ArrayList<Registration>)inAuctionCen.readObject();
            System.out.println("Returning auction houses...");
            return listOfAuctionHouses;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private void printListOfAuctionItems(ArrayList<AuctionItem> auctionItems)
    {
        int counter = 0;

        for(AuctionItem ai : auctionItems)
        {
            counter++;
            System.out.println(counter + ". " + ai.getName());
        }
    }

    private void printListOfAuctionHouses(ArrayList<Registration> auctionHouses)
    {
        int counter = 0;

        for(Registration ah : auctionHouses)
        {
            counter++;
            System.out.println(counter + ". " + ah.getHouseName());
        }
    }
}
