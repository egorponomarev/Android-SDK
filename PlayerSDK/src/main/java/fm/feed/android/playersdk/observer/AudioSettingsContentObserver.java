package fm.feed.android.playersdk.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

/**
 * Author: "adi" http://stackoverflow.com/questions/6896746/android-is-there-a-broadcast-action-for-volume-changes<br/>
 * <p/>
 * Edited by mharkins on 9/25/14.
 */
public class AudioSettingsContentObserver extends ContentObserver {
    int mPreviousVolume;
    Context mContext;

    private VolumeListener mVolumeListener;

    public interface VolumeListener {
        public void onChange(int volume, boolean increased);
    }

    public AudioSettingsContentObserver(Context context, Handler handler, VolumeListener volumeListener) {
        super(handler);
        this.mContext = context;
        this.mVolumeListener = volumeListener;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mPreviousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta= mPreviousVolume -currentVolume;

        if(delta>0)
        {
            mPreviousVolume =currentVolume;
            if (mVolumeListener != null) {
                mVolumeListener.onChange(currentVolume, true);
            }
        }
        else if(delta<0)
        {
            mPreviousVolume =currentVolume;
            if (mVolumeListener != null) {
                mVolumeListener.onChange(currentVolume, false);
            }
        }
    }

    public int getCurrentVolume() {
        return mPreviousVolume;
    }
}
