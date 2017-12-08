 =============================================
 Project 4 Public Auction - CS 351 UNM
 @authors Tristin Glunt   | tglunt@unm.edu
           Zach Fleharty  |
           Eric Turnbull  | eturnbull@unm.edu
           Adam Spanswick | aspanswick@unm.edu
=============================================

===============How To Use Agent===============
The clients interface (Agent) has been configured to run on a linux machine. Specifically, system out statements
are color coded and if run on a Windows machine may seem buggy. So, please do not take into consideration the buggyness
on a Windows machine, as per the classes guidelines we write code to run or be graded on the machines in lab. Thanks!

1. Ensure the Bank server and Auction Central server are properly setup.
2. When running the jar, ensure arguments are as follows:

{Bank host name} {Auction Central Host Name} {Bank Port Number} {Auction Central Port Number} {UserName}

If not properly entered, the Agent will properly close and need to be restarted.
3. If you setup the Bank and Auction Central correctly, you will now be at the Main Menu prompt of the Agent, where you
can either 1. see a list of auction houses, or $ see current account balance. Note that every agent starts out with an
account balance of 100.
4. If no Auction House server has been setup, the list of auction houses will be empty, so make sure to setup
some auction houses so you can start actually playing!
5. From here on out, the prompts through the command line will guide you on how to play. Just follow their direction!

==============================================

===============How To Use Bank================

The Bank server is run from the BankManager class. BankManager takes two command line arguments, the first is the port number
that AuctionCentralServer will connect to and the second argument is the port number for the Agent to connect to. BankManager
will accept multiple connections for Agent's so multiple agents can create accounts at the same time. BankManager does not accept
multiple connections from AuctionCentral it assumes that once the connection is made it lasts till the program is exited. After the
Bank connects to AuctionCentral it supports operations to put funds in hold (they bid on a item), release the funds back to the
agents account (they lost an auction) and withdrawing the funds from the agents account (when they won a auction). When BankManager
established a connection to the Agent it takes the name passed in and uses that to create a unique account number, unique bankKey
and sets all agent's balance to 100. BankManager also checks for duplicate account numbers and bankKeys every time a new account is
set up to make sure they are all unique. When BankManager is being run it prints out certain statements that are not test statements
but are used to see where the program is currently executing.

==============================================

==========How To Use Auction House============

An Auction House is run from the AuctionHouse class.

Each AuctionHouse takes 5 command line arguments in this order: host name an auction house will be at, the port number
an auction house will be hosting from, the visual name of the house, the port number that auction central is located at,
the host name that auction central is located at, an integer between 1-3 that indicates what the house should sell from
a set list. A value of 1 means the house will sell art, 2 = books, and 3 = cars, any other value will default to the
house selling art. One example of this that we have used for testing is:

localHost 1040 Cars 1032 localHost 3

IMPORTANT: An auction house has to be created after the bank server and auction central server have been created, making
an auction house before will cause an error.

Once an auction house has been set up there is no other direct input needed on the agents end. All interaction with an
auction house is done via Agent. There are some print statements that occur during the operation of the programs. If
the value given was not between 1-3 it will print that the house is using its default value. It also indicates that the
house was registered with auction central. When the bidding on an item is done it prints that it is updating the
winners list that is maintained. These statements were kept in to verify that the server was running.

==============================================

===========How To Use Auction Central=========


Auction Central is ran from the AuctionCentralServer class. 

The AuctionCentralServer takes in three command line arguments.The first arguments is the bank server IP. It is important to ensure the bank server is running before launching AuctiuonCentralServer. The second argument is the port number that the bank uses to connect to AuctionCentral. This port number should be the same number given to the BankManager class as it's first command line argument. The third argument should be the port number on which you wish to run the AuctionCentralServer. This number should not match any argument given to the BankManager. If BankManager gets run with arguments [1027] [1028] on Bank IP then the AuctionCentralServer should be launched with arguments [Bank IP] [1027] [Any number that != 1027 || 1028] 

If a global boolean called DEBUG is set to true then when the AuctionCentralServer runs debug statements will print on the CommandLine to show where the AuctionCentral is currently executing. If this is set to false the AuctionCentral has no print statements other when a house or agent disconnects to notify that a disconnect occured. 
