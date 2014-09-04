package fm.feed.android.playersdk.service.webservice.model;

/**
 * Created by mharkins on 8/29/14.
 */

public enum AudioFormat {
    MP3("mp3"),
    AAC("acc");


    private String mValue;

    AudioFormat(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}
