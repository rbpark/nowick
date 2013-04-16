package nowick.cache;

import java.util.HashSet;
import java.util.Set;


public class CacheManager {
	// Thread that expires caches at 
	private static final long UPDATE_FREQUENCY = 30000; // Every 30 sec.
	
	private long updateFrequency = UPDATE_FREQUENCY;
	private Set<Cache> caches;
	private static CacheManager manager = new CacheManager();
	private final CacheManagerThread updaterThread;
	
	private boolean activeExpiry = false;
	
	private CacheManager() {
		updaterThread = new CacheManagerThread();
		caches = new HashSet<Cache>();
		
		updaterThread.start();
	}
	
	public static void setUpdateFrequency(long updateFreqMs) {
		manager.internalUpdateFrequency(updateFreqMs);
	}
	
	public static void shutdown() {
		manager.internalShutdown();
	}
	
	public static Cache createCache() {
		Cache cache = new Cache(manager);
		manager.internalAddCache(cache);
		return cache;
	}
	
	public static void removeCache(Cache cache) {
		manager.internalRemoveCache(cache);
	}
	
	private void internalUpdateFrequency(long updateFreq) {
		updateFrequency = updateFreq;
		updaterThread.interrupt();
	}
	
	private void internalAddCache(Cache cache) {
		caches.add(cache);
		updaterThread.interrupt();
	}
	
	private void internalRemoveCache(Cache cache) {
		caches.remove(cache);
	}
	
	private synchronized void internalShutdown() {
		updaterThread.shutdown();
	}
	
	/*package*/ synchronized void update() {
		boolean activeExpiry = false;
		for (Cache cache: caches) {
			if(cache.getExpireTimeToIdle() > 0 || cache.getExpireTimeToLive() > 0) {
				activeExpiry = true;
				break;
			}
		}
		
		if (this.activeExpiry != activeExpiry && activeExpiry) {
			this.activeExpiry = activeExpiry;
			updaterThread.interrupt();
		}
	}
	
	private class CacheManagerThread extends Thread {
		private boolean shutdown = false;
		
		public void run() {
			while (!shutdown) {
				if (activeExpiry) {
					for (Cache cache: caches) {
						cache.expireCache();
					}
					
					synchronized(this) {
						try {
							wait(updateFrequency);
						} catch (InterruptedException e) {
						}
					}
				}
				else {
					synchronized(this) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		
		public void shutdown() {
			this.shutdown = true;
			updaterThread.interrupt();
		}
	}
}
