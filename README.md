# Project-4-Public-Auction

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
2. When running the jar, ensure the first argument is the Bank server IP, the second argument is the Auction Central
IP, and the third is your name. If not properly entered, the Agent will properly close and need to be restarted.
3. If you setup the Bank and Auction Central correctly, you will now be at the Main Menu prompt of the Agent, where you
can either 1. see a list of auction houses, or $ see current account balance. Note that every agent starts out with an
account balance of 100.
4. If no Auction House server has been setup, the list of auction houses will be empty, so make sure to setup
some auction houses so you can start playing!
5. From here on out, the prompts through the command line will guide you on how to play. Just follow their direction!

==============================================

===============How To Use Bank================

The Bank server is run from the BankManager class. BankManager takes two command line arguments, the first is the port number
that AuctionCentralServer will connect to and the second argument is the port number for the Agent to connect to. BankManager
will accept multiple connections for Agent's so multiple agents can create accounts at the same time. BankManager does not accept
multiple connections from AuctionCentral it assumes that once the connection is made it lasts till the program is exited. After the
Bank connects to AuctionCentral it supports operations to put funds in hold (they bid on a item), release the funds back to the
agents account (they lost a auction) and withdrawing the funds from the agents account (when they won a auction). When BankManager
established a connection to the Agent it takes the name passed in and uses that to create a unique account number, unique bankKey
and sets all agent's balance to 100. BankManager also checks for duplicate account numbers and bankKeys every time a new account is
set up to make sure they are all unique. When BankManager is being run it prints out certain statements that are not test statements
but are used to see where the program is currently executing.

==============================================