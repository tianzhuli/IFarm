package com.ifarm.redis.util;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRedisUtil {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public void saveOrUpdateUserToken(String userId, String token) {
		redisTemplate.opsForValue().set(RedisContstant.USER_TOKEN + userId, token);
		redisTemplate.expire(RedisContstant.USER_TOKEN + userId, RedisContstant.TOKEN_EXPIRE_TIME, TimeUnit.DAYS);
	}

	public String getUserToken(String userId) {
		return redisTemplate.opsForValue().get(RedisContstant.USER_TOKEN + userId);
	}

	public void delUserToken(String userId) {
		redisTemplate.delete(RedisContstant.USER_TOKEN + userId);
	}

	public void saveUserDetail(String userId, HashMap<String, String> map) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		hashOperations.putAll(RedisContstant.USER_DETAIL + userId, map);
	}

	public void updateUserDetail(String userId, String key, String value) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		hashOperations.put(RedisContstant.USER_DETAIL + userId, key, value);
	}

	public void delUserDetail(String userId) {
		redisTemplate.delete(RedisContstant.USER_DETAIL + userId);
	}
}
