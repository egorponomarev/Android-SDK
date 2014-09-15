package fm.feed.android.playersdk.service.webservice.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
* Created by mharkins on 9/15/14.
*/
public class FeedFMErrorDeserializer implements JsonDeserializer<FeedFMError> {
    @Override
    public FeedFMError deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement jsonCode = jsonObject.get("code");
        JsonElement jsonMessage = jsonObject.get("message");
        JsonElement jsonStatus = jsonObject.get("status");

        return new FeedFMError(jsonCode.getAsInt(), jsonMessage.getAsString(), jsonStatus.getAsInt());
    }
}
