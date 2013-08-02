package com.appspot.hachiko_schedule.data;

import com.appspot.hachiko_schedule.R;

/**
 * イベントの種類
 * カフェとか焼肉とか
 */
public enum EventCategory {
    // 0 になってるとこはリソースが無いので，良きものをさがしてあてる
    COFFEE("カフェ", R.drawable.ic_cafe, R.drawable.cafe),
    BUSINESS("ミーティング", R.drawable.ic_business, 0),
    BEER("飲み会", R.drawable.ic_beer, 0),
    MOVIE("映画", R.drawable.ic_movie, 0),
    SCHOOL("お勉強", R.drawable.ic_school, 0),
    SHOPPING("お買い物", R.drawable.ic_shopping, 0),
    GRILLED_BEEF("焼肉", 0, R.drawable.grilled_beaf),
    UNKNOWN(null, 0, 0);

    private final String simpleDescription;
    private final int iconResourceId;
    private final int thumbnailResourceId;

    private EventCategory(String simpleDescription, int iconResourceId, int thumbnailResourceId) {
        this.simpleDescription = simpleDescription;
        this.iconResourceId = iconResourceId;
        this.thumbnailResourceId = thumbnailResourceId;
    }

    public String getSimpleDescription() {
        return simpleDescription;
    }

    /**
     * @return 自身に対応するイベントアイコンのリソースID
     */
    public int getIconResourceId() {
        if (this.equals(UNKNOWN)) {
            throw new UnsupportedOperationException("No icon for type unknown");
        }
        return iconResourceId;
    }

    public int getThumbnailResourceId() {
        return thumbnailResourceId;
    }
}
