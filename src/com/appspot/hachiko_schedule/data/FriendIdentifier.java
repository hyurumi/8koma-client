package com.appspot.hachiko_schedule.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Preconditions;

/**
 * サーバとの通信に使われる，友達を識別するためのクラス
 */
public class FriendIdentifier implements Parcelable {
    long hachikoId;
    String email;
    String name;

    public FriendIdentifier(long hachikoId, String email, String name) {
        Preconditions.checkNotNull(name);
        this.hachikoId = hachikoId;
        this.email = email;
        this.name = name;
    }

    public long getHachikoId() {
        return hachikoId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(hachikoId);
        dest.writeString(email);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FriendIdentifier> CREATOR = new Creator<FriendIdentifier>() {

        @Override
        public FriendIdentifier createFromParcel(Parcel source) {
            return new FriendIdentifier(
                    source.readLong(),
                    source.readString(),
                    source.readString()
            );
        }

        @Override
        public FriendIdentifier[] newArray(int size) {
            return new FriendIdentifier[size];
        }
    };
}
