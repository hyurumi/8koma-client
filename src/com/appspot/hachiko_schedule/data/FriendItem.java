package com.appspot.hachiko_schedule.data;

import android.net.Uri;

/**
 * 友達を表すアイコン画像つきデータクラス
 */
public class FriendItem {
    private String displayName;
    private Uri photoUri;
    private String emailAddress;

    public FriendItem (String displayName, Uri photoUri, String emailAddress) {
        this.displayName = displayName;
        this.photoUri = photoUri;
        this.emailAddress = emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    // Note: toString()の値が(ArrayAdapterにデフォルト実装の)Filterでも使われる
    @Override
    public String toString() {
        return displayName;
    }
}
