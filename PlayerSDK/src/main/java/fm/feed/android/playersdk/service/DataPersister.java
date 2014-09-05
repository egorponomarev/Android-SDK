package fm.feed.android.playersdk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by mharkins on 9/5/14.
 */
public class DataPersister {
    private static final String TAG = DataPersister.class.getSimpleName();

    private static final String packageName = DataPersister.class.getPackage().toString();

    public static enum Blob {
        clientId;

        Blob() {
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

    public String getString(Blob blobName, String defaultValue) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        String value = settings.getString(blobName.toString(), defaultValue);

        Log.i(TAG, "Client ID: >>>>>>>>\"" + value + "\"<<<<<<<");

        return value;
    }

    public void putString(Blob blobName, String value) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(blobName.toString(), value);
        editor.commit();

        Log.i(TAG, "Client ID: >>>>>>>>\"" + value + "\"<<<<<<<");
    }
}
