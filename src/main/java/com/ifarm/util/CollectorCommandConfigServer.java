package com.ifarm.util;

import java.util.ArrayList;
import java.util.List;

import com.ifarm.bean.CollectorConfigCommand;

public class CollectorCommandConfigServer {
	public String configCollectorCommand(CollectorConfigCommand collectorConfig) {
		StringBuffer result = new StringBuffer(collectorConfig.getTime());
		if (collectorConfig.getCollectorId() != null && CacheDataBase.collectorDeviceAddCache.containsKey(collectorConfig.getCollectorId())) {
			List<Long> list = new ArrayList<Long>();
			StringBuffer sensors = new StringBuffer();
			list = CacheDataBase.collectorDeviceAddCache.get(collectorConfig.getCollectorId());
			for (int i = 0; i < list.size(); i++) {
				String configSensor = CacheDataBase.configSensor.replace("?", list.get(i).toString());
				sensors.append(configSensor);
			}
			collectorConfig.setSensor(sensors.toString());
			collectorConfig.setAddSensor(true);
			result.append(collectorConfig.getSensor());
		}
		if (collectorConfig.getCollectorId() != null && CacheDataBase.collecotConfigMessage.containsKey(collectorConfig.getCollectorId())) {
			collectorConfig.setIpConfig(CacheDataBase.collecotConfigMessage.get(collectorConfig.getCollectorId()));
			collectorConfig.setConfig(true);
			result.append(collectorConfig.getIpConfig());
		}
		result.append(collectorConfig.getStop());
		return result.toString();
	}
}
