package com.bultimatom.silgitsin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeCardView extends FrameLayout {
    private static final float SWIPE_THRESHOLD = 0.28f;
    private static final float ROTATION_MAX = 14f;
    private static final float SCALE_BACK_CARD = 0.94f;
    private static final float TAP_SLOP = 18f;
    private static final long ANIM_DURATION = 260L;

    public interface SwipeListener {
        void onSwipedRight(PhotoItem photo);
        void onSwipedLeft(PhotoItem photo);
        void onSwipeProgress(float progress, boolean isRight);
        void onPhotoTapped(PhotoItem photo);
        void onPhotoDoubleTapped(PhotoItem photo);
    }

    private final View backCard;
    private final View frontCard;
    private PhotoItem currentPhoto;
    private float downX;
    private float downY;
    private long lastTapTime = 0L;
    private Runnable pendingSingleTap;
    private SwipeListener listener;
    private boolean isAnimatingSwipe = false;

    public SwipeCardView(@NonNull Context context) {
        this(context, null);
    }

    public SwipeCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        backCard = LayoutInflater.from(context).inflate(R.layout.view_photo_card, this, false);
        frontCard = LayoutInflater.from(context).inflate(R.layout.view_photo_card, this, false);
        addView(backCard);
        addView(frontCard);
        backCard.setScaleX(SCALE_BACK_CARD);
        backCard.setScaleY(SCALE_BACK_CARD);
        backCard.setAlpha(0.7f);
        setupTouchListener();
    }

    public void setSwipeListener(SwipeListener listener) {
        this.listener = listener;
    }

    public void setPhotos(@Nullable PhotoItem current, @Nullable PhotoItem next) {
        isAnimatingSwipe = false;
        currentPhoto = current;
        resetCard(frontCard);
        resetCard(backCard);
        frontCard.setVisibility(current == null ? GONE : VISIBLE);
        backCard.setVisibility(next == null ? INVISIBLE : VISIBLE);

        if (current != null) {
            loadPhotoIntoCard(frontCard, current);
        }
        if (next != null) {
            loadPhotoIntoCard(backCard, next);
        }
    }

    public void swipeRight() {
        if (currentPhoto != null && !isAnimatingSwipe) {
            animateFlyOut(true);
        }
    }

    public void swipeLeft() {
        if (currentPhoto != null && !isAnimatingSwipe) {
            animateFlyOut(false);
        }
    }

    @Nullable
    public PhotoItem getCurrentPhoto() {
        return currentPhoto;
    }

    private void setupTouchListener() {
        frontCard.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (isAnimatingSwipe) {
                        return true;
                    }
                    downX = event.getRawX();
                    downY = event.getRawY();
                    frontCard.animate().cancel();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    frontCard.setTranslationX(dx);
                    frontCard.setTranslationY(dy * 0.25f);
                    frontCard.setRotation((dx / Math.max(getWidth(), 1)) * ROTATION_MAX);
                    updateOverlayAlpha(dx);
                    updateBackCard(Math.abs(dx));
                    if (listener != null) {
                        listener.onSwipeProgress(Math.min(Math.abs(dx) / swipeThreshold(), 1f), dx > 0);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDx = frontCard.getTranslationX();
                    float totalDy = event.getRawY() - downY;
                    if (Math.abs(totalDx) < TAP_SLOP && Math.abs(totalDy) < TAP_SLOP) {
                        animateBackToCenter();
                        if (listener != null && currentPhoto != null) {
                            handleTap(currentPhoto);
                        }
                    } else if (Math.abs(totalDx) >= swipeThreshold()) {
                        animateFlyOut(totalDx > 0);
                    } else {
                        animateBackToCenter();
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private void handleTap(PhotoItem photo) {
        long now = System.currentTimeMillis();
        if (now - lastTapTime < 320L) {
            if (pendingSingleTap != null) {
                removeCallbacks(pendingSingleTap);
                pendingSingleTap = null;
            }
            lastTapTime = 0L;
            listener.onPhotoDoubleTapped(photo);
            return;
        }

        lastTapTime = now;
        pendingSingleTap = () -> {
            pendingSingleTap = null;
            if (listener != null) {
                listener.onPhotoTapped(photo);
            }
        };
        postDelayed(pendingSingleTap, 330L);
    }

    private void updateOverlayAlpha(float dx) {
        float fraction = Math.min(Math.abs(dx) / swipeThreshold(), 1f);
        frontCard.findViewById(R.id.overlayKeep).setAlpha(dx > 0 ? fraction : 0f);
        frontCard.findViewById(R.id.overlayDelete).setAlpha(dx < 0 ? fraction : 0f);
    }

    private void updateBackCard(float absDx) {
        float progress = Math.min(absDx / swipeThreshold(), 1f);
        float scale = SCALE_BACK_CARD + (1f - SCALE_BACK_CARD) * progress;
        backCard.setScaleX(scale);
        backCard.setScaleY(scale);
        backCard.setAlpha(0.7f + 0.3f * progress);
    }

    private void animateFlyOut(boolean toRight) {
        if (isAnimatingSwipe) {
            return;
        }
        PhotoItem photo = currentPhoto;
        if (photo == null) {
            return;
        }

        isAnimatingSwipe = true;
        frontCard.setOnTouchListener(null);
        float targetX = toRight ? getWidth() * 1.65f : -getWidth() * 1.65f;
        float targetRotation = toRight ? ROTATION_MAX * 1.8f : -ROTATION_MAX * 1.8f;

        frontCard.animate()
                .translationX(targetX)
                .translationY(frontCard.getTranslationY() - 90f)
                .rotation(targetRotation)
                .alpha(0.85f)
                .setDuration(ANIM_DURATION + 40L)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frontCard.setOnTouchListener(null);
                        if (listener != null) {
                            if (toRight) {
                                listener.onSwipedRight(photo);
                            } else {
                                listener.onSwipedLeft(photo);
                            }
                        }
                        post(() -> setupTouchListener());
                    }
                })
                .start();

        backCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateBackToCenter() {
        frontCard.findViewById(R.id.overlayKeep).setAlpha(0f);
        frontCard.findViewById(R.id.overlayDelete).setAlpha(0f);
        if (listener != null) {
            listener.onSwipeProgress(0f, true);
        }

        frontCard.animate()
                .translationX(0f)
                .translationY(0f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(280L)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .setListener(null)
                .start();

        backCard.animate()
                .scaleX(SCALE_BACK_CARD)
                .scaleY(SCALE_BACK_CARD)
                .alpha(0.7f)
                .setDuration(280L)
                .start();
    }

    private void resetCard(View card) {
        card.animate().cancel();
        card.setTranslationX(0f);
        card.setTranslationY(0f);
        card.setRotation(0f);
        card.setAlpha(1f);
        card.findViewById(R.id.overlayKeep).setAlpha(0f);
        card.findViewById(R.id.overlayDelete).setAlpha(0f);

        if (card == backCard) {
            card.setScaleX(SCALE_BACK_CARD);
            card.setScaleY(SCALE_BACK_CARD);
            card.setAlpha(0.7f);
        } else {
            card.setScaleX(1f);
            card.setScaleY(1f);
        }
    }

    private void loadPhotoIntoCard(View card, PhotoItem photo) {
        ImageView image = card.findViewById(R.id.ivPhoto);
        ImageView backdrop = card.findViewById(R.id.ivPhotoBackdrop);
        TextView name = card.findViewById(R.id.tvPhotoName);
        TextView date = card.findViewById(R.id.tvPhotoDate);
        TextView videoBadge = card.findViewById(R.id.tvVideoBadge);

        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);
        videoBadge.setVisibility(photo.isVideo() ? VISIBLE : GONE);
        if (photo.isVideo()) {
            Bitmap frame = createVideoFrame(photo);
            image.setImageBitmap(frame);
            backdrop.setImageBitmap(frame);
        } else {
            image.setImageURI(photo.getUri());
            backdrop.setImageURI(photo.getUri());
        }
        String context = photo.getContextLabel();
        if (context == null || context.trim().isEmpty()) {
            name.setText("");
            date.setText("");
            card.findViewById(R.id.layoutPhotoInfo).setVisibility(GONE);
        } else {
            card.findViewById(R.id.layoutPhotoInfo).setVisibility(VISIBLE);
            name.setText(context);
            date.setText((photo.isVideo() ? "Video · " : "") + photo.getReadableSize());
        }
    }

    @Nullable
    private Bitmap createVideoFrame(PhotoItem photo) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(getContext(), photo.getUri());
            return retriever.getFrameAtTime(0);
        } catch (RuntimeException exception) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private float swipeThreshold() {
        return Math.max(getWidth(), 1) * SWIPE_THRESHOLD;
    }
}
