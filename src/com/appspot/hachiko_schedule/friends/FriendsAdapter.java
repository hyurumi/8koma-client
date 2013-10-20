package com.appspot.hachiko_schedule.friends;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that stands between cursor from Contacts and our own GridView.
 */
public class FriendsAdapter extends ArrayAdapter<FriendItem> {
    private LayoutInflater inflater;
    private Set<String> filteredItem = new HashSet<String>();
    private List<FriendItem> entries;
    private UserTableHelper userTableHelper;

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
            Typeface fontForAnswer = Typeface.createFromAsset(
                    getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
            ((TextView) convertView.findViewById(R.id.icon_check)).setTypeface(fontForAnswer);
        }
        FriendItem item = getItem(position);
        ((TextView) convertView.findViewById(R.id.friend_name)).setText(item.getDisplayName());
        ImageView pictureView = (ImageView) convertView.findViewById(R.id.friend_picture);
        if (item.getPhotoUri() == null) {
            pictureView.setImageDrawable(
                    getContext().getResources().getDrawable(R.drawable.ic_contact_picture));
        } else {
            pictureView.setImageURI(item.getPhotoUri());
        }

        String emailOrHachikoUser;
        if (userTableHelper.isHachikoUser(item.getLocalContactId())) {
            emailOrHachikoUser = getContext().getResources().getString(R.string.hachiko_user);
        } else {
            emailOrHachikoUser = userTableHelper.queryPrimaryEmail(item.getLocalContactId());
        }
        ((TextView) convertView.findViewById(R.id.friend_email)).setText(emailOrHachikoUser);
        applyFilter(filteredItem.contains(item.getDisplayName()), convertView);
        return convertView;
    }

    /**
     * あるViewがクリックされたことを知らせる
     *
     * @return クリックの結果要素が選択された状態になればtrue
     */
    public boolean notifySelect(View view, int position) {
        String key = ((TextView) view.findViewById(R.id.friend_name)).getText().toString();
        boolean isSelected = filteredItem.contains(key);
        applyFilter(!isSelected, view);
        return !isSelected;
    }

    private void applyFilter(boolean apply, View wrapperView) {
        View nameView = wrapperView.findViewById(R.id.friend_name_container);
        TextView textView = (TextView) wrapperView.findViewById(R.id.friend_name);
        TextView iconView = (TextView)wrapperView.findViewById(R.id.icon_check);
        if (apply) {
            nameView.setBackgroundResource(R.color.background_white);
            textView.setTypeface(null, Typeface.BOLD);
            iconView.setVisibility(View.VISIBLE);
            filteredItem.add(textView.getText().toString());
        } else {
            nameView.setBackgroundResource(R.color.background_color_gray);
            textView.setTypeface(null, Typeface.NORMAL);
            iconView.setVisibility(View.GONE);
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
}
