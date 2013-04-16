package nowick.user;

import nowick.cache.Cache;
import nowick.cache.Cache.EjectionPolicy;
import nowick.cache.CacheManager;
import nowick.utils.Properties;

/**
 * Cache for web session.
 */
public class SessionCache {
	private Cache cache;

	/**
	 * Constructor taking global props.
	 * 
	 * @param props
	 */
	public SessionCache(Properties props) {
		cache = CacheManager.createCache();
		cache.setUpdateFrequencyMs(300000)
			 .setEjectionPolicy(EjectionPolicy.LRU)
			 .setMaxCacheSize(1000);
	}

	/**
	 * Returns the cached session using the session id.
	 * 
	 * @param sessionId
	 * @return
	 */
	public Session getSession(String sessionId) {
		Session session = cache.get(sessionId);

		if (session == null) {
			return null;
		}

		return session;
	}

	/**
	 * Adds a session to the cache. Accessible through the session ID.
	 * 
	 * @param id
	 * @param session
	 */
	public void addSession(Session session) {
		cache.put(session.getSessionId(), session);
	}

	/**
	 * Removes the session from the cache.
	 * 
	 * @param id
	 * @return
	 */
	public void removeSession(String id) {
		cache.remove(id);
	}
}