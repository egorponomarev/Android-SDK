package fm.feed.android.playersdk.service.webservice.util;

import android.util.Base64;

import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.webservice.model.AudioFormat;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 8/22/14.
 */
public class WebserviceUtils {
    private WebserviceUtils() {
    }

    public static String getAuth(Credentials credentials) {
        String concat = credentials.getToken() + ":" + credentials.getSecret();
        String base64Auth = Base64.encodeToString(concat.getBytes(), Base64.NO_WRAP);
        return "Basic " + base64Auth;
    }

    /**
     * Serialize a list of {@link AudioFormat} into a comma separated list.
     *
     * @param audioFormats
     *         a {@link AudioFormat}[] list
     *
     * @return Comma separated list of audio formats. {@code null} if no {@code audioFormats} specified.
     */
    public static String getAudioFormatStr(AudioFormat[] audioFormats) {
        if (audioFormats != null && audioFormats.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (AudioFormat audioFormat : audioFormats) {
                sb.append(audioFormat.getValue())
                        .append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return null;
    }

}
