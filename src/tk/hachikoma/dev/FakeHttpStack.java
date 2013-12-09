package tk.hachikoma.dev;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.StringRequest;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.util.HachikoLogger;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ローカルでのデバッグ用に，メモリ上 or リソース上の値を返す{@link HttpStack}.
 */
class FakeHttpStack implements HttpStack {
    private static final int SIMULATED_DELAY_MS = 500;
    private final Context context;

    FakeHttpStack(Context context) {
        this.context = context;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> stringStringMap)
            throws IOException, AuthFailureError {
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
        }
        HttpResponse response
                = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        List<Header> headers = defaultHeaders();
        response.setHeaders(headers.toArray(new Header[0]));
        response.setLocale(Locale.JAPAN);
        response.setEntity(createEntity(request));
        return response;
    }

    private List<Header> defaultHeaders() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd mmm yyyy HH:mm:ss zzz");
        return Lists.<Header>newArrayList(
                new BasicHeader("Date", dateFormat.format(new Date())),
                new BasicHeader("Server",
                        /* さくらサーバからかえってきた適当なサーバ情報を利用 */
                        "Apache/1.3.42 (Unix) mod_ssl/2.8.31 OpenSSL/0.9.8e")
        );
    }

    /**
     * /res/raw以下のファイルを利用して偽のレスポンスを返す
     */
    private HttpEntity createEntity(Request request) throws UnsupportedEncodingException {
        String resourceName = constructFakeResponseFileName(request);
        int resourceId = context.getResources().getIdentifier(
                resourceName, "raw", context.getApplicationContext().getPackageName());
        if (resourceId == 0) {
            HachikoLogger.warn("No fake file named ", resourceName,
                    "default fake response should be used.");
        } else {
            InputStream stream = context.getResources().openRawResource(resourceId);
            try {
                String string = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
                if ("randomInt".equals(string)) {
                    string = Integer.toString((int) (Math.random() * Integer.MAX_VALUE));
                }

                return new StringEntity(string);
            } catch (IOException e) {
                HachikoLogger.error("error reading " + resourceName, e);
            }
        }
        // 適切なリソースが無いので，適当に返す
        if (request instanceof StringRequest) {
            return new StringEntity("100");
        }
        return new StringEntity(" {\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5}");
    }

    private String constructFakeResponseFileName(Request request) {
        String reqUrl = request.getUrl();
        String apiName = reqUrl.substring(HachikoAPI.BASE.length()).split("/", 2)[0];
        // Note: request.getmethod()を参考にしようと思ったが，Method.DEPRECATED_GET_OR_POSTとかややこいので
        // とりあえずURLのみ見る
        return "fake_res_" + apiName;
    }
}
