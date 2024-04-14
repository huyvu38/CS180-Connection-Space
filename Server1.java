import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Team Project
 *
 * Server.java
 *
 * @version 28 March 2024
 */
public class Server1 {
    private static final int PORT = 4242;
    private static ExecutorService threadPool = Executors.newCachedThreadPool(); // Using thread pool for better performance
    public static Database database;
    public static ArrayList<UserAccount> allUserAccount;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        database = new Database("AllUserAccount.txt");  // Correct initialization
        database.readAllUserAccount();
        allUserAccount = database.getAllUserAccount();

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected to a new client.");
                Runnable clientHandler = new ClientHandler1(socket);
                threadPool.execute(clientHandler);
            }
        } finally {
            serverSocket.close();
        }
    }
}

class ClientHandler1 implements Runnable {
    private Socket socket;

    public ClientHandler1(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Handling client communication
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            while (true) {
                String command = reader.readLine();
                //Command 1 is create account
                if (command.equals("1")) {
                    boolean result = true;
                    //Get all information for server
                    String username = reader.readLine();
                    String password = reader.readLine();
                    String age = reader.readLine();
                    String gender = reader.readLine();
                    String nationality = reader.readLine();
                    String job = reader.readLine();
                    String hobby = reader.readLine();
                    //Check information
                    if (username.contains(" ") || username.contains(";")) {
                        result = false;
                    }
                    if (password.contains(" ") || password.contains(";")) {
                        result = false;
                    }
                    if (nationality.contains(" ") || nationality.contains(";")) {
                        result = false;
                    }
                    if (job.contains(" ") || job.contains(";")) {
                        result = false;
                    }
                    if (hobby.contains(" ") || hobby.contains(";")) {
                        result = false;
                    }
                    int newAge = 0;
                    try {
                        newAge = Integer.parseInt(age);
                    } catch (NumberFormatException e) {
                        result = false;
                    }
                    if (newAge < 0) {
                        result = false;
                    }
                    //If the user enter all valid information -> the result still true
                    //Then check if the username is valid to create a new Profile
                    Profile newUserProfile = new Profile(username, password, newAge, gender, nationality, job, hobby);
                    UserAccount newUserAccount = new UserAccount(newUserProfile);

                    //if the result is still true -> send back to the client that account create successfully
                    if (createAccount(Server1.database, newUserAccount, username, password)) {
                        result = true;
                    }
                    if (result) {
                        writer.write("Create account successfully. You have to log in again.");
                        writer.println();
                        writer.flush();
                    } else {
                        writer.write("The account is already exist or you enter wrong information.");
                        writer.println();
                        writer.flush();
                    }
                }
                //Command 2 is log in
                if (command.equals("2")) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    //Log in success
                    if (loginAccount(username, password)) {
                        writer.write("Log in successfully");
                        writer.println();
                        writer.flush();
                        while (true) {
                            String choice = reader.readLine();
                            //Choice 1 is view their own profile
                            if (choice.equals("1")) {
                                //Get the information that user want to view
                                String viewChoice = reader.readLine();
                                for (UserAccount userAccount : Server1.allUserAccount) {
                                    if (userAccount.getUserProfile().getUserName().equals(username)) {
                                        if (viewChoice.equals("1")) {
                                            writer.write(userAccount.getUserProfile().getAge());
                                        }
                                        if (viewChoice.equals("2")) {
                                            writer.write(userAccount.getUserProfile().getGender());
                                        }
                                        if (viewChoice.equals("3")) {
                                            writer.write(userAccount.getUserProfile().getNationality());
                                        }
                                        if (viewChoice.equals("4")) {
                                            writer.write(userAccount.getUserProfile().getJob());
                                        }
                                        if (viewChoice.equals("5")) {
                                            writer.write(userAccount.getUserProfile().getHobby());
                                        }
                                        writer.println();
                                        writer.flush();
                                    }
                                }
                            }
                            if (choice.equals("2")) {
                                String editChoice = reader.readLine();
                                String editInformation = reader.readLine();
                                for (UserAccount userAccount : Server1.allUserAccount) {
                                    if (userAccount.getUserProfile().getUserName().equals(username)) {
                                        if (editChoice.equals("1")) {
                                            if (editInformation.contains(" ") || editInformation.contains(";")) {
                                                writer.write("Can not edit your information");
                                            } else {
                                                if (checkPasswordLength(editInformation)) {
                                                    userAccount.getUserProfile().setPassword(editInformation);
                                                    writer.write("Edit successfully");
                                                } else {
                                                    writer.write("Can not edit your information");
                                                }
                                            }
                                        }
                                        if (editChoice.equals("2")) {
                                            try {
                                                int editAge = Integer.parseInt(editInformation);
                                                if (editAge < 0) {
                                                    writer.write("Can not edit your information");
                                                    userAccount.getUserProfile().setAge(editAge);
                                                    writer.write("Edit successfully");
                                                } else {
                                                    userAccount.getUserProfile().setAge(editAge);
                                                    writer.write("Edit successfully");
                                                }
                                            } catch (Exception e) {
                                                writer.write("Can not edit your information");
                                            }
                                        }
                                        if (editChoice.equals("3")) {
                                            userAccount.getUserProfile().setGender(editInformation);
                                            writer.write("Edit successfully");
                                            //Assume that the client only choose from Male, Female, Other
                                        }
                                        if (editChoice.equals("4")) {
                                            if (editInformation.contains(" ") || editInformation.contains(";")) {
                                                writer.write("Can not edit your information");
                                            } else {
                                                userAccount.getUserProfile().setNationality(editInformation);
                                                writer.write("Edit successfully");
                                            }
                                        }
                                        if (editChoice.equals("5")) {
                                            if (editInformation.contains(" ") || editInformation.contains(";")) {
                                                writer.write("Can not edit your information");
                                            } else {
                                                userAccount.getUserProfile().setJob(editInformation);
                                                writer.write("Edit successfully");
                                            }
                                        }
                                        if (editChoice.equals("6")) {
                                            if (editInformation.contains(" ") || editInformation.contains(";")) {
                                                writer.write("Can not edit your information");
                                            } else {
                                                userAccount.getUserProfile().setHobby(editInformation);
                                                writer.write("Edit successfully");
                                            }
                                        }
                                    }
                                }
                                writer.println();
                                writer.flush();
                                Server1.database.saveAllUserAccount();
                            }
                            if (choice.equals("3")) {
                                String addFriendUserName = reader.readLine();
                                if (addFriend(username, addFriendUserName)) {
                                    writer.write("Add friend successfully");
                                } else {
                                    writer.write("You can not add that user");
                                }
                                writer.println();
                                writer.flush();
                            }
                            if (choice.equals("4")) {
                                String unfriendUserName = reader.readLine();
                                if (deleteFriend(username, unfriendUserName)) {
                                    writer.write("Unfriend successfully");
                                } else {
                                    writer.write("You can not unfriend that user");
                                }
                                writer.println();
                                writer.flush();
                            }
                            if (choice.equals("5")) {
                                String blockUserName = reader.readLine();
                                if (blockUser(username, blockUserName)) {
                                    writer.write("Block successfully");
                                    //If both users are friend then delete after block
                                    deleteFriend(username, blockUserName);
                                } else {
                                    writer.write("You can not block that user");
                                }
                                writer.println();
                                writer.flush();
                            }
                            if (choice.equals("6")) {
                                String unblockUserName = reader.readLine();
                                if (unblockUser(username, unblockUserName)) {
                                    writer.write("Unblock successfully");
                                } else {
                                    writer.write("You can not unblock that user");
                                }
                                writer.println();
                                writer.flush();
                            }
                            if (choice.equals("7")) {
                                String userName = reader.readLine();
                                System.out.println(userName);
                                UserAccount currentUserAcc = null;
                                boolean hasFriends = false;
                                for (UserAccount userAccount: Server1.allUserAccount) {
                                    if (userAccount.getUserProfile().getUserName().equals(userName)) {
                                        currentUserAcc = userAccount;
                                        if (!userAccount.getFriendList().isEmpty()) {
                                            hasFriends = true;
                                        }
                                    }
                                }
                                writer.write(String.valueOf(hasFriends));
                                writer.println();
                                writer.flush();
                                String ans = reader.readLine();
                                System.out.println(ans);
                                if (ans.equals("1")) {
                                    //Send message to specific person
                                    String receiver = reader.readLine();
                                    String message = reader.readLine();
                                    boolean isBlock = false;
                                    for (UserAccount userAccount: Server1.allUserAccount) {
                                        if (userAccount.getUserProfile().getUserName().equals(receiver)) {
                                            isBlock = userAccount.getBlockList().contains(userName);
                                        }
                                    }
                                    if (sendMessage(userName, receiver, message, isBlock)) {
                                        writer.write("Message sent successfully");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        writer.write("Message sent failed");
                                        writer.println();
                                        writer.flush();
                                    }

                                } else if (ans.equals("2")) {
                                    //Send message to friends
                                    String message = reader.readLine();
                                    // System.out.println(message);
                                    assert currentUserAcc != null;
                                    ArrayList<String> friendList = currentUserAcc.getFriendList();
                                    String result = restrictMessage(userName, friendList, message);
                                    //System.out.println(result);
                                    if (result == null) {
                                        writer.write("Message sent successfully");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        writer.write(result);
                                        writer.println();
                                        writer.flush();
                                    }

                                } else if (ans.equals("3")) {
                                    //View history Message
                                    String name = reader.readLine();
                                    String readMsg = printHistoryMessage(userName, name);
                                    String[] messageResult = readMsg.split("\n");
                                    //System.out.println(readMsg);
                                    for (int i=0; i< messageResult.length; i++) {
                                        writer.write(messageResult[i]);
                                        writer.println();
                                        writer.flush();
                                    }
                                    writer.write("end");
                                    writer.println();
                                    writer.flush();

                                }
                            }
                            if (choice.equals("8")) {
                                String word = reader.readLine();
                                //username is the one who search other user
                                ArrayList<String> findUserName = searchUser(username, word);
                                if (findUserName.size() == 0) {
                                    writer.write("Can not find any user");
                                    writer.println();
                                    writer.flush();
                                } else {
                                    String allFindUser = "";
                                    for (int i = 0; i < findUserName.size(); i++) {
                                        String findUser = findUserName.get(i);
                                        if (findUser != null && !findUser.isEmpty()) {
                                            allFindUser += findUser;
                                            if (i < (findUserName.size() - 1)) {
                                                allFindUser += " ";
                                            }
                                        }
                                    }
                                    writer.write(allFindUser);
                                    writer.println();
                                    writer.flush();
                                    //User want to view Profile of other user
                                    String userNameToViewProfile = reader.readLine();
                                    String viewOtherProfileChoice = reader.readLine();
                                    for (UserAccount userAccount : Server1.allUserAccount) {
                                        if (userAccount.getUserProfile().getUserName().equals(userNameToViewProfile)) {
                                            if (viewOtherProfileChoice.equals("1")) {
                                                writer.write(userAccount.getUserProfile().getAge());
                                            }
                                            if (viewOtherProfileChoice.equals("2")) {
                                                writer.write(userAccount.getUserProfile().getGender());
                                            }
                                            if (viewOtherProfileChoice.equals("3")) {
                                                writer.write(userAccount.getUserProfile().getNationality());
                                            }
                                            if (viewOtherProfileChoice.equals("4")) {
                                                writer.write(userAccount.getUserProfile().getJob());
                                            }
                                            if (viewOtherProfileChoice.equals("5")) {
                                                writer.write(userAccount.getUserProfile().getHobby());
                                            }
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                }
                            }
                            //Log Out
                            if (choice.equals("9")) {
                                break;
                            }
                        }
                    } else {
                        writer.write("Log in failure");
                        writer.println();
                        writer.flush();
                    }
                }
                //Exit the app
                if (command.equals("3")) {
                    socket.close();
                    writer.close();
                    reader.close();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("A User is disconnect");
            //e.printStackTrace();

        }
    }

    public boolean checkIfPasswordCorrect(Profile profile, String userPassword) {
        return profile.getPassword().equals(userPassword);
    }
    //We already check if contain space or semicolon
    public boolean checkPasswordLength(String password) {
        return password.length() >= 6;
    }
    public boolean checkUserNameFormat(String userName) {
        return userName.length() >= 4;
    }
    public boolean usernameInDatabase(String userName) {
        //From a list of user profile, find the specific username
        for (UserAccount eachUserAccount : Server1.allUserAccount) {
            if (eachUserAccount.getUserProfile().getUserName().equals(userName)) {
                return true; //User exist in the database
            }
        }
        return false; // User doesn't exist in the database
    }
    public boolean inFriendList(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            for (UserAccount userAccount : Server1.allUserAccount) {
                //Find the account of user1
                //If we find username2 in friend list of username1,
                // we don't have to check username1 in friendlist of username2
                if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                    //Check the friend list of user1
                    for (String friend : userAccount.getFriendList()) {
                        if (friend.equals(userNameTwo)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public boolean inBlockList(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            for (UserAccount userAccount : Server1.allUserAccount) {
                //Find the account of user1 and check if the user1 block user2
                if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                    //Check the block list of user1
                    for (String blockUser : userAccount.getBlockList()) {
                        if (blockUser.equals(userNameTwo)) {
                            return true; //Return true if username2 in blocklsit of username1
                        }
                    }
                }
            }
        }
        return false;
        //The method return false if user1 do not block user2
    }
    public synchronized boolean createAccount(Database database, UserAccount userAccount, String username, String password) {
        if (checkUserNameFormat(username) && checkPasswordLength(password)) {
            ArrayList<UserAccount> temp = database.getAllUserAccount();
            temp.add(userAccount);
            database.setAllUserAccount(temp);
            database.saveAllUserAccount();
            return true;
        }
        return false;
    }

    public synchronized boolean loginAccount(String username, String userPassword) {
        if (usernameInDatabase(username)) {
            for (UserAccount eachUserAccount: Server1.allUserAccount) {
                if (eachUserAccount.getUserProfile().getUserName().equals(username)) {
                    if (eachUserAccount.getUserProfile().getPassword().equals(userPassword)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
    public synchronized boolean addFriend(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (inBlockList(userNameOne, userNameTwo)) {
                return false; //User1 block user2
            }
            if (inBlockList(userNameTwo, userNameOne)) {
                return false; //User2 block user1
            }
            if (inFriendList(userNameOne, userNameTwo)) {
                return false; // two users already in the friend list so cannot add friend
            } else {
                //If both users not in friendlist
                for (UserAccount userAccount : Server1.allUserAccount) {
                    //Find the friendlist of user1
                    if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                        ArrayList<String> friendListUserOne = userAccount.getFriendList();
                        //Add the user2 to friend list of user1
                        friendListUserOne.add(userNameTwo);
                        userAccount.setFriendList(friendListUserOne);
                    }
                    //Find the friendlist of user2
                    if (userAccount.getUserProfile().getUserName().equals(userNameTwo)) {
                        ArrayList<String> friendListUserTwo = userAccount.getFriendList();
                        //Add the user1 to friend list of user2
                        friendListUserTwo.add(userNameOne);
                        userAccount.setFriendList(friendListUserTwo);
                    }
                }
                Server1.database.saveAllUserAccount();
                return true; //add friend success
            }
        }
        return false; //If one of two username not in the database
    }
    public synchronized boolean deleteFriend(String userNameOne, String userNameTwo) {
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (inFriendList(userNameOne, userNameTwo)) { //both users are friend
                for (UserAccount userAccount : Server1.allUserAccount) {
                    //Find the friendlist of user1
                    if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                        ArrayList<String> friendListUserOne = userAccount.getFriendList();
                        //remove the user2 to friend list of user1
                        friendListUserOne.remove(userNameTwo);
                        userAccount.setFriendList(friendListUserOne);
                    }
                    //Find the friendlist of user2
                    if (userAccount.getUserProfile().getUserName().equals(userNameTwo)) {
                        ArrayList<String> friendListUserTwo = userAccount.getFriendList();
                        //remove the user1 to friend list of user2
                        friendListUserTwo.remove(userNameOne);
                        userAccount.setFriendList(friendListUserTwo);
                    }
                }
                Server1.database.saveAllUserAccount();
                return true; // remove friend successfully
            }
        }
        return false; //If one of two username not in the database or not in the friendlist of each other
    }
    public synchronized boolean blockUser(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (inBlockList(userNameOne, userNameTwo)) {
                return false; //User1 already block user2
            }
            if (inBlockList(userNameTwo, userNameOne)) {
                return false; //User2 block user1 so user1 cannot block user2
            }
            for (UserAccount userAccount : Server1.allUserAccount) {
                //Find the blocklist of user1
                if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                    ArrayList<String> blockListUserOne = userAccount.getBlockList();
                    //add the user2 to block list of user1
                    blockListUserOne.add(userNameTwo);
                    userAccount.setBlockList(blockListUserOne);
                    Server1.database.saveAllUserAccount();
                    return true; //user1 block user2 successfully
                }
            }
        }
        return false;
    }
    public synchronized boolean unblockUser(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (inBlockList(userNameOne, userNameTwo)) {
                //User1 block user2 and want to remove user2 from blocklist
                for (UserAccount userAccount : Server1.allUserAccount) {
                    //Find the blocklist of user1
                    if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                        ArrayList<String> blockListUserOne = userAccount.getBlockList();
                        //remove the user2 from the block list of user1
                        blockListUserOne.remove(userNameTwo);
                        userAccount.setBlockList(blockListUserOne);
                        Server1.database.saveAllUserAccount();
                        return true;
                    }
                }
            }
        }
        return false;//if one of the username not valid or user2 not in block list of user1
    }


    //User1 finds user2
    public synchronized ArrayList<String> searchUser(String userNameOne, String word) {
        ArrayList<String> findUserName = new ArrayList<>();
        //Check if no account block user1
        for (UserAccount userAccount : Server1.allUserAccount) {
            if (userAccount.getUserProfile().getUserName().contains(word)) {
                boolean userOneIsBlocked = false;
                for (String blockListOfUserTwo : userAccount.getBlockList()) {
                    //That user not block user1
                    if (blockListOfUserTwo.equals(userNameOne) == true) {
                        userOneIsBlocked = true;
                        break;
                    }
                }
                if (userOneIsBlocked == false) {
                    findUserName.add(userAccount.getUserProfile().getUserName());
                }
            }
        }
        //Check if user 1 block any one in the findUserName
        for (UserAccount userAccount : Server1.allUserAccount) {
            if (userAccount.getUserProfile().getUserName().equals(userNameOne)) {
                for (String eachBlockUserOfUserOne : userAccount.getBlockList()) {
                    for (String eachUser : findUserName) {
                        if (eachUser.equals(eachBlockUserOfUserOne)) {
                            findUserName.remove(eachUser);
                        }
                    }
                }
                findUserName.remove(userNameOne);
                //DO NOT INCLUDE THAT USERNAME IN SEARCH
            }
        }
        return findUserName;
    }

    public synchronized boolean sendMessage(String sendUserName, String receiverUserName, String content, boolean isBlocked) {
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        //
        String lastLine = null;
        BufferedReader reader = null;
        boolean isGetId = false;
        int id = 0;
        try {
            // Create a BufferedReader to read from a file
            reader = new BufferedReader(new FileReader("Messages.txt"));

            // Read each line from the file
            String line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }

            // Print the last line

            if (lastLine != null) {
                String[] rowInfo = lastLine.split(",");
                id = Integer.parseInt(rowInfo[0]) + 1;
            }
            isGetId = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the BufferedReader
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isGetId) {
            // Create a message row
            String messageRow = id + ",1," + formattedDateTime + "," + sendUserName + "," + receiverUserName;
            if (isBlocked) {
                messageRow += ",blocked," + content;

            } else {
                messageRow += ",notBlocked," + content;
            }

            //Write the message to the bottom of the Message.txt
            BufferedWriter wr = null;
            try {
                wr = new BufferedWriter(new FileWriter("Messages.txt", true));
                wr.write(messageRow);
                wr.newLine();

                // Flush the data to the file
                wr.flush();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the BufferedWriter
                    if (wr != null) {
                        wr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return false;
    }


    public synchronized boolean deleteMessage(int messageID) {
        Path path = Paths.get("Messages.txt");


        try {
            // Read all lines into a List
            List<String> lines = Files.readAllLines(path);

            // Stream through the lines, replace the string, and collect the results
            List<String> replaced = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith(messageID + ",1,")) {
                    String[] row = line.split(",");
                    row[1] = String.valueOf(0);
                    line = String.join(",", row);
                }
                replaced.add(line);
            }

            // Write the lines back to the file
            Files.write(path, replaced);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Only send message to members in friendList
    // (before use this method, make sure all input should in Users friendList.
    // Only send message when otherUserName is in friendList
    // return empty string if all success, otherwirse indicate which one failed.
    public synchronized String restrictMessage(String userName, ArrayList<String> friendListList, String content) {
        List<String> failedUser = new ArrayList<>();
        for (String friend: friendListList) {
            if (!this.sendMessage(userName, friend, content, false)) {
                failedUser.add(friend);
            }
        }

        if (!failedUser.isEmpty()) {
            return "Failed to send to " + failedUser.toString();

        } else {
            return null;
        }

    }

    // printHistoryMessage()
    // Take sender's name and receiver's name as parameters to filter the message in the Messages.txt
    // that should be print out
    // The message that already deleted will not be printed in this method, but it still exist in the database
    // For the blocked message, the sender still can see it, but on the receiver side, it won't be shown

    public synchronized String printHistoryMessage(String senderName, String receiverName) {
        String filePath = "Messages.txt";
        BufferedReader br = null;
        boolean isSuccessful = true;
        String result = "";


        try {
            br = new BufferedReader(new FileReader(filePath));
            String line;
            result += "[conversationID] [ConversationTime] [Sender-Message] [if message blocked]\n";
            //System.out.println("[conversationID] [ConversationTime] [Sender-Message] [if message blocked]");
            int counter = 0;
            while ((line = br.readLine()) != null) {
                String[] array = line.split(",");

                // Step1: Merge the message that contain ","
                ArrayList<String> temp = new ArrayList<String>();
                String mergeText = "";
                if (array.length > 7) {
                    for (int i = 0; i < array.length; i++) {
                        if (i >= 6) {
                            mergeText += array[i] + ",";
                        } else {
                            temp.add(array[i]);
                        }
                    }
                    mergeText = mergeText.substring(0, mergeText.length() - 1);
                    temp.add(mergeText);
                    temp.toArray(array);
                }

                // Step2: print the message by checking:
                //    1. if message has deleted
                //    2. if the message has been blocked
                //    3. sender and receiver matched
                // All message should follow the format:
                // [conversationID] [ConversationTime] [SenderName-MessageContent] [if message blocked]
                if ((array[1].equals("1")
                        && array[3].equals(senderName)
                        && array[4].equals(receiverName)) || (array[1].equals("1")
                        && array[3].equals(receiverName)
                        && array[4].equals(senderName))) {
                    counter++;
                    result += String.format("%s %s %s: %s %s\n", array[0], array[2], array[3], array[6], array[5]);

                }
            }
            if (senderName.equals(receiverName)) {
                result = "Don't message yourself";
            }

            if (counter == 0) {
                result = "No Message Yet";
            }
            //return true;
        } catch (IOException e) {
            isSuccessful = false;
            //e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                isSuccessful = false;
                //e.printStackTrace();

            }
        }
        return result;

    }
}