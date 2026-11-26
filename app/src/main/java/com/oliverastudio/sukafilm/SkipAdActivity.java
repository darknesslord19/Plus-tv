// ============================================================
//   Project Name  : SUKA FILM
//   Author        : Celine Olivia Clarissa
//   Studio        : Olivera Studio
//   Copyright (c) : 2026 Olivera Studio. All rights reserved.
//   Description   : Turning coffee into code, one line at a time.
// ============================================================
//   Terms & Conditions :
//   - This code is NOT for sale or commercial redistribution.
//   - Modifying, copying, or redistributing any part of this
//     code requires prior written permission from the developer.
//   - For inquiries, please contact the author directly.
// ============================================================
//   Crafted with logic, love, and a dash of magic.
// ============================================================

package com.oliverastudio.sukafilm;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class SkipAdActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnClose;
    private TextView txtAppBarStage;
    private TextView stepIndicator1;
    private TextView stepIndicator2;
    private TextView stepIndicator3;
    private View stepLine1;
    private View stepLine2;

    private ImageView imgMoviePoster;
    private TextView txtMovieTitle;
    private TextView txtMovieQualitySize;

    private TextView txtStageHeading;
    private TextView txtStageDescription;
    private ImageView imgAdBanner;
    private TextView txtAdTitle;
    private TextView txtAdDesc;

    private TextView txtCountdownLabel;
    private ProgressBar progTimerBar;
    private Button btnActionSkip;

    private int currentStage = 1;
    private static final int TOTAL_STAGES = 3;
    private static final long STAGE_DURATION_MS = 10000;
    private CountDownTimer countDownTimer;

    private int movieId;
    private String quality;
    private String size;
    private String defaultUrl;
    private Movie movie;

    private DownloadManagerHelper downloadHelper;

    private final String[] adImages = {
            "https://raw.githubusercontent.com/OliveraStudio/SukaFilm/refs/heads/main/app/src/main/assets/ads_example/example_ads.png",
            "https://raw.githubusercontent.com/OliveraStudio/SukaFilm/refs/heads/main/app/src/main/assets/ads_example/example_ads.png",
            "https://raw.githubusercontent.com/OliveraStudio/SukaFilm/refs/heads/main/app/src/main/assets/ads_example/example_ads.png"
    };

    private final String[] adTitles = {
        "Uygulama fikriniz mi var?",
        "Olivera Studio ile oyun geliştirin",
        "İşletmeniz için modern web sitesi"
    };
        
    private final String[] adDescs = {
        "Fikrinizi Olivera Studio ile modern bir uygulamaya dönüştürün.",
        "2D ve 3D oyun fikirlerinizi hayata geçirin.",
        "Duyarlı ve şık bir web sitesiyle işletmenizi tanıtın."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skip_ad);
        
        getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
		getWindow().getDecorView().setSystemUiVisibility(
		View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		);

        downloadHelper = new DownloadManagerHelper(this);

        movieId = getIntent().getIntExtra("movie_id", 1);
        quality = getIntent().getStringExtra("quality");
        size = getIntent().getStringExtra("size");
        defaultUrl = getIntent().getStringExtra("default_url");
        String customTitle = getIntent().getStringExtra("title_custom");

        movie = PlaylistManager.getInstance().getMovieById(movieId);
        if (movie == null) {
            movie = new Movie(movieId, "Suka Film", "Suka Film", 2024, "2sa 15dk", 8.5, quality != null ? quality : "1080p Full HD",
                    "Aksiyon", "Video indirmeye hazır.", "Yönetmen", "Oyuncular", "", "", defaultUrl != null ? defaultUrl : "",
                    defaultUrl != null ? defaultUrl : "", defaultUrl != null ? defaultUrl : "", defaultUrl != null ? defaultUrl : "",
                    size != null ? size : "2.4 GB", size != null ? size : "1.1 GB", size != null ? size : "500 MB");
        }

        if (customTitle != null && !customTitle.isEmpty() && !customTitle.equalsIgnoreCase(movie.getTitle())) {
            movie = new Movie(movie.getId(), movie.getTitle() + " " + customTitle, movie.getOriginalTitle(),
                    movie.getYear(), movie.getDuration(), movie.getRating(), quality, movie.getGenre(),
                    movie.getDescription(), movie.getDirector(), movie.getCast(), movie.getPosterUrl(),
                    movie.getBackdropUrl(), movie.getStreamUrl(), defaultUrl, defaultUrl, defaultUrl,
                    size, size, size);
        }

        if (quality == null) quality = "1080p Full HD";
        if (size == null) size = "2.4 GB";
        if (defaultUrl == null) defaultUrl = movie.getDownload1080p();

        initViews();
        setupMovieHeader();
        setupClickListeners();
        loadStage(1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_skip_ad_back);
        btnClose = findViewById(R.id.btn_skip_ad_close);
        txtAppBarStage = findViewById(R.id.txt_app_bar_stage);
        stepIndicator1 = findViewById(R.id.step_indicator_1);
        stepIndicator2 = findViewById(R.id.step_indicator_2);
        stepIndicator3 = findViewById(R.id.step_indicator_3);
        stepLine1 = findViewById(R.id.step_line_1);
        stepLine2 = findViewById(R.id.step_line_2);

        imgMoviePoster = findViewById(R.id.img_target_movie_poster);
        txtMovieTitle = findViewById(R.id.txt_target_movie_title);
        txtMovieQualitySize = findViewById(R.id.txt_target_movie_quality_size);

        txtStageHeading = findViewById(R.id.txt_stage_heading);
        txtStageDescription = findViewById(R.id.txt_stage_description);
        imgAdBanner = findViewById(R.id.img_ad_banner);
        txtAdTitle = findViewById(R.id.txt_ad_title);
        txtAdDesc = findViewById(R.id.txt_ad_desc);

        txtCountdownLabel = findViewById(R.id.txt_countdown_label);
        progTimerBar = findViewById(R.id.prog_timer_bar);
        btnActionSkip = findViewById(R.id.btn_action_skip);
    }

    private void setupMovieHeader() {
        txtMovieTitle.setText(movie.getTitle());
        txtMovieQualitySize.setText("Kalite: " + quality + " • " + size);

        Glide.with(this)
                .load(movie.getPosterUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgMoviePoster);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnClose.setOnClickListener(v -> finish());

        btnActionSkip.setOnClickListener(v -> {
            if (currentStage == 1) {
                loadStage(2);
            } else if (currentStage == 2) {
                loadStage(3);
            } else if (currentStage == 3) {
                startMovieDownloadProcess();
            }
        });
    }

    private void loadStage(int stage) {
        currentStage = stage;
        updateStepperUI(stage);

        txtAppBarStage.setText("İndirme adımı (" + stage + "/3)");

        if (stage == 1) {
            txtStageHeading.setText("Adım 1 / 3");
            txtStageDescription.setText("İndirmeden önce lütfen bekleyin.");
        } else if (stage == 2) {
            txtStageHeading.setText("Adım 2 / 3");
            txtStageDescription.setText("İndirmeden önce lütfen bekleyin.");
        } else {
            txtStageHeading.setText("Adım 3 / 3");
            txtStageDescription.setText("Video /Suka Film/ klasörüne kaydedilecek.");
        }

        int adIndex = stage - 1;
        txtAdTitle.setText(adTitles[adIndex]);
        txtAdDesc.setText(adDescs[adIndex]);
        Glide.with(this)
                .load(adImages[adIndex])
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgAdBanner);

        startCountDownTimer(stage);
    }

    private void updateStepperUI(int stage) {
        if (stage == 1) {
            stepIndicator1.setBackgroundResource(R.drawable.bg_badge_gold);
            stepIndicator1.setTextColor(0xFF0B0E14);
            stepLine1.setBackgroundColor(0xFF2A3245);
            stepIndicator2.setBackgroundResource(R.drawable.bg_badge_dark);
            stepIndicator2.setTextColor(0xFF94A3B8);
            stepLine2.setBackgroundColor(0xFF2A3245);
            stepIndicator3.setBackgroundResource(R.drawable.bg_badge_dark);
            stepIndicator3.setTextColor(0xFF94A3B8);
        } else if (stage == 2) {
            stepIndicator1.setBackgroundResource(R.drawable.bg_badge_gold);
            stepIndicator1.setTextColor(0xFF0B0E14);
            stepLine1.setBackgroundColor(0xFFF5C518);
            stepIndicator2.setBackgroundResource(R.drawable.bg_badge_gold);
            stepIndicator2.setTextColor(0xFF0B0E14);
            stepLine2.setBackgroundColor(0xFF2A3245);
            stepIndicator3.setBackgroundResource(R.drawable.bg_badge_dark);
            stepIndicator3.setTextColor(0xFF94A3B8);
        } else {
            stepIndicator1.setBackgroundResource(R.drawable.bg_badge_gold);
            stepIndicator1.setTextColor(0xFF0B0E14);
            stepLine1.setBackgroundColor(0xFFF5C518);
            stepIndicator2.setBackgroundResource(R.drawable.bg_badge_gold);
            stepIndicator2.setTextColor(0xFF0B0E14);
            stepLine2.setBackgroundColor(0xFFF5C518);
            stepIndicator3.setBackgroundResource(R.drawable.bg_btn_green);
            stepIndicator3.setTextColor(0xFFFFFFFF);
        }
    }

    private void startCountDownTimer(int stage) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnActionSkip.setEnabled(false);
        btnActionSkip.setBackgroundResource(R.drawable.bg_btn_dark);
        btnActionSkip.setTextColor(0xFF94A3B8);
        btnActionSkip.setText("Reklam bekleniyor (10 sn)");
        progTimerBar.setProgress(0);

        countDownTimer = new CountDownTimer(STAGE_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) Math.ceil(millisUntilFinished / 1000.0);
                txtCountdownLabel.setText("Kalan süre: " + secondsRemaining + " saniye");
                btnActionSkip.setText("Reklam bekleniyor (" + secondsRemaining + " sn)");
                progTimerBar.setProgress(10 - secondsRemaining);
            }

            @Override
            public void onFinish() {
                progTimerBar.setProgress(10);
                txtCountdownLabel.setText("Süre doldu, devam edebilirsiniz.");
                btnActionSkip.setEnabled(true);

                if (stage == 1) {
                    btnActionSkip.setBackgroundResource(R.drawable.bg_btn_gold);
                    btnActionSkip.setTextColor(0xFF0B0E14);
                    btnActionSkip.setText("Reklamı geç: Adım 2");
                } else if (stage == 2) {
                    btnActionSkip.setBackgroundResource(R.drawable.bg_btn_gold);
                    btnActionSkip.setTextColor(0xFF0B0E14);
                    btnActionSkip.setText("Reklamı geç: Son adım");
                } else {
                    btnActionSkip.setBackgroundResource(R.drawable.bg_btn_green);
                    btnActionSkip.setTextColor(0xFFFFFFFF);
                    btnActionSkip.setText("İndirmeyi başlat");
                }
            }
        }.start();
    }

    private void startMovieDownloadProcess() {
        // Android 9 and older need runtime storage permission to save into Downloads.
        if (android.os.Build.VERSION.SDK_INT <= 28
                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Toast.makeText(this, "Depolama izni verin, sonra düğmeye tekrar dokunun.", Toast.LENGTH_LONG).show();
            return;
        }
        String path = android.net.Uri.parse(defaultUrl).getPath();
        String lowerPath = path == null ? "" : path.toLowerCase(java.util.Locale.ROOT);
        if (lowerPath.endsWith(".m3u8") || lowerPath.endsWith(".m3u")) {
            Toast.makeText(this, "HLS (m3u8) yayınları indirilemez. Oynat düğmesini kullanın.", Toast.LENGTH_LONG).show();
            return;
        }

        btnActionSkip.setEnabled(false);
        downloadHelper.startDownload(movie, quality, defaultUrl, size);
        Toast.makeText(this, "İndirme başladı: /Suka Film/", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, DownloadsActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!btnActionSkip.isEnabled()) {
            startCountDownTimer(currentStage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}