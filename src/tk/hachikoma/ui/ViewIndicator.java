package tk.hachikoma.ui;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 11/23/13
 * Time: 5:52 PM
 * To change this template use File | Settings | File Templates.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import tk.hachikoma.R;

/**
 * View which has circle-formed page indicator.
 *
 * @author ksoichiro
 */
public class ViewIndicator extends View {

    private static final float RADIUS = 5.0f;
    private static final float DISTANCE = 30.0f;

    private int mNumOfViews;
    private int mPosition;
    private ViewPager mViewPager;

    public ViewIndicator(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ViewIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param context
     * @param attrs
     */
    public ViewIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPosition(final int position) {
        if (position < mNumOfViews) {
            mPosition = position;
            if (mViewPager != null) {
                mViewPager.setCurrentItem(mPosition);
            }
            invalidate();
        }
    }

    public void setViewPager(final ViewPager viewPager) {
        mViewPager = viewPager;
        updateNumOfViews();
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateNumOfViews();
                setPosition(position);
            }
        });
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);

        for (int i = 0; i < mNumOfViews; i++) {
            float cx = (getWidth() - (mNumOfViews - 1) * DISTANCE) / 2 + i * DISTANCE;
            float cy = getHeight() / 2.0f;
            if (mPosition == i) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                paint.setStyle(Paint.Style.STROKE);
            }
            canvas.drawCircle(cx, cy, RADIUS, paint);
        }
    }

    private void updateNumOfViews() {
        if (mViewPager.getAdapter() == null) {
            mNumOfViews = 0;
        } else {
            mNumOfViews = mViewPager.getAdapter().getCount();
        }
    }

}