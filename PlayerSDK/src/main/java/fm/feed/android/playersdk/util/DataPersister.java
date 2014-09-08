package fm.feed.android.playersdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
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
     * Retrieve a Data value based on the {@link fm.feed.android.playersdk.util.DataPersister.Key}
     *
     * @param keyName an identifier for the Data blob {@link fm.feed.android.playersdk.util.DataPersister.Key}
     * @param defaultValue
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
     * @param keyName an identifier for the Data blob {@link fm.feed.android.playersdk.util.DataPersister.Key}
     * @param value
     */
    public void putString(Key keyName, String value) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(keyName.toString(), value);
        editor.commit();

        Log.i(TAG, "Client ID: >>>>>>>>\"" + value + "\"<<<<<<<");
    }
}
