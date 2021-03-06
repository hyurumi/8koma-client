package tk.hachikoma.util;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.deploygate.sdk.DeployGate;

import java.io.PrintWriter;
import java.io.StringWriter;

import static tk.hachikoma.Constants.IS_ALPHA_USER;
import static tk.hachikoma.Constants.IS_DEVELOPER;

/**
 * Wrapper class of {@link Log}
 */
public class HachikoLogger {
    private static final String MY_CLASS_NAME = HachikoLogger.class.getSimpleName();
    private static final String DEFAULT_TAG = "HachikoApp";

    static public int info(Object... objects) {
        return Log.i(DEFAULT_TAG, appendAsString(objects));
    }

    static public int info(String msg) {
        return Log.i(DEFAULT_TAG, msg);
    }

    static public int verbose(String msg) {
        return Log.v(DEFAULT_TAG, msg);
    }

    static public int error(String msg) {
        DeployGate.logError(msg);
        return Log.e(DEFAULT_TAG, msg);
    }

    static public int warn(String msg) {
        return Log.w(DEFAULT_TAG, msg);
    }

    static public int warn(Object... objects) {
        return Log.w(DEFAULT_TAG, appendAsString(objects));
    }

    static public int error(String msg, Throwable throwable) {
        DeployGate.logError(msg);
        DeployGate.logError(toString(throwable));
        return Log.e(DEFAULT_TAG, msg, throwable);
    }

    static public int error(String msg, VolleyError volleyError) {
        if (volleyError != null && volleyError.networkResponse != null) {
            DeployGate.logError(msg + " [" + volleyError.networkResponse.statusCode + "] "
                    + new String(volleyError.networkResponse.data));
            DeployGate.logError(toString(volleyError));
            return Log.e(DEFAULT_TAG, msg + " [" + volleyError.networkResponse.statusCode + "] "
            + new String(volleyError.networkResponse.data), volleyError);
        } else {
            DeployGate.logError("null response " + toString(volleyError));
            return Log.e(DEFAULT_TAG, msg + " null response ", volleyError);
        }
    }

    static private String toString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    static public int debug(String msg) {
        if (!IS_ALPHA_USER) {
            return 0;
        }
        msg = calcFileNameAndLineNumberUsingException() + msg;
        DeployGate.logDebug(msg);
        return Log.d(DEFAULT_TAG, msg);
    }

    static public int dumpRequest(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append("url: ").append(request.getUrl()).append("\n")
                .append("method: ").append(request.getMethod()).append("\n");
        try {
            if (IS_DEVELOPER) {
                builder.append("headers: ").append(request.getHeaders()).append("\n");
            }
            builder.append("body: ")
                    .append(request.getBody() == null ? "" : new String(request.getBody()))
                    .append("\n(").append(request.getBodyContentType()).append(")");
        } catch (Exception e) {
            builder.append("encountered error while getting header or body " + e);
        }
        return HachikoLogger.debug(builder.toString());
    }

    static public int dumpResponse(NetworkResponse response) {
        if (response == null) {
            return HachikoLogger.debug("null response, server may be dead.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("status: ").append(response.statusCode).append("\n")
                .append("headers: ").append(response.headers).append("\n")
                .append("body: ").append(response.data == null ? "" : new String(response.data))
                .append("\n").append("notModified?: ").append(response.notModified);
        return HachikoLogger.debug(builder.toString());
    }

    static public int debug(Object... objects) {
        if (!IS_ALPHA_USER) { // 冗長な検査だけど，これがないとデバッグモードじゃない時も文字列結合してしまう
            return 0;
        }
        return debug(appendAsString(objects));
    }

    static public int debugDeloperOnly(Object... objects) {
        if (!IS_DEVELOPER) {
            return 0;
        }
        return debug(objects);
    }

    static private String appendAsString(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object obj: objects) {
            builder.append(obj).append(' ');
        }
        return builder.toString();
    }

    static public int debugWithSeparator(CharSequence separator, Object... objects) {
        if (!IS_ALPHA_USER) { // 冗長な検査だけど，これがないとデバッグモードじゃない時も文字列結合してしまう
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj: objects) {
            builder.append(obj).append(' ').append(separator);
        }
        return debug(builder.toString());
    }

    /**
     * 呼び出し元のファイル名，行数に関わる情報を取得する．例外機構を利用するため，呼び出しにコストがかかる．プロダクションの
     * 正常系のログには用いるべきではないと思われる．
     *
     * @return 呼び出し元のファイル名と行数
     */
    private static String calcFileNameAndLineNumberUsingException(){
        Exception e = new Exception();
        StackTraceElement[] elements = e.getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            String fileName = elements[i].getFileName();
            if (fileName.contains(MY_CLASS_NAME)) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            if (fileName != null && fileName.endsWith(".java")) {
                builder.append(fileName.substring(0, fileName.length() - 5));
            } else {
                builder.append(fileName);
            }
            builder.append(':').append(elements[i].getLineNumber()).append("] ");
            return builder.toString();
        }
        return "(unknown location)";
    }
}
