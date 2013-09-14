package com.appspot.hachiko_schedule.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Android端末(とかそのSIM?)に紐付いたユーザ自身の情報を取得するためのヘルパ
 */
public class LocalProfileHelper {
    private final Context context;
    public LocalProfileHelper(Context context) {
        this.context = context;
    }

    /**
     * @return この端末の電話番号，取得失敗したらNULL．
     */
    public String getMyOwnPhoneNumber() {
        TelephonyManager telephonyManager
                = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    /**
     * @return この端末に紐付いたEメールアドレス，取得失敗したらNULL
     */
    public String getMyOwnEmail() {
        // TODO: とりあえず一番うえのやつ実装しただけなので，他のも比較検討
        // http://stackoverflow.com/questions/2112965/how-to-get-the-android-devices-primary-e-mail-address
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return null;
    }

    /**
     * ユーザのProfileに紐付いた名前を取得. API14以上が必要．
     * @return displayName or null
     */
    public String getDisplayName() {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                new String[] {ContactsContract.Profile.DISPLAY_NAME},
                null,
                null,
                null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        int nameIndex = cursor
                .getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
        String name = cursor.getString(nameIndex);
        cursor.close();
        return name;
    }
}
