package fm.feedfm.sdk;

import org.json.JSONException;
import org.json.JSONObject;

public class Play  {
	
	private JSONObject raw_json;
	private boolean success = false;
	private String play_id = null;
	private Station station = null;
	private AudioFile audio_file = null;
	
	public static final String NOT_FOUND = "Value for key not found";
	
	
	
	public boolean success() {
		return this.success;
	}
	
	public String getPlayID() {
		
		return play_id;
	}
	
	public Station getStation() {
		return this.station;
	}

	public AudioFile getAudioFile() {
		return this.audio_file;
	}
	
	
	/**
	 * The play object 
	 * 
	 * 
	 * @param raw_play
	 * @throws IllegalArgumentException
	 */
	public Play(JSONObject raw_play) throws IllegalArgumentException {
		
		
		
		
		if(raw_play != null) {
			
			this.raw_json = raw_play;
			String json_str = this.raw_json.toString();
			try {
				//this.success = this.raw_json.getBoolean("success");
				this.play_id = extractStringValue("id",this.raw_json);
				JSONObject raw_station_json = this.raw_json.getJSONObject("station");
				this.station = new Station(raw_station_json);
				JSONObject raw_audio_file = this.raw_json.getJSONObject("audio_file");
				this.audio_file = new AudioFile(raw_audio_file);
				
				
			} catch (JSONException e) {
				throw new IllegalArgumentException("JSON object representing the play was malformed");
			}
			
			
			
		}
		
		
		
		
	}
	
	/**
	 * 
	 * The station object assoicated with the play object
	 * 
	 * @author tomdebenning
	 *
	 */
	public class Station {
		
		private String id = null;
		private String name = null;
		
		/**
		 * 
		 * The Station object that is associated with the play ojbect contructor
		 * 
		 * @param station_object
		 * @throws IllegalArgumentException
		 */
		public Station(JSONObject station_object) throws IllegalArgumentException {
			
			if(station_object != null) {
				
	
				id = extractStringValue("id",station_object);
				name = extractStringValue("name",station_object);
				
							
			}
			
			
		}
		
		/**
		 * 
		 * Returns the id of the station associated with the play object
		 * 
		 * @return
		 */
		public String getStationID() {
			return this.id;
			
		}
		
		/**
		 * 
		 * Returns the name associated with the play object
		 * 
		 * @return
		 */
		public String getStationName() {
			return this.name;
		}
		
	}
	
	
	public class AudioFile {
		
		
		private JSONObject the_raw_json = null;
			
		private JSONObject raw_obj = null;
		private String audio_file_id = null;
		private String duration_in_seconds = null;
		private String codec = null;
		private String bitrate = null;
		private String url = null;
		
		
		
		private Track the_track = null;
		private Release the_release = null;
		private Artist  the_artist = null;
		
		/**
		 * 
		 * Gets the ID of the audio file associated with the play object
		 * @return
		 */
		public String getAudioFileID() {
			return this.audio_file_id;
		}
		
		
		/**
		 * 
		 * 
		 * @return
		 */
		public String getDurationInSeconds() {
			
			return this.duration_in_seconds;
		}
		
		/**
		 * 
		 * The codec associated with the play object
		 * 
		 * @return
		 */
		public String getCodec() {
			
			return this.codec;
		}
		
		/**
		 * 
		 * The bitrate assoicated wiht the AudioFile object
		 * 
		 * @return
		 */
		public String getBitrate() {
			
			return this.bitrate;
		}
		
		/**
		 * 
		 * URL for the resource of the audio file
		 * 
		 * @return
		 */
		public String getURL() {
			
			return this.url;
		}
		
		/**
		 * 
		 * The Track associated with the AudioFile Object
		 * 
		 * @return
		 */
		public Track getTrack() {
			return this.the_track;
		}
		
		/**
		 * The release associated with this play object;
		 * 
		 * @return
		 */
		public Release getRelease() {
			
			return this.the_release;
		}
		
		
		/**
		 * 
		 * The Artist that is associated with this AudioFile object
		 * 
		 * @return
		 */
		public Artist getArtist() {
			
			return this.the_artist;
		}
		/**
		 * 
		 * 
		 * The Audiofile that is associated with the Play object
		 * 
		 * 	
		 * @param the_object
		 * @throws IllegalArgumentException
		 */
		public AudioFile(JSONObject the_object) throws IllegalArgumentException  {
			
				
			if(the_object != null) {
				this.the_raw_json = the_object;
			
				String json_str = the_raw_json.toString();
				try {
					
					this.bitrate = extractStringValue("bitrate",this.the_raw_json);
					
					this.audio_file_id = extractStringValue("id",this.the_raw_json);
					this.duration_in_seconds = extractStringValue("duration_in_seconds",this.the_raw_json);
					this.codec = extractStringValue("codec",this.the_raw_json);
					this.url = extractStringValue("url",this.the_raw_json);
					JSONObject track_obj = the_raw_json.getJSONObject("track");
					this.the_track = new Track(track_obj);
					JSONObject release_obj = the_raw_json.getJSONObject("release");
					this.the_release = new Release(release_obj);
					JSONObject artist_obj = the_raw_json.getJSONObject("artist");
					this.the_artist = new Artist(artist_obj);
					
					
					
				} catch (JSONException e) {
					e.printStackTrace();
					throw new IllegalArgumentException("Audio File JSON was malformed");
				}
				
				
				
				
				
				
			} else {
				
				throw new IllegalArgumentException("The json object representing the audio file was null"); 
			}
			
			
			
		}
		/**
		 * 
		 * Represents the Track object associated with this play object
		 * 
		 * 
		 * @author tomdebenning
		 *
		 */
		public class Track  {
			
			private JSONObject raw_track= null;
			private String track_id = null;
			private String track_title = null;
			
			
			/**
			 * Constructor for the Track object
			 * 
			 * 
			 * 
			 * @param obj
			 * @throws IllegalArgumentException
			 */
			public Track(JSONObject obj) throws IllegalArgumentException {
				
				if(obj != null) {
					
					this.raw_track = obj;
				
						this.track_id = extractStringValue("id",this.raw_track);
						this.track_title =extractStringValue("title",this.raw_track);
					
					
					
					
					
					
				} else {
					
					throw new NullPointerException("The json object for the track object was null");
				}
				
				
			}
			/**
			 * 
			 * Returns the track id for the track associated with the Play Object
			 * 
			 * 
			 * @return
			 */
			public String getTrackID() {
				return this.track_id;
			}
			
			/**
			 * 
			 * Returns the title assocated with a track for a play object
			 * 
			 * @return
			 */
			public String getTrackTitle() {
				
				return this.track_title;
			}
		}
		
		
		/**
		 * 
		 * Represents a release assocated with a play object.
		 * 
		 * 
		 * @author tomdebenning
		 *
		 */
		public class Release {
			
			private JSONObject raw_json = null;
			private String release_id = null;
			private String release_title = null;
			
			/**
			 * 
			 * This is the constructor for the release object that is assocated with this play object
			 * 
			 * 
			 * @param obj -- The json object that represents the release.
			 * @throws IllegalArgumentException
			 */
			public Release(JSONObject obj) {
				
				if(obj != null) {
					
					this.raw_json = obj;
					
						
					this.release_id = extractStringValue("id",this.raw_json);
					this.release_title = extractStringValue("title",this.raw_json);
										
				} else {
					
					throw new NullPointerException("The json object representing the release was null");
					
				}
				
				
				
			}
			
			/**
			 * 
			 * This returns the ID for the release that is associated with the play object
			 * 
			 * @return
			 */
			public String getReleaseID() {
				return this.release_id;
			}
			
			/**
			 * 
			 * This returns the release title that is associated with the play object 
			 * 
			 * 
			 * @return
			 */
			public String getReleaseTitle() {
				
				return this.release_title;
			}
			
			
		}
		
		/**
		 * 
		 * Inner class that represents the Artist that is associated with this play object.
		 * 
		 * 
		 * @author tomdebenning
		 *
		 */
		public class Artist {
			
			private JSONObject raw_json = null;
			private String artist_id = null;
			private String name = null;
			
			/**
			 * 
			 * The contructor for artist object associated with the play object 
			 * 
			 * @param obj the json object that represents the artist
			 * @throws IllegalArgumentException
			 */
			public Artist(JSONObject obj) throws IllegalArgumentException {
				
				if(obj != null) {
					
					this.raw_json = obj;
				
					this.artist_id = extractStringValue("id",this.raw_json);
					this.name = extractStringValue("name",this.raw_json);
					
					
				} else {
					
					throw new NullPointerException("The json object representing the Artist was null");
				}
				
				
			}
			
			
			/**
			 * 
			 * Returns the Artist ID that is assocated with this Play object
			 * 
			 * 
			 * @return
			 */
			public String getArtistID() {
				
				return this.artist_id;
			}
			
			
			/**
			 * 
			 * Returns the name associated with the artist for this Play object
			 * 
			 * 
			 * @return
			 */
			public String getArtistName() {
				
				return this.name;
			}
				
		}
		
		
	}
	
	
	/**
	 * 
	 * Convenience method to handle the extractions of strings from JSONObject;
	 * 
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public static String extractStringValue(String key, JSONObject obj) {
		
		
		String result = NOT_FOUND;
		
		if(key != null) {
			
			if(obj != null) {
				
				try {
					result = obj.getString(key);
				} catch (JSONException e) {
					//Ignoring error and moving on.
				}
				
				
			} else {
				
				throw new NullPointerException("The JSON object to extract was null");
			}
			
			
		} else {
			
			throw new NullPointerException("The key was null");
		}
		
		return result;
		
	}
	
	

}
