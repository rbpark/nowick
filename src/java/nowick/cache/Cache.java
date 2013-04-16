package nowick.cache;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
	private long nextUpdateTime = 0;
	private long updateFrequency = 1*60*1000;
	private int maxCacheSize = -1;

	private long expireTimeToLive = -1; //Never expires
	private long expireTimeToIdle = -1;
	
	private EjectionPolicy ejectionPolicy = EjectionPolicy.LRU;
	private CacheManager manager = null;
	
	private Map<Object, Element<?>> elementMap = new ConcurrentHashMap<Object, Element<?>>();
	public enum EjectionPolicy {
		LRU, FIFO
	}
	
	/*package*/ Cache(CacheManager manager) {
		this.manager = manager;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		Element<?> element = elementMap.get(key);
		if (element == null) {
			return null;
		}
		return (T) element.getElement();
	}
	
	public <T> void put(Object key, T item) {
		Element<T> elem = new Element<T>(key, item);
		elementMap.put(key, elem);
	}
	
	public void remove(Object key) {
		elementMap.remove(key);
	}
	
	public Cache setMaxCacheSize(int size) {
		maxCacheSize = size;
		return this;
	}
	
	public Cache setEjectionPolicy(EjectionPolicy policy) {
		ejectionPolicy = policy;
		return this;
	}
	
	public Cache setUpdateFrequencyMs(long updateFrequencyMs) {
		this.updateFrequency = updateFrequencyMs;
		return this;
	}

	public Cache setExpiryTimeToLive(long time) {
		this.expireTimeToLive = time;
		if (time > 0) {
			manager.update();
		}
		
		return this;
	}
	
	public Cache setExpiryIdleTime(long time) {
		this.expireTimeToIdle = time;
		if (time > 0) {
			manager.update();
		}
		return this;
	}
	
	public int getSize() {
		return elementMap.size();
	}
	
	public long getExpireTimeToLive() {
		return expireTimeToLive;
	}
	
	public long getExpireTimeToIdle() {
		return expireTimeToIdle;
	}
	
	public synchronized <T> void insertElement(Object key, T item) {
		if (maxCacheSize < 0 || elementMap.size() < maxCacheSize) {
			Element<T> elem = new Element<T>(key, item);
			elementMap.put(key, elem);
		}
		else {
			internalExpireCache();
			
			Element<T> elem = new Element<T>(key, item);
			if (elementMap.size() < maxCacheSize) {
				elementMap.put(key,  elem);
			}
			else {
				Element<?> element = getNextExpiryElement();
				if (element != null) {
					elementMap.remove(element.getKey());
				}
				
				elementMap.put(key, elem);
			}
		}
	}
	
	private Element<?> getNextExpiryElement() {
		if (ejectionPolicy == EjectionPolicy.LRU) {
			long latestAccessTime = Long.MAX_VALUE;
			Element<?> ejectionCandidate = null;
			for (Element<?> elem: elementMap.values()) {
				if (latestAccessTime > elem.getLastUpdateTime()) {
					latestAccessTime = elem.getLastUpdateTime();
					ejectionCandidate = elem;
				}
			}
			
			return ejectionCandidate;
		}
		else if (ejectionPolicy == EjectionPolicy.FIFO) {
			long earliestCreateTime = Long.MAX_VALUE;
			Element<?> ejectionCandidate = null;
			for (Element<?> elem: elementMap.values()) {
				if (earliestCreateTime > elem.getCreationTime()) {
					earliestCreateTime = elem.getCreationTime();
					ejectionCandidate = elem;
				}
			}
			return ejectionCandidate;
		}
		
		return null;
	}
	
	public synchronized void expireCache() {
		long currentTime = System.currentTimeMillis();
		if (nextUpdateTime < currentTime) {
			internalExpireCache();
			nextUpdateTime = currentTime + updateFrequency;
		}
	}
	
	private synchronized void internalExpireCache() {
		ArrayList<Element<?>> elems = new ArrayList<Element<?>>(elementMap.values());
		for (Element<?> elem: elems) {
			if (shouldExpire(elem)) {
				elementMap.remove(elem.getKey());
			}
		}
	}
	
	private boolean shouldExpire(Element<?> elem) {
		if (expireTimeToLive > -1) {
			if (System.currentTimeMillis() - elem.getCreationTime() > expireTimeToLive) {
				return true;
			}
		}
		if (expireTimeToIdle > -1) {
			if (System.currentTimeMillis() - elem.getLastUpdateTime() > expireTimeToIdle) {
				return true;
			}
		}
		
		return false;
	}
}
