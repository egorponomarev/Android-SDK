package fm.feed.android.playersdk.service.bus;

/**
 * Created by mharkins on 8/25/14.
 */
public class PlayerAction {
    public static enum ActionType {
        TUNE,
        PLAY,
        SKIP,
        PAUSE,
        LIKE,
        UNLIKE,
        DISLIKE
    }

    private ActionType mAction;

    public PlayerAction(ActionType action) {
        this.mAction = action;
    }

    public ActionType getAction() {
        return mAction;
    }
}
