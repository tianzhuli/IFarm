package com.ifarm.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifarm.bean.FarmCollector;
import com.ifarm.bean.ProductionDevice;
import com.ifarm.redis.util.ProductionDeviceUtil;
import com.ifarm.service.FarmCollectorService;
import com.ifarm.service.ProductionDeviceService;

@RestController
@RequestMapping("device")
public class DeviceController {
	@Autowired
	private ProductionDeviceService productionDeviceService;

	@Autowired
	private ProductionDeviceUtil productionDeviceUtil;
	
	@Autowired
	private FarmCollectorService farmCollectorService;

	@RequestMapping("production")
	String productionDevcie(String deviceType, String deviceCategory, String deviceDescription, @RequestParam("batch") int batch) {
		return productionDeviceService.batchProductionDevice(deviceType, deviceCategory, deviceDescription, batch).toString();
	}

	@RequestMapping("category")
	String productionDeviceCategory() {
		return productionDeviceUtil.getProductionDeviceType().toString();
	}

	@RequestMapping("addition")
	String deviceAppendToFarm(ProductionDevice productionDevice) {
		return null;
	}
	
	@RequestMapping("concentrator")
	String concentratorAppend(FarmCollector farmCollector) {
		return farmCollectorService.saveFarmCollector(farmCollector);
	}
}
