package tk.hachikoma.data;

import android.content.Context;
import tk.hachikoma.db.UserTableHelper;
import tk.hachikoma.prefs.HachikoPreferences;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Collection;

/**
 * 予定を表すデータクラス
 */
public class Plan {
    private final long planId;
    private final String title;
    private final boolean isFixed;
    private final long ownerId;

    public Plan(long planId, String title, long ownerId, boolean isFixed) {
        this.planId = planId;
        this.title = title;
        this.isFixed = isFixed;
        this.ownerId = ownerId;
    }

    public long getPlanId() {
        return planId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName(Context context) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        Collection<String> names
                = userTableHelper.getFriendsNameForHachikoIds(Arrays.asList(new Long[]{ownerId}));
        if (names.size() > 0) {
            return Iterables.get(names, 0);
        } else if (HachikoPreferences.getMyHachikoId(context) == ownerId) {
            return "あなた";
        }
        return "";
    }

    public boolean isHost(Context context) {
        return HachikoPreferences.getMyHachikoId(context) == ownerId;
    }
}
