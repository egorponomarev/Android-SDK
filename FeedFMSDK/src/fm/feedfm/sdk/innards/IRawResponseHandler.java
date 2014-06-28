package fm.feedfm.sdk.innards;

import java.io.IOException;

import org.apache.http.HttpResponse;

public interface IRawResponseHandler {
	
	public boolean isSuccess(HttpResponse response);
	
	public void handleFailure(HttpResponse response);
	
	public void handleSuccess(HttpResponse response) throws IOException;

}
