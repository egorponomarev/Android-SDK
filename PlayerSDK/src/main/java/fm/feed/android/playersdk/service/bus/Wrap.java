package fm.feed.android.playersdk.service.bus;

/**
 * Created by mharkins on 8/22/14.
 */
public class Wrap <T>{
    private T mObject;

    public Wrap(T mObject) {
        this.mObject = mObject;
    }

    public T getObject() {
        return mObject;
    }
}
