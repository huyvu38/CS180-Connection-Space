import java.util.ArrayList;

/**
 * Team Project
 *
 * ServerInterface.java
 *
 * @author Huy Vu, Yanxin Yu - CS180 - L22
 * @version 28 March 2024
 */

public interface ServerInterface extends Runnable {
    boolean checkPasswordLength(String password);
    boolean checkUserNameFormat(String userName);
    boolean usernameInDatabase(String userName);
    boolean inFriendList(String userNameOne, String userNameTwo);
    boolean inBlockList(String userNameOne, String userNameTwo);
    boolean createAccount(Database database, UserAccount userAccount, String username, String password);
    boolean loginAccount(String username, String userPassword);
    boolean addFriend(String userNameOne, String userNameTwo);
    boolean deleteFriend(String userNameOne, String userNameTwo);
    boolean blockUser(String userNameOne, String userNameTwo);
    boolean unblockUser(String userNameOne, String userNameTwo);
    ArrayList<String> searchUser(String userNameOne, String word);
    boolean sendMessage(String senderUserName, String receiverUserName, String content);
    boolean deleteMessage(String sender, String receiver, String messageID);
    String printHistoryMessage(String senderName, String receiverName);
}