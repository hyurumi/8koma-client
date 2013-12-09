package tk.hachikoma.data;

import android.net.Uri;

import java.util.Set;

/**
 * 何人かの友人で構成されるグループを表すデータクラス
 */
public class FriendGroup extends FriendOrGroup{
    private final int id;
    private final Set<FriendItem> members;

    public FriendGroup(int groupId, String groupName, Uri iconUri, Set<FriendItem> members) {
        super(groupName, iconUri);
        this.id = groupId;
        this.members = members;
    }

    public String getGroupName() {
        return super.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + members.size() + "人)";
    }

    public int getId() {
        return id;
    }

    public Set<FriendItem> getMembers() {
        return members;
    }
}
