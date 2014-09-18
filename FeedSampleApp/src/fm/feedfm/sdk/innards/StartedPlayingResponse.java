package fm.feedfm.sdk.innards;

import org.json.JSONObject;

import fm.feedfm.sdk.IResponseEvent;
import fm.feedfm.sdk.Session;

public class StartedPlayingResponse implements IResponseEvent {
	
	private IResponseEvent the_user_event = null;
	private Session session = null;


	@Override
	public void successEvent(JSONObject response_object) {
	
		if(this.session != null) {
			
			this.session.setStarted_playing(true);
			
		}
		
		if(this.the_user_event != null) {
			
			this.the_user_event.successEvent(response_object);
		}
		

	}

	@Override
	public void failureEvent(int statuscode, String reason, String data) {
		
		if(this.session != null) {
			this.session.setStarted_playing(false);
		}

		if(this.the_user_event != null) {
				this.the_user_event.failureEvent(statuscode, reason, data);
		}
		
		
	}

}
