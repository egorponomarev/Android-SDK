package com.feedfm.sdk;

public interface ISession {
	
	public void setBaseURL(String baseUrl);
	
	public void setCreditials(String token, String secret);
	
	public void setPlacementID(String placementID);
	
	public void setStationID(String StationID);
	
	public void setFormats(String formats);
	
	public void setMaxBitRate(String bitrate);
	
	public void tune();
	
	public Placement getActivePlacement();
	
	public Play getActivePlay();
	
	public boolean hasActivePlayStarted();
	
	public boolean isTuned();
	
	public void reportPlayStarted();
	
	public void reportPlayElapsed(String seconds);
	
	public void reportPlayCompleted();
	
	public void requestSkip();
	
	public void requestInvalidate();
	
	public void likePlay();
	
	public void unlikePlay();
	
	public void dislikePlay();
	
	public void Unsuspend();
	
	
	

}
