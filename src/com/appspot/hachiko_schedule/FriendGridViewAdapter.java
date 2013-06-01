package com.appspot.hachiko_schedule;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Point;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that stands between cursor from Contacts and our own GridView.
 */
public class FriendGridViewAdapter extends BaseAdapter {
    private static final int NUM_OF_COLUMNS = 4;

    private Context context;
    private List<Entry> entries;
    private Set<Integer> positionOfIconsWithFilter = new HashSet<Integer>();

    public FriendGridViewAdapter(Context context) {
        this.context = context;
        ContactManager contactManager = new ContactManager(context);
        Cursor cursor = contactManager.queryAllFriends();
        if (cursor == null) {
            throw new IllegalStateException("cursor must not be null");
        }

        entries = new ArrayList<Entry>();
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbnailIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        while( cursor.moveToNext()){
            String displayName = cursor.getString(nameIndex);
            if (displayName.matches("^[a-zA-Z0-9_\\+@\\/\\(\\)\\-\\.\\s]+$")) {
                continue;
            }
            String uriString = cursor.getString(thumbnailIndex);
            entries.add(
                    new Entry(cursor.getString(nameIndex),
                    uriString == null ? null : Uri.parse(uriString)));
        }
        cursor.close();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Entry getItemById(long id) {
        return entries.get((int) id);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater viewInflater =
                    (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = viewInflater.inflate(R.layout.grid_item_friend, null);
        Entry entry = entries.get(position);
        ((TextView) view.findViewById(R.id.friend_name)).setText(entry.displayName);
        ((ImageView) view.findViewById(R.id.friend_picture)).setImageURI(entry.photoUri);
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getSize(displaySize);
        view.setMinimumHeight(displaySize.x / NUM_OF_COLUMNS - 10);
        if (positionOfIconsWithFilter.contains(position)) {
            applyFilterToIcon(true, view, position);
        }
        return view;
    }

    public void applyFilterToIcon(boolean apply, View wrapperView, int position) {
        ImageView imageView = (ImageView) (wrapperView.findViewById(R.id.friend_picture));
        if (apply) {
            imageView.setColorFilter(new LightingColorFilter(Color.GRAY, 0));
            positionOfIconsWithFilter.add(position);
        } else {
            imageView.clearColorFilter();
            positionOfIconsWithFilter.remove(position);
        }
    }

    public static class Entry {
        private String displayName;
        private Uri photoUri;

        public Entry(String displayName, Uri photoUri) {
            this.displayName = displayName;
            this.photoUri = photoUri;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Uri getPhotoUri() {
            return photoUri;
        }
    }
}
