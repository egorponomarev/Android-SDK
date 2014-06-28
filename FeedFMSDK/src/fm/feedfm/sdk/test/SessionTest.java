package fm.feedfm.sdk.test;
import android.util.Base64;
import fm.feedfm.sdk.Session;

import junit.framework.TestCase;

public class SessionTest extends TestCase {
	

	public void testGetAuthToken() {
		
		String token = "thomas";
		String secret = "fish";
		String clientid = "myid";
		String concat_input = token + ":" + secret;
		String test_against = Base64.encodeToString(concat_input.getBytes(),0,concat_input.getBytes().length, Base64.DEFAULT);
		
		
		Session session = new Session(token,secret,clientid);
		
		String auth_token = session.getAuthToken();
		
		assertEquals(test_against,auth_token);
	
			
	}
	public void testGetClientID() {
		
		String token = "thomas";
		String secret = "fish";
		String clientid = "myid";
		
		Session session = new Session(token,secret,clientid);
		
		String session_client_id = session.getClientID();
		
		assertEquals(clientid, session_client_id);
		
	}
	

}
