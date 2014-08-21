package com.feedfm.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.widget.Toast;

import com.feedfm.android.playersdk.model.Client;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by mharkins on 8/21/14.
 */
public class Player {
    private static Player mInstance;

    public static final String EVENT_CLIENT_ID_RECEIVED = "com.feedfm.android.playersdk.event.clientid";
    public static final String EXTRA_CLIENT_ID = "com.feedfm.android.playersdk.extra.clientid";

    //TODO: Context will be removed from here and we'll use the Service as context
    private Context mContext;


    private AuthInterface mService;

    private Player(Context context) {
        mContext = context;

        String apiVersion = context.getString(R.string.api_version);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://feed.fm/api/" + apiVersion)
                .build();

        mService = restAdapter.create(AuthInterface.class);
    }

    public static Player getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Player(context);
        }
        return mInstance;
    }

    public void setCredentials(String token, String secret) throws Exception{
        String concat = token + ":" + secret;
        String base64Auth = Base64.encodeToString(concat.getBytes(), Base64.NO_WRAP | Base64.DEFAULT);
        if (!base64Auth.equals("ZDQwYjdjYzk4YTAwMWZjOWJlOGRkM2ZkMzJjM2EwYzQ5NWQwZGI0MjpiNTljNmQ5YzFiNWE5MWQxMjVmMDk4ZWY5YzJhNzE2NWRjMWJkNTE3")) {
            throw new Exception("Wrong Authorization code: " + base64Auth);
        }
        mService.getClientId("Basic " + base64Auth, new Callback<Client>() {
            @Override
            public void success(Client client, Response response) {
                if (client.isSuccess()) {
                    Toast.makeText(mContext, String.format("Client Id: %s", client.getClientId()), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EVENT_CLIENT_ID_RECEIVED);
                    intent.putExtra(EXTRA_CLIENT_ID, client.getClientId());
                    mContext.sendBroadcast(intent);
                } else {
                    Toast.makeText(mContext, "Client Id Request Unsuccessful", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(mContext, "Client Id Request failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    interface AuthInterface {
        @POST("/client")
        public void getClientId(@Header("Authorization") String authorization, Callback<Client> callback);
    }
}
