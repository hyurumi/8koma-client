package tk.hachikoma.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 枠線をつけることの出来る{@link ImageView} (実態はImageViewを内包する{@link LinearLayout}).
 *
 * ImageViewには枠線をつけるAPIはないので，枠線を表示したい時は，背景色を適当な色にして，ボーダー分paddingを
 * 開ける．ただし，画像が透明背景だと，さらにImageViewを入れ子にしなくてはならない...
 * このクラスはその辺の面倒を見る
 * {@see http://stackoverflow.com/questions/3263611/border-for-an-image-view-in-android}
 */
public class BorderedImageView extends LinearLayout {
    private final ImageView imageView;
    private final LinearLayout borderView;
    private int borderColor = Color.BLACK;
    private int borderWidth = 1;

    public BorderedImageView(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        borderView = new LinearLayout(context);
        borderView.setBackgroundColor(borderColor);
        borderView.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
        imageView = new ImageView(context);
        imageView.setBackgroundColor(Color.WHITE);
        addView(borderView);
        borderView.addView(imageView);
    }

    public void setImageResource(int resourceId) {
        imageView.setImageResource(resourceId);
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        imageView.setScaleType(scaleType);
    }

    public BorderedImageView setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderView.setBackgroundColor(borderColor);
        return this;
    }

    public BorderedImageView setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        borderView.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
        return this;
    }
}
