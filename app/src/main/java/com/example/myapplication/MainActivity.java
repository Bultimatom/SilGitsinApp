package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SwipeCardView.SwipeListener {
    private MediaRepository mediaRepository;
    private ReviewStore reviewStore;
    private SwipeCardView swipeCardView;
    private View layoutEmpty;
    private TextView tvPhotoCount;
    private TextView tvDeletedCount;
    private TextView tvKeptCount;
    private TextView tvSavedMb;
    private TextView tvScoreCaption;
    private TextView tvStreakLabel;
    private TextView tvHintTitle;
    private TextView tvHintBody;
    private TextView tvNoticeIcon;
    private TextView tvNoticeMessage;
    private TextView tvEmptyTitle;
    private TextView tvEmptyDesc;
    private View layoutEmptyIcon;
    private TextView btnGrantPermission;
    private ImageButton btnDelete;
    private ImageButton btnUndo;
    private ImageButton btnKeep;
    private ImageButton btnFavorite;
    private ImageButton btnCollection;
    private ImageButton btnSettings;
    private ImageButton btnTheme;
    private TextView btnSecondaryAction;
    private TextView btnReviewSelected;
    private View layoutIntroHint;
    private View layoutOnboardingOverlay;
    private View onboardingDemoCard;
    private View onboardingKeepLabel;
    private View onboardingDeleteLabel;
    private View onboardingFavoriteButton;
    private View onboardingFinger;
    private View onboardingUndoButton;
    private View onboardingKeepButton;
    private View onboardingDeleteButton;
    private View onboardingReviewButton;
    private TextView tvOnboardingStepTitle;
    private TextView tvOnboardingStepBody;
    private TextView tvOnboardingCounter;
    private TextView btnOnboardingDone;
    private View layoutNotice;
    private ScrollView scrollDeletePreview;
    private GridLayout gridDeletePreview;
    private ProgressBar progressBar;
    private ProgressBar scoreProgress;

    private final List<PhotoItem> photos = new ArrayList<>();
    private final List<PhotoItem> queuedDeletePhotos = new ArrayList<>();
    private final List<List<PhotoItem>> duplicateGroups = new ArrayList<>();
    private final ArrayDeque<UndoAction> undoStack = new ArrayDeque<>();
    private int currentIndex = 0;
    private int duplicateGroupIndex = 0;
    private int deletedCount = 0;
    private int keptCount = 0;
    private int streakCount = 0;
    private long sessionSavedBytes = 0L;
    private boolean editingDeleteList = false;
    private int onboardingStep = 0;
    private Runnable onboardingReplayRunnable;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = false;
                for (Boolean value : result.values()) {
                    if (Boolean.TRUE.equals(value)) {
                        granted = true;
                        break;
                    }
                }
                if (granted) {
                    loadPhotos();
                } else {
                    showPermissionDenied();
                }
            });

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    onBatchDeleteConfirmed();
                } else {
                    showToast("Silme iptal edildi");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences.promotePendingThemeOnce(this);
        AppPreferences.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupSystemBars();
        mediaRepository = new MediaRepository(this);
        reviewStore = new ReviewStore(this);
        setupListeners();
        checkPermissions();
        showOnboardingIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reviewStore != null) {
            updateCollectionButton();
        }
        updateThemeButton();
    }

    private void bindViews() {
        swipeCardView = findViewById(R.id.swipeCardView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvSavedMb = findViewById(R.id.tvSavedMb);
        tvScoreCaption = findViewById(R.id.tvScoreCaption);
        tvStreakLabel = findViewById(R.id.tvStreakLabel);
        tvPhotoCount = tvStreakLabel;
        tvDeletedCount = tvStreakLabel;
        tvKeptCount = tvStreakLabel;
        tvHintTitle = findViewById(R.id.tvHintTitle);
        tvHintBody = findViewById(R.id.tvHintBody);
        tvNoticeIcon = findViewById(R.id.tvNoticeIcon);
        tvNoticeMessage = findViewById(R.id.tvNoticeMessage);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        layoutEmptyIcon = findViewById(R.id.layoutEmptyIcon);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        btnDelete = findViewById(R.id.btnDelete);
        btnUndo = findViewById(R.id.btnUndo);
        btnKeep = findViewById(R.id.btnKeep);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnCollection = findViewById(R.id.btnCollection);
        btnSettings = findViewById(R.id.btnSettings);
        btnTheme = findViewById(R.id.btnTheme);
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction);
        btnReviewSelected = findViewById(R.id.btnReviewSelected);
        layoutIntroHint = findViewById(R.id.layoutIntroHint);
        layoutOnboardingOverlay = findViewById(R.id.layoutOnboardingOverlay);
        onboardingDemoCard = findViewById(R.id.onboardingDemoCard);
        onboardingKeepLabel = findViewById(R.id.onboardingKeepLabel);
        onboardingDeleteLabel = findViewById(R.id.onboardingDeleteLabel);
        onboardingFavoriteButton = findViewById(R.id.onboardingFavoriteButton);
        onboardingFinger = findViewById(R.id.onboardingFinger);
        onboardingUndoButton = findViewById(R.id.onboardingUndoButton);
        onboardingKeepButton = findViewById(R.id.onboardingKeepButton);
        onboardingDeleteButton = findViewById(R.id.onboardingDeleteButton);
        onboardingReviewButton = findViewById(R.id.onboardingReviewButton);
        tvOnboardingStepTitle = findViewById(R.id.tvOnboardingStepTitle);
        tvOnboardingStepBody = findViewById(R.id.tvOnboardingStepBody);
        tvOnboardingCounter = findViewById(R.id.tvOnboardingCounter);
        btnOnboardingDone = findViewById(R.id.btnOnboardingDone);
        layoutNotice = findViewById(R.id.layoutNotice);
        scrollDeletePreview = findViewById(R.id.scrollDeletePreview);
        gridDeletePreview = findViewById(R.id.gridDeletePreview);
        progressBar = findViewById(R.id.progressBar);
        scoreProgress = findViewById(R.id.scoreProgress);
    }

    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        swipeCardView.setSwipeListener(this);
        updateCollectionButton();
        updateThemeButton();
        btnTheme.setOnClickListener(view -> {
            animateButton(btnTheme);
            AppPreferences.setDarkMode(this, !AppPreferences.isDarkMode(this));
            updateThemeButton();
        });
        btnSettings.setOnClickListener(view -> showSettingsDialog());
        btnCollection.setOnClickListener(view -> startActivity(new Intent(this, FavoritesActivity.class)));
        btnGrantPermission.setOnClickListener(view -> permissionLauncher.launch(requiredPermissions()));
        btnKeep.setOnClickListener(view -> {
            animateButton(btnKeep);
            swipeCardView.swipeRight();
        });
        btnFavorite.setOnClickListener(view -> {
            animateButton(btnFavorite);
            addCurrentPhotoToFavorites();
        });
        btnDelete.setOnClickListener(view -> {
            animateButton(btnDelete);
            swipeCardView.swipeLeft();
        });
        btnUndo.setOnClickListener(view -> {
            animateButton(btnUndo);
            undoLastAction();
        });
        btnReviewSelected.setOnClickListener(view -> startDeleteReview());
        btnOnboardingDone.setOnClickListener(view -> advanceOnboarding());
        findViewById(R.id.btnOnboardingSkip).setOnClickListener(view -> hideOnboardingOverlay());
    }

    private void showOnboardingIfNeeded() {
        if (layoutOnboardingOverlay == null || AppPreferences.isOnboardingDone(this)) {
            return;
        }
        layoutOnboardingOverlay.setVisibility(View.VISIBLE);
        layoutOnboardingOverlay.setAlpha(0f);
        layoutOnboardingOverlay.animate()
                .alpha(1f)
                .setDuration(220L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    onboardingStep = 0;
                    renderOnboardingStep();
                })
                .start();
    }

    private void hideOnboardingOverlay() {
        AppPreferences.setOnboardingDone(this);
        AppPreferences.setMainHintDone(this);
        stopOnboardingAnimation();
        if (layoutOnboardingOverlay == null || layoutOnboardingOverlay.getVisibility() != View.VISIBLE) {
            return;
        }
        layoutOnboardingOverlay.animate()
                .alpha(0f)
                .setDuration(180L)
                .withEndAction(() -> layoutOnboardingOverlay.setVisibility(View.GONE))
                .start();
    }

    private void advanceOnboarding() {
        if (onboardingStep >= 5) {
            hideOnboardingOverlay();
            return;
        }
        onboardingStep++;
        renderOnboardingStep();
    }

    private void renderOnboardingStep() {
        String[] titles = {
                "Sağa kaydır",
                "Sola kaydır",
                "Çift dokun: favori",
                "Geri al",
                "Seçilenleri incele",
                "Silme en sonda"
        };
        String[] bodies = {
                "Fotoğraf sağa giderse tutulur ve sıradaki karta geçilir.",
                "Fotoğraf sola giderse silinecekler listesine eklenir; hemen silinmez.",
                "Fotoğrafa iki kez dokunarak favorilere ekleyebilirsin. Üstteki kalp favorilerini açar.",
                "Yanlış kaydırırsan geri al butonu son işlemi geri getirir.",
                "Sola kaydırınca kartın üstünde seçilenler barı çıkar. Hepsini bitirmeden oradan listeye girebilirsin.",
                "Silme işlemi, seçilenler ekranındaki son Android onayından sonra gerçekleşir."
        };
        tvOnboardingStepTitle.setText(titles[onboardingStep]);
        tvOnboardingStepBody.setText(bodies[onboardingStep]);
        tvOnboardingCounter.setText((onboardingStep + 1) + "/6");
        btnOnboardingDone.setText(onboardingStep == 5 ? "Anladım" : "İleri");
        playOnboardingAnimation();
    }

    private void playOnboardingAnimation() {
        stopOnboardingAnimation();
        resetOnboardingDemo();

        if (onboardingStep == 0) {
            showSwipeAnimation(true);
        } else if (onboardingStep == 1) {
            showSwipeAnimation(false);
        } else if (onboardingStep == 2) {
            showDoubleTapFavoriteAnimation();
        } else if (onboardingStep == 3) {
            showUndoAnimation();
        } else if (onboardingStep == 4) {
            onboardingReviewButton.setVisibility(View.VISIBLE);
            pulseOnboardingView(onboardingReviewButton);
        } else {
            onboardingReviewButton.setVisibility(View.VISIBLE);
            onboardingDeleteLabel.setVisibility(View.VISIBLE);
            onboardingDeleteLabel.setAlpha(1f);
            onboardingDemoCard.setAlpha(0.72f);
            pulseOnboardingView(onboardingReviewButton);
        }

        onboardingReplayRunnable = this::playOnboardingAnimation;
        layoutOnboardingOverlay.postDelayed(onboardingReplayRunnable, 2600L);
    }

    private void showUndoAnimation() {
        onboardingDeleteLabel.setVisibility(View.VISIBLE);
        pulseOnboardingView(onboardingUndoButton);
        onboardingDemoCard.animate()
                .translationX(-dpToPx(72))
                .rotation(-7f)
                .setDuration(360L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> onboardingDemoCard.animate()
                        .translationX(0f)
                        .rotation(0f)
                        .setStartDelay(180L)
                        .setDuration(420L)
                        .setInterpolator(new OvershootInterpolator(0.8f))
                        .start())
                .start();
    }

    private void showDoubleTapFavoriteAnimation() {
        onboardingFinger.setVisibility(View.VISIBLE);
        onboardingFinger.setAlpha(0f);
        onboardingFinger.setScaleX(1f);
        onboardingFinger.setScaleY(1f);
        onboardingFinger.setTranslationY(dpToPx(22));
        onboardingFinger.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> tapOnboardingFinger(0))
                .start();
    }

    private void tapOnboardingFinger(int tapCount) {
        if (onboardingStep != 2) {
            return;
        }
        if (tapCount >= 2) {
            pulseOnboardingView(onboardingFavoriteButton);
            return;
        }
        animateOnboardingCardTap();
        onboardingFinger.animate()
                .scaleX(0.78f)
                .scaleY(0.78f)
                .setDuration(130L)
                .withEndAction(() -> onboardingFinger.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .setInterpolator(new OvershootInterpolator(0.8f))
                        .withEndAction(() -> onboardingFinger.postDelayed(
                                () -> tapOnboardingFinger(tapCount + 1),
                                tapCount == 0 ? 160L : 0L
                        ))
                        .start())
                .start();
    }

    private void animateOnboardingCardTap() {
        onboardingDemoCard.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(110L)
                .withEndAction(() -> onboardingDemoCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .setInterpolator(new OvershootInterpolator(0.8f))
                        .start())
                .start();
    }

    private void showSwipeAnimation(boolean keep) {
        View label = keep ? onboardingKeepLabel : onboardingDeleteLabel;
        View button = keep ? onboardingKeepButton : onboardingDeleteButton;
        float distance = keep ? dpToPx(92) : -dpToPx(92);
        float rotation = keep ? 9f : -9f;

        label.setVisibility(View.VISIBLE);
        label.setAlpha(0f);
        label.animate().alpha(1f).setDuration(180L).start();
        pulseOnboardingView(button);
        onboardingDemoCard.animate()
                .translationX(distance)
                .rotation(rotation)
                .setStartDelay(260L)
                .setDuration(520L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> onboardingDemoCard.animate()
                        .alpha(0f)
                        .setDuration(160L)
                        .withEndAction(() -> {
                            onboardingDemoCard.setTranslationX(0f);
                            onboardingDemoCard.setRotation(0f);
                            onboardingDemoCard.animate().alpha(1f).setDuration(180L).start();
                        })
                        .start())
                .start();
    }

    private void pulseOnboardingView(View view) {
        view.animate()
                .scaleX(1.18f)
                .scaleY(1.18f)
                .setDuration(260L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(340L)
                        .setInterpolator(new OvershootInterpolator(0.7f))
                        .start())
                .start();
    }

    private void resetOnboardingDemo() {
        View[] views = {
                onboardingDemoCard,
                onboardingFavoriteButton,
                onboardingFinger,
                onboardingUndoButton,
                onboardingKeepButton,
                onboardingDeleteButton,
                onboardingReviewButton
        };
        for (View view : views) {
            view.animate().cancel();
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setAlpha(1f);
        }
        onboardingDemoCard.setTranslationX(0f);
        onboardingDemoCard.setTranslationY(0f);
        onboardingDemoCard.setRotation(0f);
        onboardingFinger.setVisibility(View.GONE);
        onboardingFinger.setTranslationY(0f);
        onboardingKeepLabel.animate().cancel();
        onboardingDeleteLabel.animate().cancel();
        onboardingKeepLabel.setVisibility(View.GONE);
        onboardingDeleteLabel.setVisibility(View.GONE);
        onboardingReviewButton.setVisibility(View.GONE);
        onboardingKeepLabel.setAlpha(1f);
        onboardingDeleteLabel.setAlpha(1f);
    }

    private void stopOnboardingAnimation() {
        if (layoutOnboardingOverlay != null && onboardingReplayRunnable != null) {
            layoutOnboardingOverlay.removeCallbacks(onboardingReplayRunnable);
            onboardingReplayRunnable = null;
        }
    }

    private void checkPermissions() {
        boolean allGranted = true;
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            loadPhotos();
        } else {
            showPermissionUI();
        }
    }

    private String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        }
        return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
    }

    private void loadPhotos() {
        editingDeleteList = false;
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<PhotoItem> loadedPhotos = mediaRepository.loadShuffledPhotos(reviewStore.getReviewedIds());
            runOnUiThread(() -> {
                photos.clear();
                photos.addAll(loadedPhotos);
                currentIndex = 0;
                duplicateGroupIndex = 0;
                duplicateGroups.clear();
                duplicateGroups.addAll(findDuplicateGroups(loadedPhotos));
                undoStack.clear();
                updateStats();

                if (photos.isEmpty()) {
                    if (queuedDeletePhotos.isEmpty()) {
                        showEmptyGallery();
                    } else {
                        showAllDone();
                    }
                } else if (!duplicateGroups.isEmpty()) {
                    showDuplicateGroup();
                } else {
                    showCurrentPhotos();
                    showIntroHintIfNeeded();
                }
            });
        }).start();
    }

    private void showCurrentPhotos() {
        resetButtonScales();
        if (currentIndex >= photos.size()) {
            showAllDone();
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.VISIBLE);
        updateReviewSelectedButton();
        PhotoItem current = photos.get(currentIndex);
        PhotoItem next = currentIndex + 1 < photos.size() ? photos.get(currentIndex + 1) : null;
        swipeCardView.setPhotos(current, next);
        updateFavoriteButton(current);
        updateCounter();
        updateProgressBar();
    }

    private void updateReviewSelectedButton() {
        if (btnReviewSelected == null) {
            return;
        }
        boolean show = !editingDeleteList
                && layoutEmpty.getVisibility() != View.VISIBLE
                && !queuedDeletePhotos.isEmpty();
        btnReviewSelected.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            btnReviewSelected.setText(queuedDeletePhotos.size() + " seçildi - İncele / Sil");
        }
    }

    @Override
    public void onSwipedRight(PhotoItem photo) {
        if (editingDeleteList) {
            removeFromDeleteList(photo);
            return;
        }

        undoStack.addLast(new UndoAction(photo, true));
        reviewStore.markReviewed(photo.getId());
        reviewStore.addKept();
        keptCount++;
        streakCount++;
        currentIndex++;
        updateStats();
        showCurrentPhotos();
        showToast("Saklandı");
    }

    @Override
    public void onSwipedLeft(PhotoItem photo) {
        if (editingDeleteList) {
            keepInDeleteList();
            return;
        }

        queuedDeletePhotos.add(photo);
        undoStack.addLast(new UndoAction(photo, false));
        reviewStore.markReviewed(photo.getId());
        deletedCount++;
        sessionSavedBytes += Math.max(photo.getSize(), 0L);
        streakCount++;
        pulseScore();
        currentIndex++;
        updateStats();
        showCurrentPhotos();
        showToast("Silme listesine eklendi");
    }

    @Override
    public void onSwipeProgress(float progress, boolean isRight) {
        float activeScale = 1f + progress * 0.14f;
        if (isRight) {
            btnKeep.setScaleX(activeScale);
            btnKeep.setScaleY(activeScale);
            btnDelete.setScaleX(1f);
            btnDelete.setScaleY(1f);
        } else {
            btnDelete.setScaleX(activeScale);
            btnDelete.setScaleY(activeScale);
            btnKeep.setScaleX(1f);
            btnKeep.setScaleY(1f);
        }
    }

    @Override
    public void onPhotoTapped(PhotoItem photo) {
        showPhotoPreview(photo);
    }

    @Override
    public void onPhotoDoubleTapped(PhotoItem photo) {
        toggleFavorite(photo);
    }

    private void showPhotoPreview(PhotoItem photo) {
        if (photo.isVideo()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(photo.getUri(), "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(intent, "Videoyu ac"));
            } catch (RuntimeException exception) {
                showToast("Video acilamadi");
            }
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        container.setPadding(0, 0, 0, 0);

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(photo.getUri());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        imageView.setContentDescription(photo.getDisplayName());
        container.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView closeHint = new TextView(this);
        closeHint.setText("Kapat");
        closeHint.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        closeHint.setTextSize(14f);
        closeHint.setPadding(24, 18, 24, 18);
        closeHint.setBackgroundColor(0x66000000);
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(24, 48, 24, 24);
        container.addView(closeHint, hintParams);

        container.setOnClickListener(view -> dialog.dismiss());
        dialog.setContentView(container);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.black);
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    private void deleteQueuedPhotos() {
        if (queuedDeletePhotos.isEmpty()) {
            restartCleaning();
            return;
        }

        if (hasFavoriteInDeleteQueue()) {
            showFavoriteDeleteWarning();
            return;
        }

        requestQueuedDelete();
    }

    private void requestQueuedDelete() {
        if (queuedDeletePhotos.isEmpty()) {
            restartCleaning();
            return;
        }

        editingDeleteList = false;
        boolean deletedImmediately = MediaDeleteHelper.requestDelete(this, queuedDeletePhotos, deleteLauncher);
        if (deletedImmediately) {
            onBatchDeleteConfirmed();
        }
    }

    private boolean hasFavoriteInDeleteQueue() {
        for (PhotoItem photo : queuedDeletePhotos) {
            if (reviewStore.getFavoriteIds().contains(String.valueOf(photo.getId()))) {
                return true;
            }
        }
        return false;
    }

    private void showFavoriteDeleteWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Favoriler de silinecek")
                .setMessage("Silme listesinde favorilere eklediğin fotoğraflar var. Devam edersen galeriden silinir ve favorilerden de kaldırılır.")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Devam Et", (dialog, which) -> requestQueuedDelete())
                .show();
    }

    private void startDeleteReview() {
        if (queuedDeletePhotos.isEmpty()) {
            restartCleaning();
            return;
        }

        editingDeleteList = false;
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        layoutEmptyIcon.setVisibility(View.GONE);
        tvEmptyTitle.setText("Silme Onayı");
        tvEmptyDesc.setText(queuedDeletePhotos.size()
                + " fotoğraf silinecek. Aşağıdaki fotoğrafları kontrol et; devam edersen Android tek sistem onayı gösterecek.");
        buildDeletePreviewGrid();
        btnGrantPermission.setText("Evet, Sil");
        btnGrantPermission.setOnClickListener(view -> deleteQueuedPhotos());
        btnSecondaryAction.setText("Düzenlemeye Devam Et");
        btnSecondaryAction.setVisibility(View.VISIBLE);
        btnSecondaryAction.setOnClickListener(view -> startDeleteListEdit());
        updateStats();
        progressBar.setProgress(100);
    }

    private void startDeleteListEdit() {
        if (queuedDeletePhotos.isEmpty()) {
            showAllDone();
            return;
        }

        editingDeleteList = true;
        currentIndex = 0;
        hideDeletePreviewGrid();
        btnReviewSelected.setVisibility(View.GONE);
        btnSecondaryAction.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        showDeleteEditPhoto();
        showToast("Sağa kaydır: silmeden çıkar, sola kaydır: silineceklerde tut");
    }

    private void showDeleteEditPhoto() {
        resetButtonScales();
        if (queuedDeletePhotos.isEmpty()) {
            editingDeleteList = false;
            showAllDone();
            return;
        }
        if (currentIndex >= queuedDeletePhotos.size()) {
            editingDeleteList = false;
            startDeleteReview();
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        PhotoItem current = queuedDeletePhotos.get(currentIndex);
        PhotoItem next = currentIndex + 1 < queuedDeletePhotos.size()
                ? queuedDeletePhotos.get(currentIndex + 1)
                : null;
        swipeCardView.setPhotos(current, next);
        updateFavoriteButton(current);
        updateStats();
        progressBar.setProgress((int) ((currentIndex / (float) queuedDeletePhotos.size()) * 100));
        updateStats();
    }

    private void keepInDeleteList() {
        currentIndex++;
        showDeleteEditPhoto();
        showToast("Silineceklerde kaldı");
    }

    private void removeFromDeleteList(PhotoItem photo) {
        queuedDeletePhotos.remove(photo);
        deletedCount = Math.max(deletedCount - 1, 0);
        keptCount++;
        sessionSavedBytes = Math.max(sessionSavedBytes - Math.max(photo.getSize(), 0L), 0L);
        reviewStore.addKept();
        updateStats();
        showDeleteEditPhoto();
        showToast("Silme listesinden çıkarıldı");
    }

    private void buildDeletePreviewGrid() {
        gridDeletePreview.removeAllViews();
        scrollDeletePreview.setVisibility(View.VISIBLE);
        int itemSize = dpToPx(92);
        int margin = dpToPx(4);

        for (PhotoItem photo : queuedDeletePhotos) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(photo.getUri());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card));
            imageView.setContentDescription(photo.getDisplayName());
            imageView.setOnClickListener(view -> showPhotoPreview(photo));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = itemSize;
            params.height = itemSize;
            params.setMargins(margin, margin, margin, margin);
            gridDeletePreview.addView(imageView, params);
        }
    }

    private void hideDeletePreviewGrid() {
        scrollDeletePreview.setVisibility(View.GONE);
        gridDeletePreview.removeAllViews();
        layoutEmptyIcon.setVisibility(View.VISIBLE);
    }

    private List<List<PhotoItem>> findDuplicateGroups(List<PhotoItem> items) {
        Map<String, List<PhotoItem>> byKey = new HashMap<>();
        for (PhotoItem item : items) {
            if (item.getSize() <= 0L) {
                continue;
            }
            List<PhotoItem> group = byKey.computeIfAbsent(item.getDuplicateKey(), key -> new ArrayList<>());
            group.add(item);
        }

        List<List<PhotoItem>> groups = new ArrayList<>();
        for (List<PhotoItem> group : byKey.values()) {
            if (group.size() > 1) {
                groups.add(group);
            }
        }
        groups.sort((left, right) -> Long.compare(totalSize(right), totalSize(left)));
        return groups;
    }

    private long totalSize(List<PhotoItem> items) {
        long total = 0L;
        for (PhotoItem item : items) {
            total += Math.max(item.getSize(), 0L);
        }
        return total;
    }

    private void showDuplicateGroup() {
        if (duplicateGroupIndex >= duplicateGroups.size()) {
            if (queuedDeletePhotos.isEmpty()) {
                showCurrentPhotos();
                showIntroHintIfNeeded();
            } else {
                startDeleteReview();
            }
            return;
        }

        editingDeleteList = false;
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        layoutEmptyIcon.setVisibility(View.GONE);
        scrollDeletePreview.setVisibility(View.VISIBLE);

        List<PhotoItem> group = duplicateGroups.get(duplicateGroupIndex);
        for (int i = 1; i < group.size(); i++) {
            PhotoItem item = group.get(i);
            if (!queuedDeletePhotos.contains(item)) {
                queuedDeletePhotos.add(item);
            }
        }

        tvEmptyTitle.setText("Duplicate bulundu");
        tvEmptyDesc.setText((duplicateGroupIndex + 1) + "/" + duplicateGroups.size()
                + " grup · " + group.size()
                + " ayni dosya. Ilkini tutuyoruz; silmek istemedigine dokun.");
        btnGrantPermission.setText(duplicateGroupIndex + 1 < duplicateGroups.size()
                ? "Sonraki Duplicate"
                : "Secilenleri Sil");
        btnGrantPermission.setOnClickListener(view -> {
            duplicateGroupIndex++;
            showDuplicateGroup();
        });
        btnSecondaryAction.setText("Bu Grubu Atla");
        btnSecondaryAction.setVisibility(View.VISIBLE);
        btnSecondaryAction.setOnClickListener(view -> {
            queuedDeletePhotos.removeAll(group);
            duplicateGroupIndex++;
            showDuplicateGroup();
        });
        buildDuplicateGrid(group);
        updateStats();
    }

    private void buildDuplicateGrid(List<PhotoItem> group) {
        gridDeletePreview.removeAllViews();
        int itemSize = dpToPx(92);
        int margin = dpToPx(4);

        for (int i = 0; i < group.size(); i++) {
            PhotoItem item = group.get(i);
            FrameLayout cell = new FrameLayout(this);
            cell.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card));

            ImageView imageView = new ImageView(this);
            if (item.isVideo()) {
                imageView.setImageBitmap(createVideoFrame(item));
            } else {
                imageView.setImageURI(item.getUri());
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setContentDescription(item.getDisplayName());
            cell.addView(imageView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            TextView badge = new TextView(this);
            badge.setGravity(android.view.Gravity.CENTER);
            badge.setTextColor(ContextCompat.getColor(this, R.color.white));
            badge.setTextSize(11f);
            badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);
            badge.setBackgroundColor(queuedDeletePhotos.contains(item) ? 0xCCF05A5F : 0xCC19B77A);
            badge.setText(i == 0 || !queuedDeletePhotos.contains(item) ? "TUT" : "SIL");
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(24),
                    android.view.Gravity.BOTTOM
            );
            cell.addView(badge, badgeParams);

            if (i > 0) {
                cell.setOnClickListener(view -> {
                    if (queuedDeletePhotos.contains(item)) {
                        queuedDeletePhotos.remove(item);
                    } else {
                        queuedDeletePhotos.add(item);
                    }
                    buildDuplicateGrid(group);
                    updateStats();
                });
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = itemSize;
            params.height = itemSize;
            params.setMargins(margin, margin, margin, margin);
            gridDeletePreview.addView(cell, params);
        }
    }

    private Bitmap createVideoFrame(PhotoItem item) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, item.getUri());
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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void onBatchDeleteConfirmed() {
        editingDeleteList = false;
        int count = queuedDeletePhotos.size();
        for (PhotoItem photo : queuedDeletePhotos) {
            reviewStore.removeFavorite(photo.getId());
        }
        for (int i = 0; i < count; i++) {
            reviewStore.addDeleted();
        }
        queuedDeletePhotos.clear();
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        btnFavorite.setVisibility(View.GONE);
        updateCollectionButton();
        tvEmptyTitle.setText("Silme tamamlandı");
        tvEmptyDesc.setText(count + " fotoğraf galeriden silindi.\n\nYeni bir temizlik turu başlatabilirsin.");
        btnGrantPermission.setText("Yeniden Başla");
        btnGrantPermission.setOnClickListener(view -> restartCleaning());
        showToast("Toplu silme tamamlandı");
    }

    private void undoLastAction() {
        if (editingDeleteList) {
            showToast("Bu modda sağa kaydırarak silme listesinden çıkarabilirsin");
            return;
        }

        if (undoStack.isEmpty() || currentIndex == 0) {
            showToast("Geri alınacak işlem yok");
            return;
        }

        UndoAction action = undoStack.removeLast();
        if (action.kept) {
            currentIndex--;
            keptCount--;
            reviewStore.removeKept();
            reviewStore.unmarkReviewed(action.photo.getId());
        } else {
            currentIndex--;
            queuedDeletePhotos.remove(action.photo);
            deletedCount--;
            sessionSavedBytes = Math.max(sessionSavedBytes - Math.max(action.photo.getSize(), 0L), 0L);
            reviewStore.unmarkReviewed(action.photo.getId());
        }
        streakCount = Math.max(streakCount - 1, 0);
        updateStats();
        showCurrentPhotos();
        showToast(action.kept ? "Saklama geri alındı" : "Silme listesinden çıkarıldı");
    }

    private void showPermissionUI() {
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        tvEmptyTitle.setText("Galeriye Erişim Gerekli");
        tvEmptyDesc.setText("Fotoğraflarını inceleyip gereksizleri silmek için galeri iznine ihtiyacımız var.");
        btnGrantPermission.setText("İzin Ver");
        btnGrantPermission.setOnClickListener(view -> permissionLauncher.launch(requiredPermissions()));
    }

    private void showPermissionDenied() {
        layoutEmpty.setVisibility(View.VISIBLE);
        swipeCardView.setVisibility(View.GONE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        tvEmptyTitle.setText("İzin Verilmedi");
        tvEmptyDesc.setText("Uygulama galeriye erişemeden çalışamaz. Ayarlardan izin verebilir veya tekrar deneyebilirsin.");
        btnGrantPermission.setText("Tekrar Dene");
        btnGrantPermission.setOnClickListener(view -> openAppPermissionSettings());
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void showEmptyGallery() {
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        tvEmptyTitle.setText("Fotoğraf Bulunamadı");
        tvEmptyDesc.setText("Galeride gösterilecek fotoğraf yok.");
        btnGrantPermission.setText("Yenile");
        btnGrantPermission.setOnClickListener(view -> loadPhotos());
        updateCounter();
        updateProgressBar();
    }

    private void showAllDone() {
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        btnFavorite.setVisibility(View.GONE);
        btnReviewSelected.setVisibility(View.GONE);
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        tvEmptyTitle.setText("Hepsi tamam!");
        if (queuedDeletePhotos.isEmpty()) {
            tvEmptyDesc.setText(keptCount + " fotoğraf saklandı.\n\nSilinecek fotoğraf seçmedin.");
            btnGrantPermission.setText("Yeniden Başla");
            btnGrantPermission.setOnClickListener(view -> restartCleaning());
        } else {
            tvEmptyDesc.setText(queuedDeletePhotos.size() + " fotoğraf silme listesinde, " + keptCount
                    + " fotoğraf saklandı.\n\nSilmeden önce seçilenleri tekrar gözden geçirebilirsin.");
            btnGrantPermission.setText("Seçilenleri Gözden Geçir");
            btnGrantPermission.setOnClickListener(view -> startDeleteReview());
        }
        updateCounter();
        updateProgressBar();
    }

    private void restartCleaning() {
        editingDeleteList = false;
        hideDeletePreviewGrid();
        deletedCount = 0;
        keptCount = 0;
        streakCount = 0;
        sessionSavedBytes = 0L;
        queuedDeletePhotos.clear();
        loadPhotos();
    }

    private void updateCounter() {
        updateStats();
    }

    private void updateStats() {
        int remaining = editingDeleteList
                ? Math.max(queuedDeletePhotos.size() - currentIndex, 0)
                : Math.max(photos.size() - currentIndex, 0);
        tvSavedMb.setText(formatBytes(sessionSavedBytes) + " kazanıldı");
        tvStreakLabel.setText("Seri " + streakCount + "  |  Kalan " + remaining
                + "  |  Sil " + deletedCount + "  |  Tut " + keptCount);
    }

    private void updateProgressBar() {
        if (photos.isEmpty()) {
            progressBar.setProgress(0);
            updateScoreProgress();
            return;
        }
        progressBar.setProgress((int) ((currentIndex / (float) photos.size()) * 100));
        updateScoreProgress();
    }

    private void updateScoreProgress() {
        if (scoreProgress == null) {
            return;
        }
        long totalBytes = getDeviceStorageBytes();
        double percent = totalBytes <= 0L ? 0.0 : (sessionSavedBytes * 100.0) / totalBytes;
        int progress = (int) Math.min(100, Math.round(percent));
        if (sessionSavedBytes > 0L) {
            progress = Math.max(progress, 2);
        }
        scoreProgress.setProgress(progress);
        if (tvScoreCaption != null) {
            tvScoreCaption.setText(String.format(
                    java.util.Locale.getDefault(),
                    "HAFIZA +%s",
                    formatStoragePercent(percent)
            ));
        }
    }

    private long getDeviceStorageBytes() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            return statFs.getTotalBytes();
        } catch (IllegalArgumentException exception) {
            return 0L;
        }
    }

    private String formatStoragePercent(double percent) {
        if (percent <= 0.0) {
            return "%0.00";
        }
        if (percent < 0.01) {
            return "<%0.01";
        }
        if (percent < 10.0) {
            return String.format(java.util.Locale.getDefault(), "%%%1$.2f", percent);
        }
        return String.format(java.util.Locale.getDefault(), "%%%1$.1f", percent);
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(70L)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(190L)
                        .setInterpolator(new OvershootInterpolator(1.35f))
                        .start())
                .start();
    }

    private void resetButtonScales() {
        btnKeep.setScaleX(1f);
        btnKeep.setScaleY(1f);
        btnDelete.setScaleX(1f);
        btnDelete.setScaleY(1f);
    }

    private void showToast(String message) {
        if (layoutNotice == null) {
            return;
        }
        layoutNotice.animate().cancel();
        tvNoticeMessage.setText(message);
        tvNoticeIcon.setText(iconForNotice(message));
        layoutNotice.setVisibility(View.VISIBLE);
        layoutNotice.setAlpha(0f);
        layoutNotice.setTranslationY(dpToPx(16));
        layoutNotice.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> layoutNotice.postDelayed(() -> layoutNotice.animate()
                        .alpha(0f)
                        .translationY(dpToPx(12))
                        .setDuration(220L)
                        .withEndAction(() -> layoutNotice.setVisibility(View.GONE))
                        .start(), 1700L))
                .start();
    }

    private String iconForNotice(String message) {
        if (message.contains("Geri") || message.contains("geri")) {
            return "↶";
        }
        if (message.contains("Sil") || message.contains("sil")) {
            return "×";
        }
        if (message.contains("Koleksiyon")) {
            return "♥";
        }
        return "✓";
    }

    private void addCurrentPhotoToFavorites() {
        PhotoItem photo = swipeCardView.getCurrentPhoto();
        if (photo == null) {
            return;
        }
        toggleFavorite(photo);
    }

    private void toggleFavorite(PhotoItem photo) {
        boolean isFavorite = reviewStore.getFavoriteIds().contains(String.valueOf(photo.getId()));
        if (isFavorite) {
            reviewStore.removeFavorite(photo.getId());
            showToast("Favorilerden cikarildi");
        } else {
            reviewStore.addFavorite(photo.getId());
            showToast("Favorilere eklendi");
        }
        updateFavoriteButton(photo);
        updateCollectionButton();
    }

    private void updateFavoriteButton(PhotoItem photo) {
        if (btnFavorite == null || reviewStore == null || photo == null) {
            return;
        }
        boolean isFavorite = reviewStore.getFavoriteIds().contains(String.valueOf(photo.getId()));
        btnFavorite.setImageResource(R.drawable.ic_action_heart);
        btnFavorite.setAlpha(isFavorite ? 1f : 0.72f);
    }

    private void updateCollectionButton() {
        if (btnCollection != null && reviewStore != null) {
            btnCollection.setImageResource(R.drawable.ic_action_heart);
            btnCollection.setAlpha(reviewStore.getFavoriteCount() > 0 ? 1f : 0.72f);
        }
    }

    private void updateThemeButton() {
        if (btnTheme == null) {
            return;
        }
        btnTheme.setImageResource(AppPreferences.isDarkMode(this)
                ? R.drawable.ic_action_sun
                : R.drawable.ic_action_moon);
    }

    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(22), dpToPx(20), dpToPx(22), dpToPx(18));
        content.setBackgroundResource(R.drawable.bg_stat_panel);

        TextView title = new TextView(this);
        title.setText("Ayarlar");
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(22f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(title);

        TextView guide = new TextView(this);
        guide.setText("Sola kaydırınca silme listesine alınır ve MB sayacı artar.\nSağa kaydırınca fotoğraf kalır.\nKalp koleksiyona ekler, üstteki kalp koleksiyonu açar.");
        guide.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        guide.setTextSize(14f);
        guide.setLineSpacing(dpToPx(3), 1f);
        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        guideParams.setMargins(0, dpToPx(12), 0, dpToPx(14));
        content.addView(guide, guideParams);

        SwitchMaterial themeSwitch = new SwitchMaterial(this);
        themeSwitch.setText("Gece modu (sonraki açılışta)");
        themeSwitch.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        themeSwitch.setTextSize(15f);
        themeSwitch.setChecked(AppPreferences.isDarkMode(this));
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setDarkMode(this, isChecked);
            updateThemeButton();
            showToast("Tema kaydedildi; temizlik akışın bozulmasın diye sonraki açılışta uygulanacak");
        });
        content.addView(themeSwitch);

        TextView privacyTitle = new TextView(this);
        privacyTitle.setText("SSS / Güven");
        privacyTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        privacyTitle.setTextSize(16f);
        privacyTitle.setTypeface(privacyTitle.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams privacyTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        privacyTitleParams.setMargins(0, dpToPx(18), 0, 0);
        content.addView(privacyTitle, privacyTitleParams);

        TextView privacyBody = new TextView(this);
        privacyBody.setText("Fotoğraflarım yükleniyor mu?\nHayır. Fotoğrafların cihazında kalır; uygulama fotoğraflarını bir sunucuya göndermez.\n\nSilme hemen oluyor mu?\nHayır. Sola kaydırılanlar önce listeye alınır. Gerçek silme, seçilenler ekranından sonra Android'in resmi onayıyla yapılır.\n\nTema değişince neden hemen dönmüyor?\nTemizlik ortasında seçilenler ve sayaç kaybolmasın diye tema tercihi kaydedilir, sonraki açılışta uygulanır.\n\nAçık kaynak mı?\nEvet. Kodunu inceleyebilir, nasıl çalıştığını görebilir ve güvenle kullanabilirsin.");
        privacyBody.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        privacyBody.setTextSize(14f);
        privacyBody.setLineSpacing(dpToPx(3), 1f);
        LinearLayout.LayoutParams privacyBodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        privacyBodyParams.setMargins(0, dpToPx(8), 0, 0);
        content.addView(privacyBody, privacyBodyParams);

        TextView sourceButton = new TextView(this);
        sourceButton.setText("Açık Kaynak Kodunu Gör");
        sourceButton.setGravity(android.view.Gravity.CENTER);
        sourceButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        sourceButton.setTextSize(15f);
        sourceButton.setTypeface(sourceButton.getTypeface(), android.graphics.Typeface.BOLD);
        sourceButton.setBackgroundResource(R.drawable.bg_action_ghost);
        sourceButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Bultimatom/SilGitsinApp"));
            startActivity(intent);
        });
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        );
        sourceParams.setMargins(0, dpToPx(14), 0, 0);
        content.addView(sourceButton, sourceParams);

        TextView appSettings = new TextView(this);
        appSettings.setText("Uygulama izinleri");
        appSettings.setGravity(android.view.Gravity.CENTER);
        appSettings.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        appSettings.setTextSize(15f);
        appSettings.setTypeface(appSettings.getTypeface(), android.graphics.Typeface.BOLD);
        appSettings.setBackgroundResource(R.drawable.bg_action_ghost);
        appSettings.setOnClickListener(view -> openAppPermissionSettings());
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        );
        settingsParams.setMargins(0, dpToPx(10), 0, 0);
        content.addView(appSettings, settingsParams);

        dialog.setContentView(content);
        Window window = dialog.getWindow();
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setLayout(
                    getResources().getDisplayMetrics().widthPixels - dpToPx(34),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void showIntroHintIfNeeded() {
        if (layoutIntroHint == null
                || AppPreferences.isMainHintDone(this)
                || !AppPreferences.isOnboardingDone(this)) {
            return;
        }
        AppPreferences.setMainHintDone(this);
        layoutIntroHint.setVisibility(View.VISIBLE);
        layoutIntroHint.setAlpha(0f);
        layoutIntroHint.setTranslationY(-dpToPx(14));
        layoutIntroHint.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> layoutIntroHint.postDelayed(() -> {
                    tvHintTitle.setText("Kural basit");
                    tvHintBody.setText("Sola kaydır: MB kazan. Sağa kaydır: sakla. Geri ile son hamleyi al.");
                    layoutIntroHint.animate()
                            .translationY(-dpToPx(8))
                            .setDuration(160L)
                            .withEndAction(() -> layoutIntroHint.animate()
                                    .translationY(0f)
                                    .setDuration(220L)
                                    .setInterpolator(new OvershootInterpolator(0.8f))
                                    .start())
                            .start();
                    layoutIntroHint.postDelayed(() -> layoutIntroHint.animate()
                            .alpha(0f)
                            .translationY(-dpToPx(12))
                            .setDuration(260L)
                            .withEndAction(() -> layoutIntroHint.setVisibility(View.GONE))
                            .start(), 2600L);
                }, 1100L))
                .start();
    }

    private void pulseScore() {
        tvSavedMb.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(120L)
                .withEndAction(() -> tvSavedMb.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220L)
                        .setInterpolator(new OvershootInterpolator(0.9f))
                        .start())
                .start();
        if (scoreProgress != null) {
            scoreProgress.animate()
                    .scaleY(1.8f)
                    .setDuration(120L)
                    .withEndAction(() -> scoreProgress.animate()
                            .scaleY(1f)
                            .setDuration(220L)
                            .setInterpolator(new OvershootInterpolator(0.9f))
                            .start())
                    .start();
        }
    }

    private String formatBytes(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        if (mb < 10) {
            return String.format(java.util.Locale.getDefault(), "%.1f MB", mb);
        }
        return String.format(java.util.Locale.getDefault(), "%.0f MB", mb);
    }

    private static class UndoAction {
        private final PhotoItem photo;
        private final boolean kept;

        private UndoAction(PhotoItem photo, boolean kept) {
            this.photo = photo;
            this.kept = kept;
        }
    }
}
