package mil.nga.mapcache.utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

import mil.nga.mapcache.R;

public class ViewAnimation {

    public static void expand(final View v, final AnimListener animListener) {
        Animation a = expandAction(v);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animListener.onFinish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);
    }

    private static Animation expandAction(final View v) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int) (targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
        return a;
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public interface AnimListener {
        void onFinish();
    }

    /**
     * Assign a "slide in from left" animation to the given view
     * @param view The view which we want to slide
     * @param duration the duration of the animation in milliseconds
     */
    public static void setSlideInFromLeftAnimation(View view, long duration){
        Animation slide = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_left);
        slide.setDuration(duration);
        view.startAnimation(slide);
    }

    /**
     * Assign a "slide in from right" animation to the given view
     * @param view The view which we want to slide
     * @param duration the duration of the animation in milliseconds
     */
    public static void setSlideInFromRightAnimation(View view, long duration){
        Animation slide = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_right);
        slide.setDuration(duration);
        view.startAnimation(slide);
    }

    /**
     * Assign a "fade in" animation to the given view
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void setFadeInAnimatiom(View view, long duration){
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }

    /**
     * Assign a "scale in to full size" animation to the given view
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void setScaleAnimatiom(View view, long duration){
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }




}
