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
import com.appspot.hachiko_schedule.data.FriendGroup;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.data.FriendOrGroup;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendsAdapter extends ArrayAdapter<FriendOrGroup> {
    private LayoutInflater inflater;
    private Set<String> filteredItem;
    private int layoutResourceId;
    private List<FriendOrGroup> entries;
    private UserTableHelper userTableHelper;

    public FriendsAdapter(Context context, int resource, List<FriendOrGroup> entries, Set<String> filteredItem) {
        super(context, resource, entries);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        userTableHelper = new UserTableHelper(context);
        this.layoutResourceId = resource;
        this.filteredItem = filteredItem;
        this.entries = entries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layoutResourceId, null);
            Typeface fontForAnswer = Typeface.createFromAsset(
                    getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
            ((TextView) convertView.findViewById(R.id.icon_check)).setTypeface(fontForAnswer);
        }
        FriendOrGroup item = getItem(position);
        ((TextView) convertView.findViewById(R.id.friend_name)).setText(item.getDisplayName());
        ImageView pictureView = (ImageView) convertView.findViewById(R.id.friend_picture);
        if (item.getPhotoUri() == null) {
            pictureView.setImageDrawable(
                    getContext().getResources().getDrawable(
                            (item instanceof FriendItem) ? R.drawable.ic_contact_picture : R.drawable.ic_action_group));
        } else {
            pictureView.setImageURI(item.getPhotoUri());
        }
        if (item instanceof FriendItem) {
            setFriendToView((FriendItem) item, convertView);
        } else {
            setGroupToView((FriendGroup) item, convertView);
        }
        applyFilter(filteredItem.contains(item.getDisplayName()), convertView);
        return convertView;
    }

    private void setFriendToView(FriendItem item, View convertView) {
        String emailOrHachikoUser;
        if (userTableHelper.isHachikoUser(item.getLocalContactId())) {
            emailOrHachikoUser = getContext().getResources().getString(R.string.hachiko_user);
        } else {
            emailOrHachikoUser = userTableHelper.queryPrimaryEmail(item.getLocalContactId());
        }
        ((TextView) convertView.findViewById(R.id.friend_email)).setText(emailOrHachikoUser);
    }

    private void setGroupToView(FriendGroup item, View convertView) {
        Set<String> names = new HashSet<String>();
        for (FriendItem member: item.getMembers()) {
            names.add(member.getDisplayName());
        }
        ((TextView) convertView.findViewById(R.id.friend_email)).setText(Joiner.on(",").join(names));
    }

    public CharSequence getNameTextFromItem(View v) {
        return ((TextView) v.findViewById(R.id.friend_name)).getText();
    }

    /**
     * あるViewがクリックされたことを知らせる
     *
     * @return クリックの結果要素が選択された状態になればtrue
     */
    public synchronized boolean notifySelect(View view, int position) {
        String key = ((TextView) view.findViewById(R.id.friend_name)).getText().toString();
        boolean isSelected = filteredItem.contains(key);
        applyFilter(!isSelected, view);
        return !isSelected;
    }

    public synchronized void unselectByName(String name) {
        if (filteredItem.contains(name)) {
            filteredItem.remove(name);
        }
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

    /**
     * @return 選択されている友達を返す. グループが選択されているときは，そのグループのメンバに対応する友達を展開
     * して返す．
     */
    public Collection<FriendItem> getSelectedEntries() {
        Set<FriendItem> friends = new HashSet<FriendItem>();
        Collection<FriendOrGroup> selected = Collections2.filter(entries, new Predicate<FriendOrGroup>() {
            @Override
            public boolean apply(FriendOrGroup entry) {
                return filteredItem.contains(entry.getDisplayName());
            }
        });
        for (FriendOrGroup item: selected) {
            if (item instanceof FriendItem) {
                friends.add((FriendItem) item);
            } else {
                friends.addAll(((FriendGroup) item).getMembers());
            }
        }
        return friends;
    }
}
