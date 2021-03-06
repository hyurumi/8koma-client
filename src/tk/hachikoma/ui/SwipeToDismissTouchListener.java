package tk.hachikoma.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

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
public class SwipeToDismissTouchListener extends HorizontalSwipeListener {

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

    private SwipeAndDismissEventListener swipeAndDismissEventListener;

    public SwipeToDismissTouchListener(Context context) {
        this(context, new SwipeAndDismissEventListenerAdapter());
    }

    public SwipeToDismissTouchListener(
            Context context, SwipeAndDismissEventListener swipeAndDismissEventListener) {
        super(context);
        this.swipeAndDismissEventListener = swipeAndDismissEventListener;
    }

    @Override
    protected void onSwipeMove(View v, MotionEvent e) {
        float deltaXAbs = Math.abs(e.getX() + v.getTranslationX() - getSwipeStartX());
        v.setAlpha(1 - deltaXAbs / v.getWidth());
    }

    @Override
    protected boolean onSwipeEnd(View v, MotionEvent e) {
        executeSwipeAnimation(v, e.getX());
        return true;
    }

    @Override
    protected boolean onSwipeCancel(View v, MotionEvent e) {
        executeSwipeAnimation(v, e.getX());
        return true;
    }

    private void executeSwipeAnimation(final View view, float eventX) {
        float x = eventX + view.getTranslationX();
        float deltaXAbs = Math.abs(x - getSwipeStartX());
        float fractionCovered;
        float endX;
        final boolean remove;
        if (deltaXAbs / view.getWidth() > DELTA_TO_DISMISS_THRESHOLD) {
            fractionCovered = deltaXAbs / view.getWidth();
            endX = x < getSwipeStartX() ? -view.getWidth() : view.getWidth();
            remove = true;
        } else {
            fractionCovered = 1 - (deltaXAbs / view.getWidth());
            endX = 0;
            remove = false;
        }
        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION_MSEC);
        duration = duration > 0 ? duration : 1L;
        Animator animator = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("alpha", remove ? 0f : 1f),
                PropertyValuesHolder.ofFloat("translationX", endX));
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                resetAlphaAndTranslation(view);
                swipeAndDismissEventListener.onSwipeEndAnimationEnd(view, remove);
            }
        });
        animator.start();

        swipeAndDismissEventListener.onSwipeEndAnimationStarted(view);
    }

    private void resetAlphaAndTranslation(View view) {
        view.setAlpha(1);
        view.setTranslationX(0);
    }
}
