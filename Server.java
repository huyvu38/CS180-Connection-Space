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
 * @author Huy Vu, Yanxin Yu - CS180 - L22
 * @version 28 March 2024
 */


public class Server implements ServerInterface {
    private static final int PORT = 5052;
    private Socket socket;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    // Using thread pool for better performance
    public static Database database;
    public static ArrayList<UserAccount> allUserAccount;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        database = new Database("AllUserAccount.txt");
        database.readAllUserAccount();
        //Use these arraylist for any parameter
        allUserAccount = database.getAllUserAccount();
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("A client is connected.");
                //make thread for client
                Thread client = new Thread(new Server(socket));
                threadPool.execute(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //Start whenever a user connect
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            while (true) {
                String command = reader.readLine();
                if (command.equals("Create account")) {
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
                    if (username.contains(" ") || username.contains(";") ) {
                        result = false;
                    }
                    if (password.contains(" ") || password.contains(";")) {
                        result = false;
                    }
                    if (nationality.contains(" ") || nationality.contains(";") || (nationality.isEmpty())) {
                        result = false;
                    }
                    if (job.contains(" ") || job.contains(";") || (job.isEmpty())) {
                        result = false;
                    }
                    if (hobby.contains(" ") || hobby.contains(";") || (hobby.isEmpty())) {
                        result = false;
                    }
                    int newAge = 0;
                    try {
                        newAge = Integer.parseInt(age);
                    } catch (NumberFormatException e) {
                        result = false;
                    }
                    if (newAge <= 0) {
                        result = false;
                    }
                    //If the user enter all valid information -> the result still true
                    //Then check if the username is valid to create a new Profile
                    Profile newUserProfile = new Profile(username, password, newAge, gender, nationality, job, hobby);
                    UserAccount newUserAccount = new UserAccount(newUserProfile);

                    //if the result is still true -> send back to the client that account create successfully
                    if (result) {
                        if (usernameInDatabase(username) == false) {
                            if (createAccount(database, newUserAccount, username, password)) {
                                result = true;
                            } else {
                                result = false;
                            }
                        } else {
                            result = false;
                        }
                    }
                    if (result) {
                        writer.write("Create account successfully.");
                        writer.println();
                        writer.flush();
                    } else {
                        writer.write("The account is already exist or you enter wrong information.");
                        writer.println();
                        writer.flush();
                    }
                }
                if (command.equals("Log in")) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    //Log in success
                    if (loginAccount(username, password)) {
                        writer.write("Log in successfully");
                        writer.println();
                        writer.flush();
                        for (UserAccount userAccount: allUserAccount) {
                            if (userAccount.getUserProfile().getUsername().equals(username)) {
                                writer.write(userAccount.getUserProfile().getUsername());
                                writer.println();
                                writer.write(userAccount.getUserProfile().getPassword());
                                writer.println();
                                int age = userAccount.getUserProfile().getAge();
                                writer.write(String.valueOf(age));
                                writer.println();
                                writer.write(userAccount.getUserProfile().getGender());
                                writer.println();
                                writer.write(userAccount.getUserProfile().getNationality());
                                writer.println();
                                writer.write(userAccount.getUserProfile().getJob());
                                writer.println();
                                writer.write(userAccount.getUserProfile().getHobby());
                                writer.println();
                                writer.flush();
                            }
                        }
                        while (true) {
                            String choice = reader.readLine();
                            if (choice.equals("Search user")) {
                                String word = reader.readLine();
                                ArrayList<String> allFindUser = searchUser(username, word);
                                if (allFindUser.size() == 0) {
                                    writer.write("Can not find anyone");
                                    writer.println();
                                } else {
                                    writer.write("Find the following users");
                                    writer.println();
                                    for (String user : allFindUser) {
                                        writer.write(user);
                                        writer.println();
                                    }
                                    writer.write(" ");
                                    writer.println();
                                }
                                writer.flush();
                            }
                            if (choice.equals("Get friend list")) {
                                for (UserAccount userAccount : allUserAccount) {
                                    if (userAccount.getUserProfile().getUsername().equals(username)) {
                                        ArrayList<String> friendlist = userAccount.getFriendList();
                                        if (friendlist.size() == 0) {
                                            writer.write("Your friend list is empty");
                                            writer.println();
                                        } else {
                                            writer.write("Find the following friends");
                                            writer.println();
                                            for (String friend : friendlist) {
                                                writer.write(friend);
                                                writer.println();
                                            }
                                            writer.write(" ");
                                            writer.println();
                                        }
                                        writer.flush();
                                        break;
                                    }
                                }
                            }
                            if (choice.equals("Get block list")) {
                                for (UserAccount userAccount : allUserAccount) {
                                    if (userAccount.getUserProfile().getUsername().equals(username)) {
                                        ArrayList<String> blocklist = userAccount.getBlockList();
                                        if (blocklist.size() == 0) {
                                            writer.write("Your block list is empty");
                                            writer.println();
                                        } else {
                                            writer.write("Find the following block user");
                                            writer.println();
                                            for (String block : blocklist) {
                                                writer.write(block);
                                                writer.println();
                                            }
                                            writer.write(" ");
                                            writer.println();
                                        }
                                        writer.flush();
                                        break;
                                    }
                                }
                            }
                            if (choice.equals("Edit profile")) {
                                boolean result = true;
                                String passWord = reader.readLine();
                                String age = reader.readLine();
                                String nationality = reader.readLine();
                                String job = reader.readLine();
                                String hobby = reader.readLine();
                                if (passWord.contains(" ") || passWord.contains(";")) {
                                    result = false;
                                }
                                if (nationality.contains(" ") || nationality.contains(";") || (nationality.isEmpty())) {
                                    result = false;
                                }
                                if (job.contains(" ") || job.contains(";") || (job.isEmpty())) {
                                    result = false;
                                }
                                if (hobby.contains(" ") || hobby.contains(";") || (hobby.isEmpty())) {
                                    result = false;
                                }
                                int newAge = 0;
                                try {
                                    newAge = Integer.parseInt(age);
                                } catch (NumberFormatException e) {
                                    result = false;
                                }
                                if (newAge <= 0) {
                                    result = false;
                                }
                                if (checkPasswordLength(passWord) == false) {
                                    result = false;
                                }
                                if (result) {
                                    for (UserAccount userAccount: allUserAccount) {
                                        if (userAccount.getUserProfile().getUsername().equals(username)) {
                                            userAccount.getUserProfile().setPassword(passWord);
                                            userAccount.getUserProfile().setAge(newAge);
                                            userAccount.getUserProfile().setNationality(nationality);
                                            userAccount.getUserProfile().setJob(job);
                                            userAccount.getUserProfile().setHobby(hobby);
                                            database.saveAllUserAccount();
                                            writer.write("success");
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                } else {
                                    for (UserAccount userAccount: allUserAccount) {
                                        if (userAccount.getUserProfile().getUsername().equals(username)) {
                                            writer.write("failure");
                                            writer.println();
                                            writer.write(userAccount.getUserProfile().getPassword());
                                            writer.println();
                                            writer.write(Integer.toString(userAccount.getUserProfile().getAge()));
                                            writer.println();
                                            writer.write(userAccount.getUserProfile().getNationality());
                                            writer.println();
                                            writer.write(userAccount.getUserProfile().getJob());
                                            writer.println();
                                            writer.write(userAccount.getUserProfile().getHobby());
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                }
                            }
                            if (choice.equals("Action")) {
                                String specificAction = reader.readLine();
                                if (specificAction.equals("Add friend")) {
                                    String addFriendUserName = reader.readLine();
                                    if (addFriend(username, addFriendUserName)) {
                                        database.saveAllUserAccount();
                                        writer.write("Add friend successfully");
                                    } else {
                                        writer.write("You can not add that user");
                                    }
                                    writer.println();
                                    writer.flush();
                                }
                                if (specificAction.equals("Unfriend")) {
                                    String unfriendUserName = reader.readLine();
                                    if (deleteFriend(username, unfriendUserName)) {
                                        database.saveAllUserAccount();
                                        writer.write("Unfriend successfully");
                                    } else {
                                        writer.write("You can not unfriend that user");
                                    }
                                    writer.println();
                                    writer.flush();
                                }
                                if (specificAction.equals("Block user")) {
                                    String blockUserName = reader.readLine();
                                    if (blockUser(username, blockUserName)) {
                                        writer.write("Block successfully");
                                        //If both users are friend then delete after block
                                        deleteFriend(username, blockUserName);
                                        database.saveAllUserAccount();
                                    } else {
                                        writer.write("You can not block that user");
                                    }
                                    writer.println();
                                    writer.flush();
                                }
                                if (specificAction.equals("Unblock user")) {
                                    String unblockUserName = reader.readLine();
                                    if (unblockUser(username, unblockUserName)) {
                                        database.saveAllUserAccount();
                                        writer.write("Unblock successfully");
                                    } else {
                                        writer.write("You can not unblock that user");
                                    }
                                    writer.println();
                                    writer.flush();
                                }
                                if (specificAction.equals("View other user profile")) {
                                    String usernameToView = reader.readLine();
                                    if (inBlockList(username, usernameToView) ||
                                            inBlockList(usernameToView, username) ||
                                            (usernameInDatabase(usernameToView) == false)) {
                                        writer.write("Can not view that user profile");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        writer.write("Click to the information that you want to see");
                                        writer.println();
                                        writer.flush();
                                        String viewOtherProfileChoice = reader.readLine();
                                        for (UserAccount userAccount : allUserAccount) {
                                            if (userAccount.getUserProfile().getUsername().equals(usernameToView)) {
                                                if (viewOtherProfileChoice.equals("Age")) {
                                                    int age = userAccount.getUserProfile().getAge();
                                                    String newAge = Integer.toString(age);
                                                    writer.write(newAge);
                                                }
                                                if (viewOtherProfileChoice.equals("Gender")) {
                                                    writer.write(userAccount.getUserProfile().getGender());
                                                }
                                                if (viewOtherProfileChoice.equals("Nationality")) {
                                                    writer.write(userAccount.getUserProfile().getNationality());
                                                }
                                                if (viewOtherProfileChoice.equals("Job")) {
                                                    writer.write(userAccount.getUserProfile().getJob());
                                                }
                                                if (viewOtherProfileChoice.equals("Hobby")) {
                                                    writer.write(userAccount.getUserProfile().getHobby());
                                                }
                                                writer.println();
                                                writer.flush();
                                            }
                                        }
                                    }
                                }
                                if (specificAction.equals("Message")) {
                                    String receiver = reader.readLine();
                                    String testReceiver = null;

                                    for (UserAccount userAccount: allUserAccount) {
                                        if (userAccount.getUserProfile().getUsername().equals(receiver)) {
                                            testReceiver = receiver;
                                        }
                                    }
                                    if (testReceiver == null) {
                                        writer.write("the User not exist");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        boolean isBlocked = false;
                                        //user1 blocks user2
                                        if (inBlockList(username, receiver)) {
                                            isBlocked = true;
                                        }
                                        //user2 blocks user1
                                        if (inBlockList(receiver, username)) {
                                            isBlocked = true;
                                        }
                                        if (isBlocked) {
                                            writer.write("the User not exist");
                                            writer.println();
                                            writer.flush();
                                        } else {
                                            writer.write("the User exist");
                                            writer.println();
                                            writer.write(printHistoryMessage(username, receiver));
                                            writer.println();
                                            writer.write("END_OF_MESSAGE");
                                            writer.println();
                                            writer.flush();
                                            while (true) {
                                                String messageAction = reader.readLine();
                                                if (messageAction.equals("Send Message")) {
                                                    String messageContent = reader.readLine();
                                                    sendMessage(username, receiver, messageContent);
                                                    writer.write(printHistoryMessage(username, receiver));
                                                    writer.println();
                                                    writer.write("END_OF_MESSAGE");
                                                    writer.println();
                                                    writer.flush();
                                                }
                                                if (messageAction.equals("Delete Message")) {
                                                    String conversationID = reader.readLine();
                                                    boolean deleteMessageResult = deleteMessage(username, receiver, conversationID);
                                                    if (deleteMessageResult) {
                                                        writer.write("Delete message successfully");
                                                        writer.println();
                                                        writer.write(printHistoryMessage(username, receiver));
                                                        writer.println();
                                                        writer.write("END_OF_MESSAGE");
                                                        writer.println();
                                                    } else {
                                                        writer.write("Delete message failure");
                                                        writer.println();
                                                    }
                                                    writer.flush();
                                                }
                                                if (messageAction.equals("Message Frame is closing")) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            //Log Out
                            if (choice.equals("Log out")) {
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
                if (command.equals("Exit the app")) {
                    socket.close();
                    writer.close();
                    reader.close();
                    System.out.println("A client is disconnected");
                    break;
                }
            }
        } catch (Exception f) {
            System.out.println("A client is disconnected");
        }
    }

    //Already check if contain space or semicolon
    public synchronized boolean checkPasswordLength(String password) {
        return password.length() >= 6;
    }
    public synchronized boolean checkUserNameFormat(String userName) {
        return userName.length() >= 4;
    }
    public synchronized boolean usernameInDatabase(String userName) {
        //From a list of user profile, find the specific username
        for (UserAccount eachUserAccount : allUserAccount) {
            if (eachUserAccount.getUserProfile().getUsername().equals(userName)) {
                return true; //User exist in the database
            }
        }
        return false; // User doesn't exist in the database
    }
    public synchronized boolean inFriendList(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            for (UserAccount userAccount : allUserAccount) {
                //Find the account of user1
                //If we find username2 in friend list of username1,
                // we don't have to check username1 in friendlist of username2
                if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
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
    public synchronized boolean inBlockList(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            for (UserAccount userAccount : allUserAccount) {
                //Find the account of user1 and check if the user1 block user2
                if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                    //Check the block list of user1
                    for (String blockUser : userAccount.getBlockList()) {
                        if (blockUser.equals(userNameTwo)) {
                            return true; //Return true if username2 in blocklist of username1
                        }
                    }
                }
            }
        }
        return false;
        //The method return false if user1 do not block user2
    }
    public synchronized boolean createAccount(Database data, UserAccount userAccount,
                                              String username, String password) {
        if (checkUserNameFormat(username) && checkPasswordLength(password)) {
            ArrayList<UserAccount> temp = data.getAllUserAccount();
            temp.add(userAccount);
            data.setAllUserAccount(temp);
            data.saveAllUserAccount();
            return true;
        }
        return false;
    }

    public synchronized boolean loginAccount(String username, String userPassword) {
        if (usernameInDatabase(username)) {
            for (UserAccount eachUserAccount: allUserAccount) {
                if (eachUserAccount.getUserProfile().getUsername().equals(username)) {
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
            if (userNameOne.equals(userNameTwo)) {
                return false; //can not add their own account
            }
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
                for (UserAccount userAccount : allUserAccount) {
                    //Find the friendlist of user1
                    if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                        ArrayList<String> friendListUserOne = userAccount.getFriendList();
                        //Add the user2 to friend list of user1
                        friendListUserOne.add(userNameTwo);
                        userAccount.setFriendList(friendListUserOne);
                    }
                    //Find the friendlist of user2
                    if (userAccount.getUserProfile().getUsername().equals(userNameTwo)) {
                        ArrayList<String> friendListUserTwo = userAccount.getFriendList();
                        //Add the user1 to friend list of user2
                        friendListUserTwo.add(userNameOne);
                        userAccount.setFriendList(friendListUserTwo);
                    }
                }
                return true; //add friend success
            }
        }
        return false; //If one of two username not in the database
    }
    public synchronized boolean deleteFriend(String userNameOne, String userNameTwo) {
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (inFriendList(userNameOne, userNameTwo)) { //both users are friend
                for (UserAccount userAccount : allUserAccount) {
                    //Find the friendlist of user1
                    if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                        ArrayList<String> friendListUserOne = userAccount.getFriendList();
                        //remove the user2 to friend list of user1
                        friendListUserOne.remove(userNameTwo);
                        userAccount.setFriendList(friendListUserOne);
                    }
                    //Find the friendlist of user2
                    if (userAccount.getUserProfile().getUsername().equals(userNameTwo)) {
                        ArrayList<String> friendListUserTwo = userAccount.getFriendList();
                        //remove the user1 to friend list of user2
                        friendListUserTwo.remove(userNameOne);
                        userAccount.setFriendList(friendListUserTwo);
                    }
                }
                return true; // remove friend successfully
            }
        }
        return false; //If one of two username not in the database or not in the friendlist of each other
    }
    public synchronized boolean blockUser(String userNameOne, String userNameTwo) {
        //Check if the two usernames is in the SocialMedia database
        if (usernameInDatabase(userNameOne) && usernameInDatabase(userNameTwo)) {
            if (userNameOne.equals(userNameTwo)) {
                return false; //can not block their own account
            }
            if (inBlockList(userNameOne, userNameTwo)) {
                return false; //User1 already block user2
            }
            if (inBlockList(userNameTwo, userNameOne)) {
                return false; //User2 block user1 so user1 cannot block user2
            }
            for (UserAccount userAccount : allUserAccount) {
                //Find the blocklist of user1
                if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                    ArrayList<String> blockListUserOne = userAccount.getBlockList();
                    //add the user2 to block list of user1
                    blockListUserOne.add(userNameTwo);
                    userAccount.setBlockList(blockListUserOne);
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
                for (UserAccount userAccount : allUserAccount) {
                    //Find the blocklist of user1
                    if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                        ArrayList<String> blockListUserOne = userAccount.getBlockList();
                        //remove the user2 from the block list of user1
                        blockListUserOne.remove(userNameTwo);
                        userAccount.setBlockList(blockListUserOne);
                        return true;
                    }
                }
            }
        }
        return false; //if one of the username not valid or user2 not in block list of user1
    }


    //User1 finds user2
    public synchronized ArrayList<String> searchUser(String userNameOne, String word) {
        ArrayList<String> findUserName = new ArrayList<>();
        //Check if no account block user1
        for (UserAccount userAccount : allUserAccount) {
            if (userAccount.getUserProfile().getUsername().contains(word)) {
                boolean userOneIsBlocked = false;
                for (String blockListOfUserTwo : userAccount.getBlockList()) {
                    //That user not block user1
                    if (blockListOfUserTwo.equals(userNameOne) == true) {
                        userOneIsBlocked = true;
                        break;
                    }
                }
                if (userOneIsBlocked == false) {
                    findUserName.add(userAccount.getUserProfile().getUsername());
                }
            }
        }
        //Check if user 1 block any one in the findUserName
        for (UserAccount userAccount : allUserAccount) {
            if (userAccount.getUserProfile().getUsername().equals(userNameOne)) {
                for (String eachBlockUserOfUserOne : userAccount.getBlockList()) {
                    findUserName.remove(eachBlockUserOfUserOne);
                }
                findUserName.remove(userNameOne);
                //DO NOT INCLUDE THAT USERNAME IN SEARCH
            }
        }
        return findUserName;
    }

    public synchronized boolean sendMessage(String senderUserName, String receiverUserName, String content) {
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        String lastLine = null;
        boolean isGetId = false;
        int id = 0;
        try {
            // Create a BufferedReader to read from a file
            BufferedReader reader = new BufferedReader(new FileReader("Messages.txt"));
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
        }
        if (isGetId) {
            // Create a message row
            String messageRow = id + "," + formattedDateTime + "," + senderUserName +
                    "," + receiverUserName + "," + content;
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
            }
        }
        //Can not send message
        return false;
    }
    public synchronized boolean deleteMessage(String sender, String receiver, String messageID) {
        Path path = Paths.get("Messages.txt");
        boolean deleteMessageResult = false;
        try {
            // Read all lines into a List
            List<String> lines = Files.readAllLines(path);

            // Stream through the lines, replace the string, and collect the results
            List<String> replaced = new ArrayList<>();
            int count = 0;
            for (String line : lines) {
                String[] element = line.split(",");
                if (!element[0].equals(messageID)) {
                    replaced.add(line);
                    count++;
                } else {
                    if ((!element[2].equals(sender)) || (!element[3].equals(receiver))) {
                        replaced.add(line);
                        count++;
                    }
                }
            }
            // Write the lines back to the file
            Files.write(path, replaced);
            if (count != lines.size()) {
                deleteMessageResult = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deleteMessageResult;
    }
    // printHistoryMessage()
    // Take sender's name and receiver's name as parameters to filter the message in the Messages.txt
    // that should be print out
    // The message that already deleted will not be printed in this method, but it still exist in the database
    // For the blocked message, the sender still can see it, but on the receiver side, it won't be shown
    public synchronized String printHistoryMessage(String senderName, String receiverName) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("Messages.txt"));
            String line;
            result += "[ConversationID] [ConversationTime] [Sender] [Message]\n";
            while ((line = br.readLine()) != null) {
                String[] array = line.split(",");

                // Step1: Merge the message that contain ","
                ArrayList<String> temp = new ArrayList<>();
                String mergeText = "";
                if (array.length > 5) {
                    for (int i = 0; i < array.length; i++) {
                        if (i >= 4) {
                            mergeText += array[i] + ",";
                        } else {
                            temp.add(array[i]);
                        }
                    }
                    //Delete the last ,
                    mergeText = mergeText.substring(0, mergeText.length() - 1);
                    temp.add(mergeText);
                    temp.toArray(array);
                }

                // Step2: print the message by checking:
                //    1. if message has deleted
                //    2. sender and receiver matched
                // All message should follow the format:
                // [ConversationID] [ConversationTime] [SenderName] [Message Content]
                if ((array[2].equals(senderName) && array[3].equals(receiverName))
                        || (array[2].equals(receiverName) && array[3].equals(senderName))) {
                    result += String.format("%s %s %s %s\n", array[0], array[1], array[2], array[4]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}