package fm.feed.android.playersdk.util;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by mharkins on 9/5/14.
 */
public class AudioFocusManager {
    private static final String TAG = AudioFocusManager.class.getSimpleName();

    private boolean mAudioFocusGranted = false;
    private boolean mWasPlayingWhenTransientLoss = false;

    private static final int UNKNOWN = -1;
    private int lastKnownAudioFocusState = -1;

    private Context mContext;
    private Listener mListener;

    public AudioFocusManager(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        init();
    }

    private void init() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mAudioFocusGranted = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (!mAudioFocusGranted) {
            Log.w(TAG, "Audio Focus was not granted!");
        }
    }

    public void release() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(mAudioFocusChangeListener);
        mAudioFocusGranted = false;
    }

    public boolean isAudioFocusGranted() {
        return mAudioFocusGranted;
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mAudioFocusGranted = true;

                    switch (lastKnownAudioFocusState) {
                        case UNKNOWN:
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (mWasPlayingWhenTransientLoss) {
                                mListener.play();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mListener.restoreVolume();
                            break;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // You have lost the audio focus for a presumably long time.
                    // You must skip all audio playback.
                    // Because you should expect not to have focus back for a long time, this would be a good place to clean up your resources as much as possible.
                    // For example, you should release the MediaPlayer.
                    mAudioFocusGranted = false;

                    mListener.releaseResources();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // You have temporarily lost audio focus, but should receive it back shortly.
                    // You must skip all audio playback, but you can keep your resources because you will probably get focus back shortly.
                    mAudioFocusGranted = false;

                    mWasPlayingWhenTransientLoss = mListener.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // allowed to continue to play audio quietly (at a low volume) instead of killing audio completely.
                    // You have temporarily lost audio focus, but you are
                    mAudioFocusGranted = true;

                    mListener.duckVolume();
                    break;
                default:
                    break;
            }

            lastKnownAudioFocusState = focusChange;
        }
    };

    public interface Listener {
        /**
         * Resume Playback
         */
        public void play();

        /**
         * Should Pause playback.
         * <p>
         * You have temporarily lost audio focus, but should receive it back shortly.
         * You must skip all audio playback, but you can keep your resources because you will probably get focus back shortly.
         * </p>
         *
         * @return {@code true} if the Playback was playing and was paused. {@code false} if the playback wasn't playing
         */
        public boolean pause();

        /**
         * Should release as much resources as possible. Audio Focus Gain will probably not regained anytime soon.<br/>
         * <p>
         * You have lost the audio focus for a presumably long time.<br/>
         * You must skip all audio playback.<br/>
         * Because you should expect not to have focus back for a long time, this would be a good place to clean up your resources as much as possible.<br/>
         * For example, you should release the MediaPlayer.
         * </p>
         */
        public void releaseResources();

        /**
         * Silence the Volume while the app is Ducking.
         * <p>
         * allowed to continue to play audio quietly (at a low volume) instead of killing audio completely.
         * You have temporarily lost audio focus, but you are
         * </p>
         */
        public void duckVolume();

        /**
         * Set the volume back to what it was before the app was asked to Duck
         */
        public void restoreVolume();
    }
}
