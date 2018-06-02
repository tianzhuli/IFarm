package com.ifarm.redis.util;

import java.util.List;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Component;

import com.ifarm.bean.ControlTask;

@Component
public class ControlTaskRedisHelper extends BaseLockRedisHelper<Long, ControlTask> {

	public ControlTaskRedisHelper() {
		setRedisKeyName(RedisContstant.CONTROL_TASK_CACHE);
	}

	/**
	 * 移除task，但是因为run里面的lock直接锁住了这个key
	 * 
	 * @param key
	 * @param controlTaskId
	 * @return
	 */
	public boolean removeControlTask(String key, Integer controlTaskId) {
		String redisKey = redisKeyName + key;
		ListOperations<String, ControlTask> listOperations = valueRedisTemplate.opsForList();
		boolean removeFlat = false;
		List<ControlTask> list = listOperations.range(redisKey, 0, listOperations.size(redisKey));
		for (int i = 0; i < list.size(); i++) {
			ControlTask task = list.get(i);
			if (task.getControllerLogId().equals(controlTaskId)) {
				list.remove(i);
				replaceRedisListValues(key, list);
				removeFlat = true;
			}
		}
		return removeFlat;
	}
}
