package com.appspot.hachiko_schedule;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.*;

/**
 * Class that stands between cursor from Contacts and our own GridView.
 */
public class FriendsAdapter extends ArrayAdapter<FriendsAdapter.Entry> {
    private LayoutInflater inflater;
    private Set<String> filteredItem = new HashSet<String>();
    private List<Entry> entries;

    public FriendsAdapter(Context context, int resource, List<Entry> entries) {
        super(context, resource, entries);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.entries = entries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_friend, null);
        Entry entry = getItem(position);
        ((TextView) view.findViewById(R.id.friend_name)).setText(entry.displayName);
        ((ImageView) view.findViewById(R.id.friend_picture)).setImageURI(entry.photoUri);
        applyFilterToIcon(filteredItem.contains(entry.displayName), view, position);
        return view;
    }

    /**
     * あるViewがクリックされたことを知らせる
     *
     * @return クリックの結果要素が選択された状態になればtrue
     */
    public boolean notifySelect(View view, int position) {
        String key = ((TextView) view.findViewById(R.id.friend_name)).getText().toString();
        boolean isSelected = filteredItem.contains(key);
        applyFilterToIcon(!isSelected, view, position);
        return !isSelected;
    }

    public void applyFilterToIcon(boolean apply, View wrapperView, int position) {
        ImageView imageView = (ImageView) wrapperView.findViewById(R.id.friend_picture);
        TextView textView = (TextView) wrapperView.findViewById(R.id.friend_name);
        if (apply) {
            imageView.setColorFilter(new LightingColorFilter(Color.GRAY, 0));
            textView.setBackgroundColor(Color.GRAY);
            filteredItem.add(textView.getText().toString());
        } else {
            imageView.clearColorFilter();
            textView.setBackgroundColor(Color.WHITE);
            filteredItem.remove(textView.getText().toString());
        }
    }

    public Collection<Entry> getSelectedEntries() {
        return Collections2.filter(entries, new Predicate<Entry>() {
            @Override
            public boolean apply(FriendsAdapter.Entry entry) {
                return filteredItem.contains(entry.displayName);
            }
        });
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

        // Note: toString()の値が(ArrayAdapterにデフォルト実装の)Filterでも使われる
        @Override
        public String toString() {
            return displayName;
        }
    }
}
