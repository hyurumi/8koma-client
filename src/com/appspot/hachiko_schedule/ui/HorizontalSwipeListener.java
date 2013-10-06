package com.appspot.hachiko_schedule.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * {@link SwipeToDismissTouchListener}とかを実現させるために横スワイプを拾うベースクラス
 */
public abstract class HorizontalSwipeListener implements View.OnTouchListener {

    private final Context context;
    private boolean itemPressed = false;
    private boolean swiping = false;
    private float downX;
    private int swipeSlop = -1;

    public HorizontalSwipeListener(Context context) {
        this.context = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (swipeSlop < 0) {
            swipeSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (itemPressed) {
                    return false;
                }
                itemPressed = true;
                downX = event.getX();
                onSwipeStart(v, event);
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setTranslationX(0);
                itemPressed = false;
                onSwipeCancel(v);
                break;
            case MotionEvent.ACTION_MOVE:{
                float x = event.getX() + v.getTranslationX();
                float deltaXAbs = Math.abs(x - downX);
                if (!swiping && deltaXAbs > swipeSlop) {
                    swiping = true;
                }
                if (swiping) {
                    v.setTranslationX(x -downX);
                    onSwipeMove(v, event);
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                if (swiping && !onSwipeEnd(v, event)) {
                    v.setTranslationX(0);
                }
                itemPressed = false;
                onTouchEnd(v, event, swiping);
                break;
            }
            default:
                return false;
        }
        return true;
    }

    protected float getSwipeStartX() {
        return downX;
    }

    /**
     * デフォルト実装はNo-op
     */
    protected void onSwipeCancel(View v) {
    }

    /**
     * デフォルト実装はNo-op
     */
    protected void onSwipeStart(View v, MotionEvent e) {
    }

    protected abstract void onSwipeMove(View v, MotionEvent e);
    /**
     * @return trueを返すとデフォルトの動作(ただちにもとの位置に戻る)がキャンセルされる
     */
    protected abstract boolean onSwipeEnd(View v, MotionEvent e);

    /**
     * デフォルト実装はNo-op, swipe中であるかどうかにかかわらずTouchが終わった時に呼ばれる．
     * onSwipeEndと同時に呼ばれ得る
     */
    protected void onTouchEnd(View v, MotionEvent e, boolean swiping) {
    };
}
