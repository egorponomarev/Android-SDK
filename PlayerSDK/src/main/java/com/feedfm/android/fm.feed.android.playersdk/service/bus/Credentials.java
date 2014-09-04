package fm.feed.android.playersdk.service.bus;

import fm.feed.android.playersdk.util.StringUtils;

/**
 * Created by mharkins on 8/22/14.
 */
public class Credentials {
    private String token;
    private String secret;

    public Credentials(String token, String secret) {
        this.token = token;
        this.secret = secret;
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isValid() {
        return !StringUtils.isEmpty(token) && !StringUtils.isEmpty(secret);
    }

    @Override
    public String toString() {
        return String.format("token: %s, secret: %s", getToken(), getSecret());
    }
}
