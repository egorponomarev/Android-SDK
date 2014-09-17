package fm.feed.android.playersdk;

/**
 * Created by mharkins on 8/27/14.
 */
public class JsonData {
    public static final String clientIdResponse = "{\n" +
            "    \"success\": true,\n" +
            "    \"client_id\": \"0i5k9tpwn42huxr0rrhboai\"\n" +
            "}";
    public static final String placementResponse = "{\n" +
            "    \"success\": true,\n" +
            "    \"placement\": {\n" +
            "        \"id\": \"10955\",\n" +
            "        \"name\": \"GrioSDK\"\n" +
            "    },\n" +
            "    \"stations\": [\n" +
            "        {\n" +
            "            \"id\": \"727\",\n" +
            "            \"name\": \"Pretty Lights Music\",\n" +
            "            \"has_thumbnail\": 0,\n" +
            "            \"options\": {}\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"2116\",\n" +
            "            \"name\": \"RockstressFM\",\n" +
            "            \"has_thumbnail\": 0,\n" +
            "            \"options\": {}\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    public static final String play1 = "{\n" +
            "    \"success\": true,\n" +
            "    \"play\": {\n" +
            "        \"id\": \"142049115\",\n" +
            "        \"station\": {\n" +
            "            \"id\": \"727\",\n" +
            "            \"name\": \"Pretty Lights Music\"\n" +
            "        },\n" +
            "        \"audio_file\": {\n" +
            "            \"id\": \"112101\",\n" +
            "            \"duration_in_seconds\": 153,\n" +
            "            \"codec\": \"mp3\",\n" +
            "            \"track\": {\n" +
            "                \"id\": \"15351656\",\n" +
            "                \"title\": \"The Higher Consciousness\"\n" +
            "            },\n" +
            "            \"release\": {\n" +
            "                \"id\": \"1613758\",\n" +
            "                \"title\": \"Silver Lining\"\n" +
            "            },\n" +
            "            \"artist\": {\n" +
            "                \"id\": \"1204020\",\n" +
            "                \"name\": \"MiMOSA\"\n" +
            "            },\n" +
            "            \"url\": \"http://s3.amazonaws.com/feedfm-audio/1408867412-39287.mp3\"\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final String play2 = "{\n" +
            "    \"success\": true,\n" +
            "    \"play\": {\n" +
            "        \"id\": \"142049138\",\n" +
            "        \"station\": {\n" +
            "            \"id\": \"727\",\n" +
            "            \"name\": \"Pretty Lights Music\"\n" +
            "        },\n" +
            "        \"audio_file\": {\n" +
            "            \"id\": \"8596\",\n" +
            "            \"duration_in_seconds\": 302,\n" +
            "            \"codec\": \"mp3\",\n" +
            "            \"track\": {\n" +
            "                \"id\": \"15233983\",\n" +
            "                \"title\": \"Take Your Time\"\n" +
            "            },\n" +
            "            \"release\": {\n" +
            "                \"id\": \"1555081\",\n" +
            "                \"title\": \"Levitate\"\n" +
            "            },\n" +
            "            \"artist\": {\n" +
            "                \"id\": \"1179325\",\n" +
            "                \"name\": \"Paper Diamond\"\n" +
            "            },\n" +
            "            \"url\": \"http://s3.amazonaws.com/feedfm-audio/1408897202-04569.mp3\"\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final String playStartCanSkip = "{\n" +
            "    \"success\": true,\n" +
            "    \"can_skip\": true\n" +
            "}";

    public static final String playStartNoSkip = "{\n" +
            "    \"success\": true,\n" +
            "    \"can_skip\": false\n" +
            "}";

    public static final String success = "{\"success\": true}";
    public static final String failure = "{\n" +
            "    \"success\": false,\n" +
            "    \"error\": {\n" +
            "        \"code\": 12,\n" +
            "        \"message\": \"This play has already been completed or skipped\",\n" +
            "        \"status\": 200\n" +
            "    }\n" +
            "}";

}
