package com.appspot.hachiko_schedule.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * アイコンとテキストをワンセットにした{@link android.text.Spannable}のサブクラス．
 * https://github.com/kpbird/chips-edittext-libraryを参考に実装
 */
public class TexttipSpan extends ImageSpan {
    public TexttipSpan(Context context, int viewResource, int textViewId,
                       int imageViewId, Uri iconUri, CharSequence text) {
        super(context,
                createBitmap(context, viewResource, textViewId, imageViewId, iconUri, text));
    }

    private static Bitmap createBitmap(Context context, int viewResource, int textViewId,
                                       int imageViewId, Uri iconUri, CharSequence text) {
        LayoutInflater inflater
                = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(viewResource, null);
        ((TextView) view.findViewById(textViewId)).setText(text);
        ((ImageView) view.findViewById(imageViewId)).setImageURI(iconUri);
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        view.setDrawingCacheEnabled(true);
        Bitmap viewBmp = view.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return viewBmp;
    }
}
