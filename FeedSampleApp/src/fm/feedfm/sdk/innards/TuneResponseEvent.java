package fm.feedfm.sdk.innards;

import org.json.JSONObject;

import fm.feedfm.sdk.IResponseEvent;
import fm.feedfm.sdk.Session;

public class TuneResponseEvent implements IResponseEvent {

	
	private IResponseEvent the_user_event = null;
	private Session session = null;
	
	public TuneResponseEvent(Session session, IResponseEvent userevent) {
		
		
		this.session = session;
		this.the_user_event = userevent;
	
		
	}
	
	
	
	@Override
	public void successEvent(JSONObject response_object) {
	
		if(this.session != null) {
			
			this.session.setTuned(true);
			
		}
		
		if(this.the_user_event != null) {
			
			this.the_user_event.successEvent(response_object);
		}
		

	}

	@Override
	public void failureEvent(int statuscode, String reason, String data) {
		
		if(this.session != null) {
			this.session.setTuned(false);
		}

		if(this.the_user_event != null) {
				this.the_user_event.failureEvent(statuscode, reason, data);
		}
		
		
	}

}
