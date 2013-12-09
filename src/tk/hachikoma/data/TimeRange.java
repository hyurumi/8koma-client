package tk.hachikoma.data;

/**
 * 05:30 - 09:10とかそういう，時間はばを表すやつ
 */
public class TimeRange {
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinutes;

    public TimeRange(String timeRangeString) {
        String[] timerange = timeRangeString.split(":|-");
        if (timerange.length != 4) {
            throw new IllegalStateException("Invalid format: " + timeRangeString);
        }
        init(Integer.parseInt(timerange[0].trim()),
                Integer.parseInt(timerange[1].trim()),
                Integer.parseInt(timerange[2].trim()),
                Integer.parseInt(timerange[3].trim()));
    }

    private TimeRange(int startHour, int startMinute, int endHour, int endMinutes) {
        init(startHour, startMinute, endHour, endMinutes);
    }

    private void init(int startHour, int startMinute, int endHour, int endMinutes) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinutes = endMinutes;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    /**
     * @return 引数で渡されるTimeRangeが始まったあとに終わる，つまり時間帯がオーバーラップするかどうか
     */
    public boolean endsAfterStartOf(TimeRange another) {
        return endHour * 60 + endMinutes > another.startHour * 60 + another.startMinute;
    }

    /**
     * @return このTimeRangeの開始時間，引数で与えられたTimeRangeの終了時間をもつ新しいTimeRangeインスタンスを返す
     */
    public TimeRange merge(TimeRange another) {
        return new TimeRange(startHour, startMinute, another.endHour, another.endMinutes);
    }

    /**
     * @return 日をまたぐかどうか (endがstartより前の時間になっている)
     */
    public boolean acrossDay() {
        return startHour * 60 + startMinute > endHour * 60 + endMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeRange)) {
            return false;
        }
        TimeRange another = (TimeRange) o;
        return startHour == another.startHour
                && startMinute == another.startMinute
                && endHour == another.endHour
                && endMinutes == another.endMinutes;
    }

    @Override
    public String toString() {
        return startHour + ":" + startMinute + " - " + endHour + ":" + endMinutes;
    }
}
