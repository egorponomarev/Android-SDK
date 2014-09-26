package fm.feed.android.playersdk.service.constant;

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
 * Created by mharkins on 9/11/14.
 */
public class Configuration {
    /** Maximum number of retry attempts for failed tasks (usually when there's a connectivity problem). */
    public static final int MAX_TASK_RETRY_ATTEMPTS = 1;
    /** Publish progress should only be sent out to the front of the application every X seconds. */
    public static final int PROGRESS_PUBLISH_INTERVAL = 500; // 0.5 seconds

    /** Elapsed time of the song is published every 10 seconds */
    public static final int ELAPSED_PING_INTERVAL = 10000; // 10 seconds

    /**
     * Maximum number of Force Skips in a row.
     * This would happen when an audio file can't be parsed properly by the Media Player.
     */
    public static final int MAX_FORCE_SKIP_COUNT = 2;

    public static final int DEFAULT_BITRATE = 64;
    public static final AudioFormat[] DEFAULT_AUDIO_FORMAT = new AudioFormat[] { AudioFormat.AAC };


    public static final String WIFI_LOCK_TAG = "fm.feed.wifilock";
}
