package com.ifarm.redis.util;

import java.util.List;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Component;

import com.ifarm.bean.WFMControlTask;

@Component
public class WfmControlTaskRedisHelper extends BaseLockRedisHelper<Long, WFMControlTask>{
	
	public WfmControlTaskRedisHelper() {
		setRedisKeyName(RedisContstant.WFM_CONTROL_TASK_CACHE);
	}
	
	public boolean removeWfmControlTask(String key, Integer controlTaskId) {
		String redisKey = redisKeyName + key;
		ListOperations<String, WFMControlTask> listOperations = valueRedisTemplate.opsForList();
		boolean removeFlat = false;
		List<WFMControlTask> list = listOperations.range(redisKey, 0, listOperations.size(redisKey));
		for (int i = 0; i < list.size(); i++) {
			WFMControlTask task = list.get(i);
			if (task.getControllerLogId().equals(controlTaskId)) {
				list.remove(i);
				replaceRedisListValues(key, list);
				removeFlat = true;
			}
		}
		return removeFlat;
	}
 }
