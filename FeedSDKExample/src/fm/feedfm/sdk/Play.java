package fm.feedfm.sdk;

public class Play  {
	
	private String play_id = null;
	
	public Play(String play_id) {
		
		if(play_id != null) {
			
			this.play_id = play_id;
			
		} else {
			
			throw new NullPointerException("Play ID was null");
		}
		
		
		
	}

}
