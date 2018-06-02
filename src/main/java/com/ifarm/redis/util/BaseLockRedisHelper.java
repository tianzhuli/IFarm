package com.ifarm.redis.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.ListOperations;

public class BaseLockRedisHelper<K, V> extends BaseRedisHelper<K, V> {
	private static Map<String, Object> lockMap = new HashMap<String, Object>();

	public BaseLockRedisHelper() {

	}

	/**
	 * 后期考虑分布式锁
	 * 
	 * @param key
	 * @return
	 */
	public Object getLock(String key) {
		Object lock = lockMap.get(key);
		if (lock == null) {
			lock = new Object();
			lockMap.put(key, lock);
		}
		return lock;
	}

	public void setLockRedisListValue(String key, V value) {
		Object lock = getLock(key);
		synchronized (lock) {
			valueRedisTemplate.opsForList().rightPush(redisKeyName + key, value);
		}
	}

	public V getLockRedisListValue(String key) {
		Object lock = getLock(key);
		ListOperations<String, V> listOperations = valueRedisTemplate.opsForList();
		synchronized (lock) {
			return listOperations.leftPop(redisKeyName + key);
		}
	}
}
