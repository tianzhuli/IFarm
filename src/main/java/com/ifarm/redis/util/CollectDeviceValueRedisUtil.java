package com.ifarm.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CollectDeviceValueRedisUtil {
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> valueRedisTemplate;
}
