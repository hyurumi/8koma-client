package com.appspot.hachiko_schedule.data;

import android.net.Uri;

/**
 * 友達を表すアイコン画像つきデータクラス
 */
public class FriendItem extends FriendOrGroup {
    private long localContactId;
    private String emailAddress;

    public FriendItem (long localContactId, String displayName, Uri photoUri, String emailAddress) {
        super(displayName, photoUri);
        this.localContactId = localContactId;
        this.emailAddress = emailAddress;
    }

    public long getLocalContactId() {
        return localContactId;
    }


    public String getEmailAddress() {
        return emailAddress;
    }
}
