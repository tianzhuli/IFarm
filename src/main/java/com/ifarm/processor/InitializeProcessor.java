package com.ifarm.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.ifarm.nosql.dao.InitRedisDao;
import com.ifarm.nosql.service.ControlSystemStateService;
import com.ifarm.service.CollectorDeviceValueService;
import com.ifarm.service.CollectorValueService;
import com.ifarm.service.ControlTaskService;
import com.ifarm.service.WFMControlTaskService;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.ControlCacheCollection;

public class InitializeProcessor implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private CollectorValueService collectorValuesService;

	@Autowired
	private CollectorDeviceValueService sensorValuesService;

	@Autowired
	private InitRedisDao initRedisDao;

	@Autowired
	private ControlTaskService taskService;

	@Autowired
	private WFMControlTaskService wService;

	@Autowired
	private ControlCacheCollection controlCacheCollection;

	@Autowired
	private ControlSystemStateService controlSystemStateService;

	@Autowired
	private CollectorDeviceValueService cValueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getApplicationContext().getParent() == null) {
			try {
				init();// 静态service初始化
				CacheDataBase.initialize();// 静态的数据库初始化
				// ReceiveDataServer.init();// 收数据线程开启
				// ControlServer.init();// 控制线程开启
				System.out.println("设备分区信息：" + CacheDataBase.collectorDeviceDistrictDetail);
				System.out.println("设备类型信息：" + CacheDataBase.collectorDeviceTypeDetail);
				System.out.println("设备参数上阈值：" + CacheDataBase.collcetorDeviceUpperThresholdMap);
				System.out.println("设备参数下阈值：" + CacheDataBase.collctorDeviceDownThresholdMap);
				System.out.println("设备信息" + CacheDataBase.collectorDeviceDetail);
				/*
				 * if (!initRedisDao.redisConnect()) {
				 * System.out.println("redis连接异常"); }
				 */
				/*
				 * Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
				 */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void init() {
		CacheDataBase.cacheCollection = controlCacheCollection;
		CacheDataBase.collectorValuesService = collectorValuesService;
		CacheDataBase.sensorValuesService = sensorValuesService;
		CacheDataBase.taskService = taskService;
		CacheDataBase.controlSystemStateService = controlSystemStateService;
		CacheDataBase.initRedisDao = initRedisDao;
		CacheDataBase.dValueService = cValueService;
		CacheDataBase.wTaskService = wService;
	}
}
