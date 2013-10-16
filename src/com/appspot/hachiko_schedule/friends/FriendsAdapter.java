package com.appspot.hachiko_schedule.friends;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
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
        View view = inflater.inflate(R.layout.list_item_friend, null);
        FriendItem item = getItem(position);
        ((TextView) view.findViewById(R.id.friend_name)).setText(item.getDisplayName());
        ((ImageView) view.findViewById(R.id.friend_picture)).setImageURI(item.getPhotoUri());

        Typeface fontForAnswer = Typeface.createFromAsset( getContext().getAssets(), "fonts/fontawesome-webfont.ttf" );
        ((TextView)view.findViewById(R.id.icon_check)).setTypeface(fontForAnswer);

        String emailOrHachikoUser;
        if (userTableHelper.isHachikoUser(item.getLocalContactId())) {
            emailOrHachikoUser = getContext().getResources().getString(R.string.hachiko_user);
        } else {
            emailOrHachikoUser = userTableHelper.queryPrimaryEmail(item.getLocalContactId());
        }
        ((TextView) view.findViewById(R.id.friend_email)).setText(emailOrHachikoUser);
        applyFilterToIcon(filteredItem.contains(item.getDisplayName()), view, position);
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
        View nameView = wrapperView.findViewById(R.id.friend_name_container);
        TextView textView = (TextView) wrapperView.findViewById(R.id.friend_name);
        TextView iconView = (TextView)wrapperView.findViewById(R.id.icon_check);
        if (apply) {
            //imageView.setColorFilter(new LightingColorFilter(Color.GRAY, 0));
            nameView.setBackgroundResource(R.color.background_white);
            textView.setTypeface(null, Typeface.BOLD);
            iconView.setVisibility(View.VISIBLE);
            filteredItem.add(textView.getText().toString());
        } else {
            //imageView.clearColorFilter();
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
