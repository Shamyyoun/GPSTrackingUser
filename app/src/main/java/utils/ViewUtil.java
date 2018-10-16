package utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class ViewUtil {
    /**
     * method used to show or hide view in default duration 250
     */
    public static void showView(final View view, boolean show) {
        fadeView(view, show, View.GONE);
    }

    /**
     * method used to show or hide view with INVISIBLE constant in default duration 250
     */
    public static void showView(final View view, boolean show, int invisibleConstant) {
        fadeView(view, show, invisibleConstant);
    }

    /**
     * method used to shows or hides a view with a smooth animation in specific duration
     */
    private static void fadeView(final View view, final boolean show, final int invisibleConstant) {
        view.setVisibility(show ? View.VISIBLE : invisibleConstant);
        view.animate().setDuration(250).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(show ? View.VISIBLE : invisibleConstant);
                    }
                });
    }
}
