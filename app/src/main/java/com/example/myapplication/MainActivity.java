package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeCardView.SwipeListener {
    private MediaRepository mediaRepository;
    private ReviewStore reviewStore;
    private SwipeCardView swipeCardView;
    private View layoutEmpty;
    private TextView tvPhotoCount;
    private TextView tvDeletedCount;
    private TextView tvKeptCount;
    private TextView tvEmptyTitle;
    private TextView tvEmptyDesc;
    private View layoutEmptyIcon;
    private TextView btnGrantPermission;
    private TextView btnDelete;
    private TextView btnUndo;
    private TextView btnKeep;
    private TextView btnSecondaryAction;
    private ScrollView scrollDeletePreview;
    private GridLayout gridDeletePreview;
    private ProgressBar progressBar;

    private final List<PhotoItem> photos = new ArrayList<>();
    private final List<PhotoItem> queuedDeletePhotos = new ArrayList<>();
    private final ArrayDeque<UndoAction> undoStack = new ArrayDeque<>();
    private int currentIndex = 0;
    private int deletedCount = 0;
    private int keptCount = 0;
    private boolean editingDeleteList = false;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupSystemBars();
        mediaRepository = new MediaRepository(this);
        reviewStore = new ReviewStore(this);
        setupListeners();
        checkPermissions();
    }

    private void bindViews() {
        swipeCardView = findViewById(R.id.swipeCardView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        tvDeletedCount = findViewById(R.id.tvDeletedCount);
        tvKeptCount = findViewById(R.id.tvKeptCount);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        layoutEmptyIcon = findViewById(R.id.layoutEmptyIcon);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        btnDelete = findViewById(R.id.btnDelete);
        btnUndo = findViewById(R.id.btnUndo);
        btnKeep = findViewById(R.id.btnKeep);
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction);
        scrollDeletePreview = findViewById(R.id.scrollDeletePreview);
        gridDeletePreview = findViewById(R.id.gridDeletePreview);
        progressBar = findViewById(R.id.progressBar);
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
        btnGrantPermission.setOnClickListener(view -> permissionLauncher.launch(requiredPermissions()));
        btnKeep.setOnClickListener(view -> {
            animateButton(btnKeep);
            swipeCardView.swipeRight();
        });
        btnDelete.setOnClickListener(view -> {
            animateButton(btnDelete);
            swipeCardView.swipeLeft();
        });
        btnUndo.setOnClickListener(view -> {
            animateButton(btnUndo);
            undoLastAction();
        });
        findViewById(R.id.btnMainHome).setOnClickListener(view -> finish());
        findViewById(R.id.btnMainGuide).setOnClickListener(view ->
                startActivity(new Intent(this, GuideActivity.class)));
        findViewById(R.id.btnMainStats).setOnClickListener(view ->
                startActivity(new Intent(this, StatsActivity.class)));
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
            return new String[] { Manifest.permission.READ_MEDIA_IMAGES };
        }
        return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
    }

    private void loadPhotos() {
        editingDeleteList = false;
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<PhotoItem> loadedPhotos = mediaRepository.loadShuffledPhotos(reviewStore.getReviewedIds());
            runOnUiThread(() -> {
                photos.clear();
                photos.addAll(loadedPhotos);
                currentIndex = 0;
                queuedDeletePhotos.clear();
                undoStack.clear();
                updateStats();

                if (photos.isEmpty()) {
                    showEmptyGallery();
                } else {
                    showCurrentPhotos();
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
        PhotoItem current = photos.get(currentIndex);
        PhotoItem next = currentIndex + 1 < photos.size() ? photos.get(currentIndex + 1) : null;
        swipeCardView.setPhotos(current, next);
        updateCounter();
        updateProgressBar();
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

    private void showPhotoPreview(PhotoItem photo) {
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

        editingDeleteList = false;
        boolean deletedImmediately = MediaDeleteHelper.requestDelete(this, queuedDeletePhotos, deleteLauncher);
        if (deletedImmediately) {
            onBatchDeleteConfirmed();
        }
    }

    private void startDeleteReview() {
        if (queuedDeletePhotos.isEmpty()) {
            restartCleaning();
            return;
        }

        editingDeleteList = false;
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
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
        tvPhotoCount.setText("0");
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
        btnSecondaryAction.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeCardView.setVisibility(View.VISIBLE);
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
        PhotoItem current = queuedDeletePhotos.get(currentIndex);
        PhotoItem next = currentIndex + 1 < queuedDeletePhotos.size()
                ? queuedDeletePhotos.get(currentIndex + 1)
                : null;
        swipeCardView.setPhotos(current, next);
        tvPhotoCount.setText(String.valueOf(queuedDeletePhotos.size() - currentIndex));
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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void onBatchDeleteConfirmed() {
        editingDeleteList = false;
        int count = queuedDeletePhotos.size();
        for (int i = 0; i < count; i++) {
            reviewStore.addDeleted();
        }
        queuedDeletePhotos.clear();
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
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
            reviewStore.unmarkReviewed(action.photo.getId());
        }
        updateStats();
        showCurrentPhotos();
        showToast(action.kept ? "Saklama geri alındı" : "Silme listesinden çıkarıldı");
    }

    private void showPermissionUI() {
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
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
        hideDeletePreviewGrid();
        btnSecondaryAction.setVisibility(View.GONE);
        tvEmptyTitle.setText("İzin Verilmedi");
        tvEmptyDesc.setText("Uygulama galeriye erişemeden çalışamaz. Ayarlardan izin verebilir veya tekrar deneyebilirsin.");
        btnGrantPermission.setText("Tekrar Dene");
        btnGrantPermission.setOnClickListener(view -> permissionLauncher.launch(requiredPermissions()));
    }

    private void showEmptyGallery() {
        swipeCardView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
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
        queuedDeletePhotos.clear();
        loadPhotos();
    }

    private void updateCounter() {
        tvPhotoCount.setText(String.valueOf(Math.max(photos.size() - currentIndex, 0)));
    }

    private void updateStats() {
        tvDeletedCount.setText("Silinecek: " + deletedCount);
        tvKeptCount.setText("Saklanan: " + keptCount);
    }

    private void updateProgressBar() {
        if (photos.isEmpty()) {
            progressBar.setProgress(0);
            return;
        }
        progressBar.setProgress((int) ((currentIndex / (float) photos.size()) * 100));
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.86f)
                .scaleY(0.86f)
                .setDuration(80L)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160L)
                        .setInterpolator(new OvershootInterpolator())
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
