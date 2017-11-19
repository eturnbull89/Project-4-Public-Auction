import java.io.IOException;

/**
 * Created by tristin on 11/18/2017.
 */
public class Agent
{
    private String agentName;   //to have 100% unique agent IDs I think the names would have to be made from a server
    private Account bankAccount;    //given from a bank

    /**
     * So an agent needs to be it's own client, so it will have it's own Main method and not a
     * constructor. The client will
     * somehow know the hostname of the Bank server, it will tell the Bank "Hey I want info", and then
     * the bank will return it's bankAccount. I also think that the AgentName may have to be given
     * from the Bank(server), otherwise the agentNames can't be truly unique.
     */

    public static void main(String[] args) throws IOException
    {

    }
}
