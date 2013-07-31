package com.appspot.hachiko_schedule.data;

import com.appspot.hachiko_schedule.R;

/**
 * イベントの種類
 * カフェとか焼肉とか
 */
public enum EventCategory {
    COFFEE(R.drawable.cafe),
    GRILLED_BEEF(R.drawable.grilled_beaf),
    // TODO: find appropriate icon
    UNKNOWN(0);

    private final int iconIdentifier;

    private EventCategory(int iconIdentifier) {
        this.iconIdentifier = iconIdentifier;
    }

    /**
     * @return 自身に対応するイベントアイコンのリソースID
     */
    public int getIconResourceId() {
        if (this.equals(UNKNOWN)) {
            throw new UnsupportedOperationException("No icon for type unknown");
        }
        return iconIdentifier;
    }
}
