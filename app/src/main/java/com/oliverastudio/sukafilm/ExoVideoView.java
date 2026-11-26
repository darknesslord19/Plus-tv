package com.oliverastudio.sukafilm;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

/** ExoPlayer behind the small subset of the VideoView API that the detail screen uses. */
public class ExoVideoView extends PlayerView {
    public interface OnPreparedListener { void onPrepared(ExoVideoView view); }
    public interface OnBufferingListener { void onBuffering(boolean buffering); }
    public interface OnCompletionListener { void onCompletion(ExoVideoView view); }
    public interface OnErrorListener { void onError(String errorName); }

    private static final String WIKIMEDIA_USER_AGENT =
            "SukaFilm/1.0 (https://github.com/darknesslord19/Plus-tv) AndroidXMedia3/1.4.1";

    private ExoPlayer player;
    // On-screen playback state, so a black screen can be diagnosed from a screenshot.
    private final TextView status;
    private boolean frameDrawn;
    private OnPreparedListener preparedListener;
    private OnBufferingListener bufferingListener;
    private OnCompletionListener completionListener;
    private OnErrorListener errorListener;

    public ExoVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUseController(false);
        status = new TextView(context);
        status.setTextColor(0xFFFFFFFF);
        status.setBackgroundColor(0xAA000000);
        status.setTextSize(12f);
        status.setPadding(16, 8, 16, 8);
        FrameLayout overlay = getOverlayFrameLayout();
        if (overlay != null) {
            overlay.addView(status, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
        }
    }

    public void setOnPreparedListener(OnPreparedListener listener) { preparedListener = listener; }
    public void setOnBufferingListener(OnBufferingListener listener) { bufferingListener = listener; }
    public void setOnCompletionListener(OnCompletionListener listener) { completionListener = listener; }
    public void setOnErrorListener(OnErrorListener listener) { errorListener = listener; }

    /** Opens the address and starts playing. HLS is recognised by the .m3u8 extension. */
    public void setVideoURI(Uri uri) {
        stopPlayback();
        frameDrawn = false;
        status.setVisibility(VISIBLE);
        status.setText("Durum: bağlanıyor");
        ExoPlayer.Builder builder = new ExoPlayer.Builder(getContext());
        String host = uri.getHost();
        if (host != null && host.endsWith(".wikimedia.org")) {
            // Wikimedia's User-Agent policy asks clients to name themselves and give a contact URL.
            DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
                    .setUserAgent(WIKIMEDIA_USER_AGENT);
            builder.setMediaSourceFactory(new DefaultMediaSourceFactory(getContext())
                    .setDataSourceFactory(new DefaultDataSource.Factory(getContext(), http)));
        }
        player = builder.build();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(), true);
        player.setHandleAudioBecomingNoisy(true);
        player.addListener(new Player.Listener() {
            private boolean prepared;

            @Override
            public void onRenderedFirstFrame() {
                frameDrawn = true;
                showState();
            }

            @Override
            public void onVideoSizeChanged(@NonNull VideoSize size) {
                showState();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                showState();
                if (bufferingListener != null) bufferingListener.onBuffering(state == Player.STATE_BUFFERING);
                if (state == Player.STATE_READY && !prepared) {
                    prepared = true;
                    if (preparedListener != null) preparedListener.onPrepared(ExoVideoView.this);
                } else if (state == Player.STATE_ENDED && completionListener != null) {
                    completionListener.onCompletion(ExoVideoView.this);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                String details = error.getErrorCodeName();
                status.setVisibility(VISIBLE);
                status.setText("Durum: hata " + details);
                Throwable cause = error.getCause();
                if (cause instanceof HttpDataSource.InvalidResponseCodeException) {
                    details += " (HTTP " + ((HttpDataSource.InvalidResponseCodeException) cause).responseCode + ")";
                }
                if (errorListener != null) errorListener.onError(details);
            }
        });
        setPlayer(player);
        setKeepScreenOn(true);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.setPlayWhenReady(true);
        player.prepare();
    }

    private void showState() {
        // After an error the player goes idle; keep the error text instead of overwriting it.
        if (player == null || player.getPlayerError() != null) return;
        int state = player.getPlaybackState();
        String name = state == Player.STATE_BUFFERING ? "yükleniyor"
                : state == Player.STATE_READY ? "hazır"
                : state == Player.STATE_ENDED ? "bitti" : "bekliyor";
        VideoSize size = player.getVideoSize();
        status.setText("Durum: " + name + " | video " + size.width + "x" + size.height
                + " | kare " + (frameDrawn ? "çizildi" : "yok")
                + " | alan " + getWidth() + "x" + getHeight());
    }

    public void start() {
        if (player == null) return;
        if (player.getPlaybackState() == Player.STATE_ENDED) player.seekTo(0);
        player.play();
    }

    public void pause() {
        if (player != null) player.pause();
    }

    /** True also while buffering, so leaving the screen pauses a stream that is still loading. */
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady()
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlaybackState() != Player.STATE_ENDED;
    }

    /** Milliseconds, or 0 when unknown (for example a live stream). */
    public int getDuration() {
        if (player == null || player.isCurrentMediaItemLive()) return 0;
        long duration = player.getDuration();
        return duration == C.TIME_UNSET ? 0 : (int) duration;
    }

    public int getCurrentPosition() {
        return player == null ? 0 : (int) player.getCurrentPosition();
    }

    public void seekTo(int positionMs) {
        if (player != null) player.seekTo(positionMs);
    }

    public void stopPlayback() {
        if (player == null) return;
        setPlayer(null);
        setKeepScreenOn(false);
        player.release();
        player = null;
    }
}
