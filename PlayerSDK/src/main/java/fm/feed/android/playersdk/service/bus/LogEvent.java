package fm.feed.android.playersdk.service.bus;

import java.util.Collections;
import java.util.Map;

public class LogEvent {

    private String event;
    private Map<String, String> params;

    public LogEvent(String event, Map<String, String> params) {
        this.event = event;
        this.params = params;
    }

    public LogEvent(String event) {
        this.event = event;
        this.params = Collections.EMPTY_MAP;
    }

    public String getEvent() {
        return event;
    }

    public Map<String, String> getParams() {
        return params;
    }

}
