package com.appspot.hachiko_schedule.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * あるタイムスロット(何時から何時),あるいは終日の時間幅を示すデータクラス
 */
public class Timeslot implements Parcelable {
    private final Date startDate;
    private final Date endDate;
    private final boolean isAllDay;

    public Timeslot(Date startDate, Date endDate, boolean isAllDay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(startDate);
        dest.writeValue(endDate);
        dest.writeByte((byte) (isAllDay ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Timeslot> CREATOR = new Creator<Timeslot>() {

        @Override
        public Timeslot createFromParcel(Parcel source) {
            return new Timeslot(
                    (Date) source.readValue(this.getClass().getClassLoader()),
                    (Date) source.readValue(this.getClass().getClassLoader()),
                    source.readByte() == 1);
        }

        @Override
        public Timeslot[] newArray(int size) {
            return new Timeslot[size];
        }
    };
}
