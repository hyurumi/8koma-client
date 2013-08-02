package com.appspot.hachiko_schedule.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Viewを横スワイプで削除，を実現するための{@link android.view.View.OnTouchListener}. 下リンクの実装をもとに
 * {@link android.widget.ListView}以外に使えるように汎用的に書きなおしたもの．
 * 注意: このクラスはスワイプで要素をアニメーションさせて画面外に出すだけで，実際にViewTreeから要素を削除すること
 * はしない．ユーザが
 * {@link SwipeAndDismissEventListener#onSwipeEndAnimationEnd(android.view.View, boolean)}
 * を実装し，自身でViewを削除する必要がある．
 *
 * {@see https://www.youtube.com/watch?v=NewCSg2JKLk}
 */
public class SwipeToDismissTouchListener implements View.OnTouchListener {

    /**
     * スワイプとそれに伴うアニメーションを監視するためのリスナ
     */
    public static interface SwipeAndDismissEventListener {
        /**
         * スワイプ開始時に呼ばれる
         *
         * @param view 操作対象のView
         */
        public void onSwipeStart(View view);

        /**
         * ユーザがスワイプ後指を離したとき呼ばれる
         *
         * @param view 捜査対象のView
         */
        public void onSwipeEndAnimationStarted(View view);

        /**
         * ユーザがスワイプ後指を離し発火されたアニメーションの終了時に呼ばれる．Viewは画面外に出ているか，
         * 初期位置に戻っているかのいずれの状態になっている．
         *
         * @param view 操作対象のView
         * @param removed Viewが画面外に出たかどうか
         */
        public void onSwipeEndAnimationEnd(View view, boolean removed);
    }

    /**
     * {@link SwipeAndDismissEventListener} を継承するための何もしない実装
     */
    public static class SwipeAndDismissEventListenerAdapter implements SwipeAndDismissEventListener {
        @Override
        public void onSwipeStart(View view) { }

        @Override
        public void onSwipeEndAnimationStarted(View view) { }

        @Override
        public void onSwipeEndAnimationEnd(View view, boolean removed) { }
    }

    private static final float DELTA_TO_DISMISS_THRESHOLD = 0.25f;
    // 要素を画面幅と同じだけ動かすのにかかる時間でアニメーション速度を指定
    private static final int SWIPE_DURATION_MSEC = 250;

    private final Context context;
    private SwipeAndDismissEventListener swipeAndDismissEventListener;
    private boolean itemPressed = false;
    private boolean swiping = false;
    private float downX;
    private int swipeSlop = -1;

    public SwipeToDismissTouchListener(Context context) {
        this(context, new SwipeAndDismissEventListenerAdapter());
    }

    public SwipeToDismissTouchListener(
            Context context, SwipeAndDismissEventListener swipeAndDismissEventListener) {
        this.context = context;
        this.swipeAndDismissEventListener = swipeAndDismissEventListener;
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
                break;
            case MotionEvent.ACTION_CANCEL:
                resetAlphaAndTranslation(v);
                itemPressed = false;
                break;
            case MotionEvent.ACTION_MOVE:{
                float x = event.getX() + v.getTranslationX();
                float deltaXAbs = Math.abs(x - downX);
                if (!swiping && deltaXAbs > swipeSlop) {
                    swiping = true;
                    swipeAndDismissEventListener.onSwipeStart(v);
                }
                if (swiping) {
                    v.setTranslationX(x -downX);
                    v.setAlpha(1 - deltaXAbs / v.getWidth());
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                if (swiping) {
                    executeSwipeAnimation(v, event.getX());
                }
                itemPressed = false;
                break;
            }
            default:
                return false;
        }

        return true;
    }

    private void executeSwipeAnimation(final View view, float eventX) {
        float x = eventX + view.getTranslationX();
        float deltaXAbs = Math.abs(x - downX);
        float fractionCovered;
        float endX;
        final boolean remove;
        if (deltaXAbs / view.getWidth() > DELTA_TO_DISMISS_THRESHOLD) {
            fractionCovered = deltaXAbs / view.getWidth();
            endX = x < downX ? -view.getWidth() : view.getWidth();
            remove = true;
        } else {
            fractionCovered = 1 - (deltaXAbs / view.getWidth());
            endX = 0;
            remove = false;
        }
        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION_MSEC);
        view.animate()
                .setDuration(duration)
                .alpha(remove ? 0 : 1)
                .translationX(endX)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        resetAlphaAndTranslation(view);
                        swipeAndDismissEventListener.onSwipeEndAnimationEnd(view, remove);
                    }
                });
        swipeAndDismissEventListener.onSwipeEndAnimationStarted(view);
    }

    private void resetAlphaAndTranslation(View view) {
        view.setAlpha(1);
        view.setTranslationX(0);
    }
}
