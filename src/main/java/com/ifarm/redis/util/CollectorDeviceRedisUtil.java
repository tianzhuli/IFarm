package com.ifarm.redis.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ifarm.bean.DeviceValueBase;

@Component
public class CollectorDeviceRedisUtil {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> valueRedisTemplate;

	public void saveCollectorDeviceValue(Long farmId, Long collectorDeviceId, DeviceValueBase deviceValueBase) {
		HashOperations<String, Long, DeviceValueBase> hashOperations = valueRedisTemplate.opsForHash();
		hashOperations.put(RedisContstant.FARM_COLLECTOR_DEVICE_VALUE + farmId.toString(), collectorDeviceId, deviceValueBase);
	}
	
	public DeviceValueBase getCollectorDeviceValue(Long farmId,Long deviceId) {
		HashOperations<String, Long, DeviceValueBase> hashOperations = valueRedisTemplate.opsForHash();
		return hashOperations.get(RedisContstant.FARM_COLLECTOR_DEVICE_VALUE + farmId.toString(), deviceId);
	}
	
	public List<DeviceValueBase> getCollectorDeviceValue(Long farmId) {
		HashOperations<String, Long, DeviceValueBase> hashOperations = valueRedisTemplate.opsForHash();
		return hashOperations.values(RedisContstant.FARM_COLLECTOR_DEVICE_VALUE + farmId.toString());
	}
}
