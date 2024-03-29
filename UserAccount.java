import java.util.ArrayList;

/**
 * Team Project
 *
 * brief_description_of_the_program
 *
 * @author Gabe Turner, Archie Baldocchi, Huy Vu, Yanxin Yu, Zander Unger, L22
 * @version 28 March 2024
 */
public class UserAccount {

    private Profile userProfile;
    private ArrayList<String> friendList;
    private ArrayList<String> blockList;

    public UserAccount(Profile userProfile) {
        this.userProfile = userProfile;
        this.friendList = new ArrayList<>();
        this.blockList = new ArrayList<>();
    }


    public Profile getUserProfile() {
        return userProfile;
    }
    public void setUserProfile(Profile userProfile) {
        this.userProfile = userProfile;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    public ArrayList<String> getBlockList() {
        return blockList;
    }

    public void setBlockList(ArrayList<String> blockList) {
        this.blockList = blockList;
    }
    public String toString() {
        String friend = "";
        for (String eachFriendUserName : this.friendList) {
            friend = friend + eachFriendUserName + ",";
        }
        String blockUser = "";
        for (String eachBlockUserName : this.blockList) {
            blockUser = blockUser + eachBlockUserName + ",";
        }
        return this.userProfile + ";" + "FriendList: ["  + friend + "];BlockList: [" + blockUser + "]";
    }
}