package com.ifarm.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifarm.nosql.dao.InitRedisDao;
import com.ifarm.service.CollectorDeviceValueService;

public class RedisLog {
	public static final Log CONNECTREDISLOG_LOG = LogFactory.getLog(InitRedisDao.class);
	public static final Log COLLECTOR_DEVICE_VALUE_LOG = LogFactory.getLog(CollectorDeviceValueService.class);
}
