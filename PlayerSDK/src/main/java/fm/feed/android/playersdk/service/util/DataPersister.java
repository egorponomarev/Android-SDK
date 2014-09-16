package fm.feed.android.playersdk.service.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 9/5/14.
 */
public class DataPersister {
    private static final String TAG = DataPersister.class.getSimpleName();

    private static final String packageName = DataPersister.class.getPackage().toString();

    public static enum Key {
        clientId;

        Key() {
        }

        @Override
        public String toString() {
            return packageName + "." + name();
        }
    }

    private Context mContext;

    public DataPersister(Context context) {
        this.mContext = context;
    }

    /**
     * Retrieve a Data value based on the {@link DataPersister.Key}
     *
     * @param keyName
     *         an identifier for the Data blob {@link DataPersister.Key}
     * @param defaultValue
     *
     * @return
     */
    public String getString(Key keyName, String defaultValue) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        String value = settings.getString(keyName.toString(), defaultValue);

        Log.i(TAG, "Client ID: >>>>>>>>\"" + value + "\"<<<<<<<");

        return value;
    }

    /**
     * Saves a Data value to the shared preferences.
     *
     * @param keyName
     *         an identifier for the Data blob {@link DataPersister.Key}
     * @param value
     */
    public void putString(Key keyName, String value) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(keyName.toString(), value);
        editor.apply();

        Log.i(TAG, "Client ID: >>>>>>>>\"" + value + "\"<<<<<<<");
    }
}
