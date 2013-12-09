package tk.hachikoma.friends;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import tk.hachikoma.R;
import tk.hachikoma.data.FriendGroup;
import tk.hachikoma.data.FriendItem;
import tk.hachikoma.data.FriendOrGroup;
import tk.hachikoma.db.UserTableHelper;
import com.google.common.base.Joiner;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kazuki Nishiura
 */
public class FriendItemView extends LinearLayout implements Checkable {
    private final Typeface fontForAnswer = Typeface.createFromAsset(
            getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
    private LayoutInflater inflater
            = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    private UserTableHelper userTableHelper = new UserTableHelper(getContext());
    private boolean isChecked = false;

    FriendItemView(Context context, int layoutResourceId) {
        super(context);
        init(context, layoutResourceId);
    }

    private void init(Context context, int layoutResourceId) {
        View v = inflater.inflate(layoutResourceId, this);
        ((TextView) v.findViewById(R.id.icon_check)).setTypeface(fontForAnswer);
    }

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
        View nameView = findViewById(R.id.friend_name_container);
        TextView textView = (TextView) findViewById(R.id.friend_name);
        TextView iconView = (TextView) findViewById(R.id.icon_check);
        if (checked) {
            nameView.setBackgroundResource(R.color.background_white);
            textView.setTypeface(null, Typeface.BOLD);
            iconView.setVisibility(View.VISIBLE);
        } else {
            nameView.setBackgroundResource(R.color.background_color_gray);
            textView.setTypeface(null, Typeface.NORMAL);
            iconView.setVisibility(View.GONE);
        }
    }

    protected void setItem(FriendOrGroup item) {
        ((TextView) findViewById(R.id.friend_name)).setText(item.getDisplayName());
        ImageView pictureView = (ImageView) findViewById(R.id.friend_picture);
        if (item.getPhotoUri() == null) {
            int defaultResource = (item instanceof FriendItem)
                    ? R.drawable.ic_contact_picture : R.drawable.ic_action_group;
            pictureView.setImageDrawable(
                    getContext().getResources().getDrawable(defaultResource));
        } else {
            pictureView.setImageURI(item.getPhotoUri());
        }

        ((TextView) findViewById(R.id.friend_email)).setText(getSubText(item));
    }

    private String getSubText(FriendOrGroup item) {
        if (item instanceof FriendItem) {
            return getSubText((FriendItem) item);
        } else {
            return getSubText((FriendGroup) item);
        }
    }

    private String getSubText(FriendItem item) {
        if (userTableHelper.isHachikoUser(item.getLocalContactId())) {
            return getContext().getResources().getString(R.string.hachiko_user);
        } else {
            return userTableHelper.queryPrimaryEmail(item.getLocalContactId());
        }
    }

    private String getSubText(FriendGroup item) {
        Set<String> names = new HashSet<String>();
        for (FriendItem member: item.getMembers()) {
            names.add(member.getDisplayName());
        }
        return Joiner.on(",").join(names);
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    public String getName() {
        return ((TextView) findViewById(R.id.friend_name)).getText().toString();
    }
}
