package com.appspot.hachiko_schedule.data;

import com.google.common.base.Preconditions;

/**
 * Data class for friend
 */
public class Friend {
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
}
