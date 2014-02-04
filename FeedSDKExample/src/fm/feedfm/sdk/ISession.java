package fm.feedfm.sdk;

import fm.feedfm.sdk.innards.UIResponseHandler;

public interface ISession {
	
	public void setBaseURL(String baseUrl);
	
	public void setCredentials(String token, String secret);
	
	public void setPlacementID(String placementID);
	
	public void setStationID(String StationID);
	
	public void setFormats(String formats);
	
	public void setMaxBitRate(String bitrate);
	
	public void tune(IResponseEvent event) throws BadRequestGenerationException;
	
	public Placement getActivePlacement();
	
	public Play getActivePlay();
	
	public boolean hasActivePlayStarted();
	
	public boolean isTuned();
	
	public void reportPlayStarted(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void reportPlayElapsed(IResponseEvent handler,String seconds) throws BadRequestGenerationException;
	
	public void reportPlayCompleted(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void requestSkip(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void requestInvalidate(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void likePlay(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void unlikePlay(IResponseEvent handler) throws BadRequestGenerationException;
	
	public void dislikePlay(IResponseEvent handler) throws BadRequestGenerationException;
	
	
	
	
	

}
