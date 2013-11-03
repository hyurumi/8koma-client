package com.appspot.hachiko_schedule.data;

import android.net.Uri;

import java.util.Set;

/**
 * 何人かの友人で構成されるグループを表すデータクラス
 */
public class FriendGroup extends FriendOrGroup{
    private final Set<FriendItem> members;

    public FriendGroup(String groupName, Uri iconUri, Set<FriendItem> members) {
        super(groupName, iconUri);
        this.members = members;
    }

    public Set<FriendItem> getMembers() {
        return members;
    }
}
