package com.appspot.hachiko_schedule.friends;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.*;

/**
 * Class that stands between cursor from Contacts and our own GridView.
 */
public class FriendsAdapter extends ArrayAdapter<FriendItem> {
    public static interface OnFriendItemSelectionChangeListener {
        void onSelectionChange(int position, String itemName, boolean selected);
    }
    private LayoutInflater inflater;
    private Set<String> filteredItem = new HashSet<String>();
    private List<FriendItem> entries;
    private UserTableHelper userTableHelper;
    private OnFriendItemSelectionChangeListener onFriendItemSelectionChangeListener;

    public FriendsAdapter(Context context, int resource, List<FriendItem> entries) {
        super(context, resource, entries);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        userTableHelper = new UserTableHelper(context);
        this.entries = entries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_friend, null);
            convertView.setOnTouchListener(new OnFriendTouchListener());
        }
        FriendItem item = getItem(position);
        ((TextView) convertView.findViewById(R.id.friend_name)).setText(item.getDisplayName());
        ((ImageView) convertView.findViewById(R.id.friend_picture)).setImageURI(item.getPhotoUri());
        String emailOrHachikoUser;
        if (userTableHelper.isHachikoUser(item.getLocalContactId())) {
            emailOrHachikoUser = getContext().getResources().getString(R.string.hachiko_user);
        } else {
            emailOrHachikoUser = userTableHelper.queryPrimaryEmail(item.getLocalContactId());
        }
        ((TextView) convertView.findViewById(R.id.friend_email)).setText(emailOrHachikoUser);
        applyFilterToIcon(filteredItem.contains(item.getDisplayName()), convertView);
        convertView.setTag(R.string.tag_friend_item, position);
        return convertView;
    }

    public void setOnFriendItemSelectedListener(
            OnFriendItemSelectionChangeListener onFriendItemSelectedListener) {
        this.onFriendItemSelectionChangeListener = onFriendItemSelectedListener;
    }

    public void applyFilterToIcon(boolean apply, View wrapperView) {
        ImageView imageView = (ImageView) wrapperView.findViewById(R.id.friend_picture);
        View nameView = wrapperView.findViewById(R.id.friend_name_container);
        TextView textView = (TextView) wrapperView.findViewById(R.id.friend_name);
        if (apply) {
            imageView.setColorFilter(new LightingColorFilter(Color.GRAY, 0));
            nameView.setBackgroundColor(Color.GRAY);
            filteredItem.add(textView.getText().toString());
        } else {
            imageView.clearColorFilter();
            nameView.setBackgroundColor(Color.WHITE);
            filteredItem.remove(textView.getText().toString());
        }
    }

    public Collection<FriendItem> getSelectedEntries() {
        return Collections2.filter(entries, new Predicate<FriendItem>() {
            @Override
            public boolean apply(FriendItem entry) {
                return filteredItem.contains(entry.getDisplayName());
            }
        });
    }

    private class OnFriendTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                String name = ((TextView) view.findViewById(R.id.friend_name)).getText().toString();
                boolean isSelected = filteredItem.contains(name);
                applyFilterToIcon(!isSelected, view);
                if (onFriendItemSelectionChangeListener != null) {
                    onFriendItemSelectionChangeListener.onSelectionChange(
                            (Integer) view.getTag(R.string.tag_friend_item), name, !isSelected);
                }
            }
            return false;
        }
    }
}
