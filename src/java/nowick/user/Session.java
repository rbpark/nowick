package nowick.user;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for the session, mapping session id to user in map
 */
public class Session {
	private final User user;
	private final String sessionId;
	private final String ip;
	private Map<String, Object> sessionData = new HashMap<String, Object>();
	
	/**
	 * Constructor for the session
	 * 
	 * @param sessionId
	 * @param user
	 */
	public Session(String sessionId, User user, String ip) {
		this.user = user;
		this.sessionId = sessionId;
		this.ip = ip;
	}

	/**
	 * Returns the User object
	 * 
	 * @return
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns the sessionId
	 * 
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	public String getIp() {
		return ip;
	}
	
	public void setSessionData(String key, Object value) {
		sessionData.put(key, value);
	}
	
	public Object getSessionData(String key) {
		return sessionData.get(key);
	}
}
