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

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imgBackdrop;
    private ImageView imgPoster;
    private TextView txtTitle;
    private TextView txtOriginalTitle;
    private TextView txtRating;
    private TextView txtQualityTag;
    private TextView txtGenreTag;
    private TextView txtDurationYear;
    private TextView txtSynopsis;
    private TextView txtDirector;
    private TextView txtCast;
    private TextView txtDownloadSummary;

    private LinearLayout btnStream;
    private LinearLayout btnDownload;
    private FrameLayout btnBack;
    private FrameLayout btnDownloadsShortcut;
    private FrameLayout btnBookmark;
    private ImageView imgBookmark;
    private LinearLayout btnPlayTrailerHero;

    private FrameLayout layoutVideoContainer;
    private VideoView videoViewTrailer;
    private View btnVideoFullscreen;
    private boolean isFullscreen = false;
    private int inlineVideoHeight;
    private int inlineBottomPadding;
    private ProgressBar progVideoBuffering;
    private TextView btnCloseVideo;

    private RecyclerView recyclerSimilar;

    private LinearLayout layoutSeasonChips;
    private TextView txtEpisodesCountBadge;
    private RecyclerView recyclerEpisodes;
    private EpisodeAdapter episodeAdapter;
    private int selectedSeasonIndex = 0;

    private TextView btnCustomPlayPause;
    private TextView txtVideoCurrentTime;
    private TextView txtVideoDuration;
    private SeekBar seekVideoProgress;
    private View layoutCustomVideoControls;

    private boolean controlsVisible = true;
    private boolean isUserSeeking = false;
    private boolean isVideoPrepared = false;

    private final Handler videoHandler =
            new Handler(Looper.getMainLooper());

    private final Handler controlsHandler =
            new Handler(Looper.getMainLooper());

    private Movie currentMovie;
    private DownloadManagerHelper downloadHelper;

    private final Runnable hideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoViewTrailer != null
                    && layoutVideoContainer != null
                    && layoutVideoContainer.getVisibility() == View.VISIBLE
                    && videoViewTrailer.isPlaying()) {
                hideVideoControls();
            }
        }
    };

    private final Runnable videoProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoViewTrailer == null
                    || layoutVideoContainer == null
                    || layoutVideoContainer.getVisibility() != View.VISIBLE
                    || !isVideoPrepared) {
                return;
            }

            if (videoViewTrailer.isPlaying() && !isUserSeeking) {
                int duration = videoViewTrailer.getDuration();
                int position = videoViewTrailer.getCurrentPosition();

                if (duration > 0) {
                    seekVideoProgress.setProgress(
                            (int) ((long) position * 1000L / duration)
                    );

                    txtVideoCurrentTime.setText(
                            formatVideoTime(position)
                    );

                    txtVideoDuration.setText(
                            formatVideoTime(duration)
                    );
                }
            }

            videoHandler.postDelayed(this, 500);
        }
    };

    private String formatVideoTime(int milliseconds) {
        int totalSeconds = Math.max(0, milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        downloadHelper = new DownloadManagerHelper(this);

        int movieId = getIntent().getIntExtra("movie_id", 1);

        currentMovie = PlaylistManager.getInstance()
                .getMovieById(movieId);

        if (currentMovie == null) {
            currentMovie = new Movie(
                    movieId,
                    "Suka Film",
                    "Suka Film",
                    2024,
                    "2j 15m",
                    8.5,
                    "1080p FHD",
                    "Aksi",
                    "Playlist sedang dimuat...",
                    "Suka Film Studio",
                    "Pemeran",
                    "", "", "", "", "", "",
                    "1.5 GB",
                    "800 MB",
                    "400 MB"
            );
        }

        if (currentMovie != null) {
            FavoritesHistoryManager.getInstance(this)
                    .recordWatchHistory(currentMovie.getId());
        }

        initViews();
        populateMovieDetails();
        setupSeasonsAndEpisodes();
        setupVideoPlayer();
        setupFullscreen();
        setupClickListeners();
        setupSimilarMovies();
    }

    private void initViews() {
        imgBackdrop = findViewById(R.id.img_detail_backdrop);
        imgPoster = findViewById(R.id.img_detail_poster);

        txtTitle = findViewById(R.id.txt_detail_title);
        txtOriginalTitle =
                findViewById(R.id.txt_detail_original_title);
        txtRating = findViewById(R.id.txt_detail_rating);
        txtQualityTag =
                findViewById(R.id.txt_detail_quality_tag);
        txtGenreTag = findViewById(R.id.txt_detail_genre_tag);
        txtDurationYear =
                findViewById(R.id.txt_detail_duration_year);
        txtSynopsis = findViewById(R.id.txt_detail_synopsis);
        txtDirector = findViewById(R.id.txt_detail_director);
        txtCast = findViewById(R.id.txt_detail_cast);
        txtDownloadSummary =
                findViewById(R.id.txt_download_sizes_summary);

        btnStream = findViewById(R.id.btn_action_stream);
        btnDownload = findViewById(R.id.btn_action_download);
        btnBack = findViewById(R.id.btn_detail_back);

        btnDownloadsShortcut =
                findViewById(R.id.btn_detail_downloads_shortcut);
        btnBookmark = findViewById(R.id.btn_detail_bookmark);
        imgBookmark = findViewById(R.id.img_detail_bookmark);
        btnPlayTrailerHero =
                findViewById(R.id.btn_play_trailer_hero);

        layoutVideoContainer =
                findViewById(R.id.layout_video_player_container);
        videoViewTrailer =
                findViewById(R.id.videoview_trailer);
        progVideoBuffering =
                findViewById(R.id.prog_video_buffering);

        btnCloseVideo =
                findViewById(R.id.btn_close_videoview);

        btnCustomPlayPause =
                findViewById(R.id.btn_custom_play_pause);
        seekVideoProgress =
                findViewById(R.id.seek_video_progress);
        txtVideoCurrentTime =
                findViewById(R.id.txt_video_current_time);
        txtVideoDuration =
                findViewById(R.id.txt_video_duration);

        layoutCustomVideoControls =
                findViewById(R.id.layout_custom_video_controls);

        recyclerSimilar =
                findViewById(R.id.recycler_detail_similar);

        layoutSeasonChips =
                findViewById(R.id.layout_season_chips);
        txtEpisodesCountBadge =
                findViewById(R.id.txt_episodes_count_badge);
        recyclerEpisodes =
                findViewById(R.id.recycler_episodes);
    }

    private void populateMovieDetails() {
        txtTitle.setText(currentMovie.getTitle());
        txtOriginalTitle.setText(currentMovie.getOriginalTitle());
        txtRating.setText(currentMovie.getRatingText());
        txtQualityTag.setText(currentMovie.getQuality());
        txtGenreTag.setText(currentMovie.getGenre());

        txtDurationYear.setText(
                currentMovie.getDuration()
                        + " • Rilis: "
                        + currentMovie.getYearText()
        );

        txtSynopsis.setText(currentMovie.getDescription());
        txtDirector.setText(currentMovie.getDirector());
        txtCast.setText(currentMovie.getCast());

        String summary =
                "Tersedia: 1080p (" + currentMovie.getSize1080p()
                        + ") | 720p (" + currentMovie.getSize720p()
                        + ") | 480p (" + currentMovie.getSize480p()
                        + ")\n"
                        + "Format: MP4 H.264 • Audio AC3 Stereo • "
                        + "Subtitle Indonesia Hardsub";

        txtDownloadSummary.setText(summary);

        Glide.with(this)
                .load(currentMovie.getBackdropUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgBackdrop);

        Glide.with(this)
                .load(currentMovie.getPosterUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgPoster);
    }

    private void setupSeasonsAndEpisodes() {
        if (!currentMovie.hasSeasons()
                || currentMovie.getSeasons() == null
                || currentMovie.getSeasons().isEmpty()) {

            findViewById(R.id.layout_seasons_episodes_section)
                    .setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.layout_seasons_episodes_section)
                .setVisibility(View.VISIBLE);

        recyclerEpisodes.setLayoutManager(
                new LinearLayoutManager(this)
        );

        List<Season> seasons = currentMovie.getSeasons();

        selectedSeasonIndex = 0;
        Season initialSeason = seasons.get(0);

        episodeAdapter = new EpisodeAdapter(
                this,
                initialSeason.getEpisodes(),
                new EpisodeAdapter.OnEpisodeActionListener() {
                    @Override
                    public void onPlayEpisode(Episode episode) {
                        playMovieStreamUrl(
                                episode.getDownloadUrl()
                        );
                    }

                    @Override
                    public void onDownloadEpisode(Episode episode) {
                        launchSkipAdActivity(
                                episode.getQuality(),
                                episode.getFileSize(),
                                episode.getDownloadUrl(),
                                episode.getTitle()
                        );
                    }
                }
        );

        recyclerEpisodes.setAdapter(episodeAdapter);
        layoutSeasonChips.removeAllViews();

        for (int i = 0; i < seasons.size(); i++) {
            Season season = seasons.get(i);
            TextView chip = new TextView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 0, 12, 0);

            chip.setLayoutParams(params);
            chip.setText(season.getSeasonTitle());
            chip.setTextSize(12);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(28, 14, 28, 14);

            final int seasonIdx = i;

            updateSeasonChipStyle(
                    chip,
                    seasonIdx == selectedSeasonIndex
            );

            chip.setOnClickListener(v -> {
                selectedSeasonIndex = seasonIdx;
                refreshSeasonChipsStyle();

                Season selected =
                        currentMovie.getSeasons().get(seasonIdx);

                episodeAdapter.updateEpisodes(
                        selected.getEpisodes()
                );

                txtEpisodesCountBadge.setText(
                        selected.getEpisodes().size() + " Episode"
                );
            });

            layoutSeasonChips.addView(chip);
        }

        txtEpisodesCountBadge.setText(
                initialSeason.getEpisodes().size() + " Episode"
        );
    }

    private void updateSeasonChipStyle(
            TextView chip,
            boolean selected) {

        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(0xFF0B0E14);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(0xFF94A3B8);
            chip.setTypeface(
                    null,
                    android.graphics.Typeface.NORMAL
            );
        }
    }

    private void refreshSeasonChipsStyle() {
        for (int i = 0;
             i < layoutSeasonChips.getChildCount();
             i++) {

            View child = layoutSeasonChips.getChildAt(i);

            if (child instanceof TextView) {
                updateSeasonChipStyle(
                        (TextView) child,
                        i == selectedSeasonIndex
                );
            }
        }
    }

    private void setupVideoPlayer() {
        videoViewTrailer.setMediaController(null);

        videoViewTrailer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (controlsVisible) {
                    hideVideoControls();
                } else {
                    showVideoControls();
                }
            }

            return true;
        });

        btnCustomPlayPause.setOnClickListener(v -> {
            if (!isVideoPrepared) {
                return;
            }

            if (videoViewTrailer.isPlaying()) {
                videoViewTrailer.pause();
                btnCustomPlayPause.setText("▶");

                controlsHandler.removeCallbacks(
                        hideControlsRunnable
                );
            } else {
                videoViewTrailer.start();
                btnCustomPlayPause.setText("Ⅱ");

                showVideoControls();
            }
        });

        seekVideoProgress.setMax(1000);

        seekVideoProgress.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        if (!fromUser || !isVideoPrepared) {
                            return;
                        }

                        int duration =
                                videoViewTrailer.getDuration();

                        if (duration > 0) {
                            int position = (int) (
                                    (long) duration
                                            * progress / 1000L
                            );

                            txtVideoCurrentTime.setText(
                                    formatVideoTime(position)
                            );
                        }

                        controlsHandler.removeCallbacks(
                                hideControlsRunnable
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar) {

                        isUserSeeking = true;

                        controlsHandler.removeCallbacks(
                                hideControlsRunnable
                        );
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar) {

                        if (isVideoPrepared) {
                            int duration =
                                    videoViewTrailer.getDuration();

                            if (duration > 0) {
                                int position = (int) (
                                        (long) duration
                                                * seekBar.getProgress()
                                                / 1000L
                                );

                                videoViewTrailer.seekTo(position);
                            }
                        }

                        isUserSeeking = false;
                        scheduleHideControls();
                    }
                }
        );

        videoViewTrailer.setOnPreparedListener(mp -> {
            isVideoPrepared = true;

            progVideoBuffering.setVisibility(View.GONE);

            int duration = videoViewTrailer.getDuration();

            txtVideoDuration.setText(
                    formatVideoTime(duration)
            );

            txtVideoCurrentTime.setText("00:00");
            seekVideoProgress.setProgress(0);

            videoViewTrailer.start();
            btnCustomPlayPause.setText("Ⅱ");

            showVideoControls();

            videoHandler.removeCallbacks(
                    videoProgressRunnable
            );

            videoHandler.post(videoProgressRunnable);
        });

        videoViewTrailer.setOnInfoListener((mp, what, extra) -> {
            if (what
                    == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START) {

                progVideoBuffering.setVisibility(View.VISIBLE);

            } else if (what
                    == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END) {

                progVideoBuffering.setVisibility(View.GONE);
            }

            return false;
        });

        videoViewTrailer.setOnCompletionListener(mp -> {
            controlsHandler.removeCallbacks(
                    hideControlsRunnable
            );

            btnCustomPlayPause.setText("▶");
            seekVideoProgress.setProgress(1000);

            txtVideoCurrentTime.setText(
                    formatVideoTime(
                            videoViewTrailer.getDuration()
                    )
            );

            showVideoControls();
        });

        videoViewTrailer.setOnErrorListener((mp, what, extra) -> {
            isVideoPrepared = false;

            progVideoBuffering.setVisibility(View.GONE);

            controlsHandler.removeCallbacks(
                    hideControlsRunnable
            );

            videoHandler.removeCallbacks(
                    videoProgressRunnable
            );

            Toast.makeText(
                    MovieDetailActivity.this,
                    "Tidak dapat memutar video. "
                            + "Periksa URL atau formatnya.",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        });

        btnCloseVideo.setOnClickListener(
                v -> stopAndHideVideo()
        );
    }

    private void setupFullscreen() {
        btnVideoFullscreen = findViewById(R.id.btn_video_fullscreen);
        btnVideoFullscreen.setOnClickListener(v -> setFullscreen(!isFullscreen));
        // Taps on the black bars around the video also toggle the controls.
        findViewById(R.id.layout_video_player_container).setOnClickListener(v -> {
            if (controlsVisible) hideVideoControls();
            else showVideoControls();
        });
        // Keep the player as tall as the screen, including after rotation.
        findViewById(R.id.vscroll2).addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    View frame = findViewById(R.id.frame_layout68);
                    int height = bottom - top;
                    if (isFullscreen && frame.getLayoutParams().height != height) {
                        v.post(() -> {
                            if (!isFullscreen) return;
                            frame.getLayoutParams().height = height;
                            frame.requestLayout();
                            v.scrollTo(0, 0);
                        });
                    }
                });
    }

    /** Enlarges the player in place; moving the VideoView would restart the video. */
    private void setFullscreen(boolean enable) {
        if (enable == isFullscreen) return;

        ViewGroup root = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        ViewGroup page = findViewById(R.id.linear67);
        View frame = findViewById(R.id.frame_layout68);
        if (enable) {
            inlineVideoHeight = frame.getLayoutParams().height;
            inlineBottomPadding = page.getPaddingBottom();
        }
        isFullscreen = enable;

        // Hide or restore everything except the player, keeping original visibility.
        for (ViewGroup group : new ViewGroup[]{page, root}) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child == frame || child.getId() == R.id.vscroll2) continue;
                if (enable) {
                    child.setTag(child.getVisibility());
                    child.setVisibility(View.GONE);
                } else if (child.getTag() instanceof Integer) {
                    child.setVisibility((Integer) child.getTag());
                    child.setTag(null);
                }
            }
        }

        page.setPadding(page.getPaddingLeft(), page.getPaddingTop(), page.getPaddingRight(),
                enable ? 0 : inlineBottomPadding);
        root.setFitsSystemWindows(!enable);
        if (enable) root.setPadding(0, 0, 0, 0);

        View scroll = findViewById(R.id.vscroll2);
        scroll.scrollTo(0, 0);
        frame.getLayoutParams().height = enable ? scroll.getHeight() : inlineVideoHeight;
        frame.requestLayout();

        setRequestedOrientation(enable
                ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        applySystemBars();
        root.requestApplyInsets();
    }

    private void applySystemBars() {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (isFullscreen) {
            flags |= View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullscreen) applySystemBars();
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            setFullscreen(false);
            return;
        }
        super.onBackPressed();
    }

    private void playMovieStreamUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "URL video belum tersedia",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );

        videoHandler.removeCallbacks(
                videoProgressRunnable
        );

        isVideoPrepared = false;
        isUserSeeking = false;
        controlsVisible = true;

        layoutVideoContainer.setVisibility(View.VISIBLE);
        progVideoBuffering.setVisibility(View.VISIBLE);

        layoutCustomVideoControls.setVisibility(View.VISIBLE);
        btnCustomPlayPause.setVisibility(View.VISIBLE);
        btnCloseVideo.setVisibility(View.VISIBLE);

        btnCustomPlayPause.setText("Ⅱ");

        seekVideoProgress.setProgress(0);
        txtVideoCurrentTime.setText("00:00");
        txtVideoDuration.setText("00:00");

        try {
            videoViewTrailer.setVideoURI(
                    Uri.parse(url.trim())
            );

            videoViewTrailer.requestFocus();

        } catch (Exception e) {
            isVideoPrepared = false;

            progVideoBuffering.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "URL video tidak valid",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void stopAndHideVideo() {
        setFullscreen(false);

        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );

        videoHandler.removeCallbacks(
                videoProgressRunnable
        );

        isVideoPrepared = false;
        isUserSeeking = false;

        if (videoViewTrailer != null) {
            videoViewTrailer.stopPlayback();
        }

        if (progVideoBuffering != null) {
            progVideoBuffering.setVisibility(View.GONE);
        }

        if (layoutVideoContainer != null) {
            layoutVideoContainer.setVisibility(View.GONE);
        }

        if (seekVideoProgress != null) {
            seekVideoProgress.setProgress(0);
        }

        if (txtVideoCurrentTime != null) {
            txtVideoCurrentTime.setText("00:00");
        }

        if (txtVideoDuration != null) {
            txtVideoDuration.setText("00:00");
        }

        controlsVisible = true;

        if (btnCustomPlayPause != null) {
            btnCustomPlayPause.setText("▶");
            btnCustomPlayPause.setVisibility(View.VISIBLE);
        }
    }

    private void showVideoControls() {
        controlsVisible = true;

        if (layoutCustomVideoControls != null) {
            layoutCustomVideoControls.setVisibility(View.VISIBLE);
        }

        if (btnCustomPlayPause != null) {
            btnCustomPlayPause.setVisibility(View.VISIBLE);
        }

        if (btnCloseVideo != null) {
            btnCloseVideo.setVisibility(View.VISIBLE);
        }

        if (btnVideoFullscreen != null) {
            btnVideoFullscreen.setVisibility(View.VISIBLE);
        }

        scheduleHideControls();
    }

    private void hideVideoControls() {
        controlsVisible = false;

        if (btnVideoFullscreen != null) {
            btnVideoFullscreen.setVisibility(View.GONE);
        }

        if (layoutCustomVideoControls != null) {
            layoutCustomVideoControls.setVisibility(View.GONE);
        }

        if (btnCustomPlayPause != null) {
            btnCustomPlayPause.setVisibility(View.GONE);
        }

        if (btnCloseVideo != null) {
            btnCloseVideo.setVisibility(View.GONE);
        }

        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );
    }

    private void scheduleHideControls() {
        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );

        if (layoutVideoContainer != null
                && layoutVideoContainer.getVisibility()
                == View.VISIBLE
                && videoViewTrailer != null
                && videoViewTrailer.isPlaying()) {

            controlsHandler.postDelayed(
                    hideControlsRunnable,
                    3000
            );
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDownloadsShortcut.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MovieDetailActivity.this,
                    DownloadsActivity.class
            );

            startActivity(intent);
        });

        btnStream.setOnClickListener(v ->
                playMovieStreamUrl(
                        currentMovie.getStreamUrl()
                )
        );

        btnPlayTrailerHero.setOnClickListener(v ->
                playMovieStreamUrl(
                        currentMovie.getStreamUrl()
                )
        );

        btnDownload.setOnClickListener(
                v -> showDownloadDialog()
        );

        if (btnBookmark != null && currentMovie != null) {
            updateBookmarkIconState(
                    FavoritesHistoryManager.getInstance(this)
                            .isFavorite(currentMovie.getId())
            );

            btnBookmark.setOnClickListener(v -> {
                boolean isFav =
                        FavoritesHistoryManager.getInstance(this)
                                .toggleFavorite(currentMovie.getId());

                updateBookmarkIconState(isFav);

                Toast.makeText(
                        this,
                        isFav
                                ? "Ditambahkan ke Favorit"
                                : "Dihapus dari Favorit",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    private void updateBookmarkIconState(boolean isFavorite) {
        if (imgBookmark != null) {
            if (isFavorite) {
                imgBookmark.setColorFilter(
                        Color.parseColor("#FACC15")
                );
            } else {
                imgBookmark.setColorFilter(
                        Color.parseColor("#94A3B8")
                );
            }
        }
    }

    private void showDownloadDialog() {
        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(
                R.layout.dialog_download_options
        );

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView txtMovieTitle =
                dialog.findViewById(
                        R.id.txt_dialog_movie_title
                );

        txtMovieTitle.setText(
                "Film: " + currentMovie.getTitle()
        );

        RadioButton radio1080p =
                dialog.findViewById(R.id.radio_1080p);

        RadioButton radio720p =
                dialog.findViewById(R.id.radio_720p);

        RadioButton radio480p =
                dialog.findViewById(R.id.radio_480p);

        TextView txtSize1080p =
                dialog.findViewById(R.id.txt_size_1080p);

        TextView txtSize720p =
                dialog.findViewById(R.id.txt_size_720p);

        TextView txtSize480p =
                dialog.findViewById(R.id.txt_size_480p);

        txtSize1080p.setText(currentMovie.getSize1080p());
        txtSize720p.setText(currentMovie.getSize720p());
        txtSize480p.setText(currentMovie.getSize480p());

        final String[] selectedQuality = {
                "1080p Full HD"
        };

        final String[] selectedUrl = {
                currentMovie.getDownload1080p()
        };

        final String[] selectedSize = {
                currentMovie.getSize1080p()
        };

        dialog.findViewById(
                R.id.opt_quality_1080p
        ).setOnClickListener(v -> {
            radio1080p.setChecked(true);
            radio720p.setChecked(false);
            radio480p.setChecked(false);

            selectedQuality[0] = "1080p Full HD";
            selectedUrl[0] = currentMovie.getDownload1080p();
            selectedSize[0] = currentMovie.getSize1080p();
        });

        dialog.findViewById(
                R.id.opt_quality_720p
        ).setOnClickListener(v -> {
            radio1080p.setChecked(false);
            radio720p.setChecked(true);
            radio480p.setChecked(false);

            selectedQuality[0] = "720p HD";
            selectedUrl[0] = currentMovie.getDownload720p();
            selectedSize[0] = currentMovie.getSize720p();
        });

        dialog.findViewById(
                R.id.opt_quality_480p
        ).setOnClickListener(v -> {
            radio1080p.setChecked(false);
            radio720p.setChecked(false);
            radio480p.setChecked(true);

            selectedQuality[0] = "480p SD";
            selectedUrl[0] = currentMovie.getDownload480p();
            selectedSize[0] = currentMovie.getSize480p();
        });

        dialog.findViewById(
                R.id.btn_dialog_close
        ).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(
                R.id.btn_confirm_download
        ).setOnClickListener(v -> {
            dialog.dismiss();

            launchSkipAdActivity(
                    selectedQuality[0],
                    selectedSize[0],
                    selectedUrl[0],
                    currentMovie.getTitle()
            );
        });

        dialog.show();
    }

    private void launchSkipAdActivity(
            String quality,
            String size,
            String url,
            String title) {

        Intent intent = new Intent(
                MovieDetailActivity.this,
                SkipAdActivity.class
        );

        intent.putExtra(
                "movie_id",
                currentMovie.getId()
        );

        intent.putExtra("quality", quality);
        intent.putExtra("size", size);
        intent.putExtra("default_url", url);
        intent.putExtra("title_custom", title);

        startActivity(intent);
    }

    private void setupSimilarMovies() {
        List<Movie> similarList =
                PlaylistManager.getInstance()
                        .getCurrentMovies();

        recyclerSimilar.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        MovieCardAdapter adapter = new MovieCardAdapter(
                this,
                similarList,
                false,
                movie -> {
                    Intent intent = new Intent(
                            MovieDetailActivity.this,
                            MovieDetailActivity.class
                    );

                    intent.putExtra(
                            "movie_id",
                            movie.getId()
                    );

                    startActivity(intent);
                    finish();
                }
        );

        recyclerSimilar.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );

        if (videoViewTrailer != null
                && videoViewTrailer.isPlaying()) {

            videoViewTrailer.pause();

            if (btnCustomPlayPause != null) {
                btnCustomPlayPause.setText("▶");
            }
        }
    }

    @Override
    protected void onDestroy() {
        controlsHandler.removeCallbacks(
                hideControlsRunnable
        );

        videoHandler.removeCallbacks(
                videoProgressRunnable
        );

        if (videoViewTrailer != null) {
            videoViewTrailer.stopPlayback();
        }

        super.onDestroy();
    }
}