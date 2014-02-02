package test;

import sagex.phoenix.cache.ICache;
import sagex.phoenix.cache.SimpleWeakMapCache;

public class TestCache {
	public static void main(String args[]) throws InterruptedException {
		ICache<String> cache = new SimpleWeakMapCache<String>(2000);
		cache.put("sean", "stuckless");
		for (int i = 0; i < 5; i++) {
			System.out.println("Sean: " + cache.get("sean"));
			Thread.currentThread().sleep(1000 * i);
		}
	}
}
