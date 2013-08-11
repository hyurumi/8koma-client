package com.appspot.hachiko_schedule.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Preconditions;

/**
 * サーバとの通信に使われる，友達を識別するためのクラス
 */
public class FriendIdentifier implements Parcelable {
    long id;
    String name;

    public FriendIdentifier(long id, String name) {
        Preconditions.checkNotNull(name);
        this.id = id;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
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
                    source.readString()
            );
        }

        @Override
        public FriendIdentifier[] newArray(int size) {
            return new FriendIdentifier[size];
        }
    };
}
