package com.appspot.hachiko_schedule.dev;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;
import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
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
public class FakeHttpStack implements HttpStack {

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> stringStringMap)
            throws IOException, AuthFailureError {
        // TODO: 遅延をはさんだほうがよい？
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

    private HttpEntity createEntity(Request request) throws UnsupportedEncodingException {
        // TODO: きめうちの値を返すのではなく，例えばrequest.getUrl()とかを見て，適切な値を返す
        return new StringEntity(" {\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5}");
    }
}
