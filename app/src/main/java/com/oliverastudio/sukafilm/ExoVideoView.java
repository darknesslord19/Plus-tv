package com.oliverastudio.sukafilm;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/** ExoPlayer behind the small subset of the VideoView API that the detail screen uses. */
public class ExoVideoView extends PlayerView {
    public interface OnPreparedListener { void onPrepared(ExoVideoView view); }
    public interface OnBufferingListener { void onBuffering(boolean buffering); }
    public interface OnCompletionListener { void onCompletion(ExoVideoView view); }
    public interface OnErrorListener { void onError(String errorName); }

    private ExoPlayer player;
    private OnPreparedListener preparedListener;
    private OnBufferingListener bufferingListener;
    private OnCompletionListener completionListener;
    private OnErrorListener errorListener;

    public ExoVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUseController(false);
    }

    public void setOnPreparedListener(OnPreparedListener listener) { preparedListener = listener; }
    public void setOnBufferingListener(OnBufferingListener listener) { bufferingListener = listener; }
    public void setOnCompletionListener(OnCompletionListener listener) { completionListener = listener; }
    public void setOnErrorListener(OnErrorListener listener) { errorListener = listener; }

    /** Opens the address and starts playing. HLS is recognised by the .m3u8 extension. */
    public void setVideoURI(Uri uri) {
        stopPlayback();
        player = new ExoPlayer.Builder(getContext()).build();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(), true);
        player.setHandleAudioBecomingNoisy(true);
        player.addListener(new Player.Listener() {
            private boolean prepared;

            @Override
            public void onPlaybackStateChanged(int state) {
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
                if (errorListener != null) errorListener.onError(error.getErrorCodeName());
            }
        });
        setPlayer(player);
        setKeepScreenOn(true);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.setPlayWhenReady(true);
        player.prepare();
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
