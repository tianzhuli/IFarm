package com.ifarm.redis.util;

import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class FarmControlSystemTypeUtil {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public JSONObject farmControlSystemType() {
		HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
		Map<String, String> hashMap = hashOperations.entries(RedisContstant.FARM_CONTROL_SYSTEM_TYPE);
		return JSONObject.fromObject(hashMap);
	}

	public JSONObject farmControlSystemTerminal(String controlType) {
		HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
		Map<String, String> hashMap = hashOperations.entries(RedisContstant.FARM_CONTROL_SYSTEM_TYPE + controlType);
		return JSONObject.fromObject(hashMap);
	}
}
