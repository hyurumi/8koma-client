package tk.hachikoma.data;

import android.net.Uri;

/**
 * 友達を表すアイコン画像つきデータクラス
 */
public class FriendItem extends FriendOrGroup {
    private final long localContactId;
    private final String emailAddress;
    private final String phoneticName;

    public FriendItem (long localContactId, String displayName, String phoneticName, Uri photoUri,
                       String emailAddress) {
        super(displayName, photoUri);
        this.localContactId = localContactId;
        this.emailAddress = emailAddress;
        this.phoneticName = phoneticName;
    }

    public long getLocalContactId() {
        return localContactId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneticName() {
        return phoneticName;
    }

    // TODO: ArrayAdapterListViewのFilterにも使われる(ArrayAdapter)
    @Override
    public String toString() {
        if (phoneticName == null || phoneticName.length() == 0) {
            return super.toString();
        } else {
            return super.toString() + " " + phoneticName;
        }
    }
}
