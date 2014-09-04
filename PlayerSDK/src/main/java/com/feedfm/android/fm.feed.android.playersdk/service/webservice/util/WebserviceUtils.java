package fm.feed.android.playersdk.service.webservice.util;

import android.util.Base64;

import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.webservice.model.AudioFormat;

/**
 * Created by mharkins on 8/22/14.
 */
public class WebserviceUtils {
    private WebserviceUtils() {}

    public static String getAuth(Credentials credentials) {
        String concat = credentials.getToken() + ":" + credentials.getSecret();
        String base64Auth = Base64.encodeToString(concat.getBytes(), Base64.NO_WRAP | Base64.DEFAULT);
        return "Basic " + base64Auth;
    }

    /**
     * Serialize a list of {@link AudioFormat} into a comma separated list.
     *
     * @param audioFormats a {@link AudioFormat}[] list
     * @return Comma separated list of audio formats. {@code null} if no {@code audioFormats} specified.
     */
    public static String getAudioFormatStr(AudioFormat[] audioFormats) {
        if (audioFormats != null && audioFormats.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (AudioFormat audioFormat: audioFormats) {
                sb.append(audioFormat.getValue())
                        .append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return null;
    }

}
