package com.ifarm.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ifarm.redis.util.ProductionDeviceUtil;
import com.ifarm.service.ProductionDeviceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ProductionDeviceTest {
	@Autowired
	private ProductionDeviceUtil productionDeviceUtil;

	@Autowired
	private ProductionDeviceService productionDeviceService;

	@Test
	public void test() {
		System.out.println(productionDeviceService.batchProductionDevice("collectorType5", "collectorDevice", "采集设备", 3));
	}
}
