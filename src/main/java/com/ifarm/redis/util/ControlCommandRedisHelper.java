package com.ifarm.redis.util;

import java.util.List;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Component;

import com.ifarm.bean.ControlCommand;
import com.ifarm.bean.WFMControlCommand;
import com.ifarm.bean.WFMControlTask;
import com.ifarm.util.CacheDataBase;

@Component
public class ControlCommandRedisHelper extends BaseLockRedisHelper<Long, ControlCommand> {

	public ControlCommandRedisHelper() {
		setRedisKeyName(RedisContstant.CONTROL_COMMAND_CACHE);
	}

	private void controlCommandIdProduce(ControlCommand command) {
		String commandId = CacheDataBase.machineCode + String.valueOf(command.hashCode());
		command.setCommandId(commandId);
	}

	@Override
	public void setLockRedisListValue(String key, ControlCommand value) {
		// TODO Auto-generated method stub
		controlCommandIdProduce(value);
		super.setLockRedisListValue(key, value);
	}

	public boolean removeControlCommand(String key, Integer controlTaskId) {
		Object lock = getLock(key);
		String redisKey = redisKeyName + key;
		ListOperations<String, ControlCommand> listOperations = valueRedisTemplate.opsForList();
		boolean removeFlat = false;
		synchronized (lock) {
			List<ControlCommand> list = listOperations.range(redisKey, 0, listOperations.size(redisKey));
			for (int i = 0; i < list.size(); i++) {
				ControlCommand command = list.get(i);
				if (controlTaskId.equals(command.getTaskId())) {
					list.remove(command);
					replaceRedisListValues(key, list);
					removeFlat = true;
				}
			}
		}
		return removeFlat;
	}

	public void removeWfmControlCommand(WFMControlTask wfmControlTask) {
		List<WFMControlCommand> list = wfmControlTask.getWfmControlCommands();
		for (int i = 0; i < list.size(); i++) {
			WFMControlCommand wCommand = list.get(i);
			Long collectorId = wCommand.getCollectorId();
			if (collectorId != null) {
				Object lock = getLock(collectorId.toString());
				String redisKey = redisKeyName + collectorId;
				ListOperations<String, ControlCommand> listOperations = valueRedisTemplate.opsForList();
				synchronized (lock) {
					List<ControlCommand> commands = listOperations.range(redisKey, 0, listOperations.size(redisKey));
					for (int j = 0; j < commands.size(); j++) {
						if (wfmControlTask.getControllerLogId().equals(commands.get(j).getTaskId())) {
							commands.remove(commands.get(j));
							replaceRedisListValues(redisKey, commands);
						}
					}
				}
			}
		}

	}
}
