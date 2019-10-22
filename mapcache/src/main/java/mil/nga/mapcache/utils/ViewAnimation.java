package mil.nga.mapcache.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
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
     * Assign a "slide out to right" animation to the given view
     * @param view The view which we want to slide
     * @param duration the duration of the animation in milliseconds
     */
    public static void setSlideOutToRightAnimation(View view, long duration){
        Animation slide = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_out_to_right);
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
     * Assign a rotate + fade out animation to the given view and stay invisible when done
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void rotateFadeOut(View view, long duration){
        // Fade out
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        fadeAnim.setDuration(duration);

        // Rotate
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, View.ROTATION, 0, 180);
        rotateAnim.setDuration(duration);

        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(fadeAnim, rotateAnim);
        set1.start();
    }

    /**
     * Assign a rotate + fade in animation to the given view and stay visible when done
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void rotateFadeIn(View view, long duration){
        // Fade in
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        fadeAnim.setDuration(duration);

        // Rotate
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, View.ROTATION, 180, 0);
        rotateAnim.setDuration(duration);

        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(fadeAnim, rotateAnim);
        set1.start();
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

    /**
     * Assign a fade and slide in animation to the given view
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void fadeInFromRight(View view, long duration){
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 180, 0);
        slide.setDuration(duration);

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        fade.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(slide, fade);
        set.start();
    }

    /**
     * Assign a bounce animation from right to left
     */
    public static void bounceRightToLeft(View view, long duration){
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 180, -50);
        slide.setDuration(duration);

        ObjectAnimator slide2 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -50, 80);
        slide2.setDuration(duration);

        ObjectAnimator slide3 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 80, 0);
        slide3.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(slide, slide2, slide3);
        set.start();
    }

    /**
     * Assign a "scale to 120% and back bounce + fade out and in" animation to the given view
     * @param view the view to animate
     * @param duration duration of the animation
     */
    public static void setBounceAnimatiom(View view, long duration){
        ScaleAnimation grow = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow.setDuration(duration);
        ScaleAnimation shrink = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(duration);
        shrink.setStartOffset(duration);

        // Fade out then repeat to fade back in
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.3f);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //and this
        fadeOut.setDuration(100);
        fadeOut.setRepeatMode(Animation.REVERSE);
        fadeOut.setRepeatCount(1);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(grow);
        set.addAnimation(shrink);
        set.addAnimation(fadeOut);
        view.startAnimation(set);
    }




}
