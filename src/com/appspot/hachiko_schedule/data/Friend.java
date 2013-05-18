package com.appspot.hachiko_schedule.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Preconditions;

/**
 * Data class for friend
 */
public class Friend implements Parcelable {
    String name;
    String phoneNo;
    String email;

    public Friend(String name, String phoneNo, String email) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(phoneNo);
        Preconditions.checkNotNull(email);
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNo);
        dest.writeString(email);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {

        @Override
        public Friend createFromParcel(Parcel source) {
            return new Friend(
                    source.readString(),
                    source.readString(),
                    source.readString()
            );
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
