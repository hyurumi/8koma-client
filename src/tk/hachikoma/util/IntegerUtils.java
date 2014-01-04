package tk.hachikoma.util;

public class IntegerUtils {
    public static int parseIntWithDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
