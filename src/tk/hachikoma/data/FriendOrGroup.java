package tk.hachikoma.data;

import android.net.Uri;

/**
 * FriendとGroupに共通する部分を表す基底クラス
 */
public abstract class FriendOrGroup {
    private final String displayName;
    private final Uri photoUri;

    public FriendOrGroup(String displayName, Uri photoUri) {
        this.displayName = displayName;
        this.photoUri = photoUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    // Note: toString()の値が(ArrayAdapterにデフォルト実装の)Filterでも使われる
    @Override
    public String toString() {
        return displayName;
    }
}
