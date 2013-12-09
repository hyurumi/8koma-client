package tk.hachikoma.data;

/**
 * 端末のカレンダー情報を識別するために使われるID&名前ペア
 */
public class CalendarIdentifier {
    private final long id;
    private final String displayName;

    public CalendarIdentifier(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CalendarIdentifier) {
            return id == ((CalendarIdentifier) o).id
                    && displayName.equals(((CalendarIdentifier) o).displayName);
        }
        return false;
    }

    public String encode() {
        return new StringBuilder().append(id).append(',').append(displayName).toString();
    }

    public static CalendarIdentifier decode(String str) {
        String[] parts = str.split(",", 2);
        return new CalendarIdentifier(Long.parseLong(parts[0]), parts[1]);
    }

    @Override
    public String toString() {
        return encode();
    }
}
