package com.ifarm.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifarm.bean.FarmCollector;
import com.ifarm.bean.FarmCollectorDevice;
import com.ifarm.bean.FarmControlDevice;
import com.ifarm.bean.ProductionDevice;
import com.ifarm.redis.util.ProductionDeviceUtil;
import com.ifarm.service.FarmCollectorDeviceService;
import com.ifarm.service.FarmCollectorService;
import com.ifarm.service.FarmControlDeviceService;
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

	@Autowired
	private FarmCollectorDeviceService farmCollectorDeviceService;

	@Autowired
	private FarmControlDeviceService farmControlDeviceService;

	@RequestMapping("production")
	String productionDevcie(String deviceType, String deviceCategory, String deviceDescription, @RequestParam("batch") int batch) {
		return productionDeviceService.batchProductionDevice(deviceType, deviceCategory, deviceDescription, batch).toString();
	}

	@RequestMapping("category")
	String productionDeviceCategory(String deviceCategory) {
		return productionDeviceUtil.getProductionDeviceType(deviceCategory).toString();
	}

	@RequestMapping("check")
	String deviceAppendToFarm(ProductionDevice productionDevice) {
		return productionDeviceService.deviceCheck(productionDevice).toString();
	}

	@RequestMapping("concentrator/addition")
	String concentratorAppend(FarmCollector farmCollector) {
		return farmCollectorService.saveFarmCollector(farmCollector);
	}

	@RequestMapping("collectorDevice/addition")
	String collectorDeviceAddition(FarmCollectorDevice farmCollectorDevice) {
		return farmCollectorDeviceService.saveCollectorDevice(farmCollectorDevice);
	}

	@RequestMapping("controlDevice/addition")
	String controlDeviceAddition(FarmControlDevice farmControlDevice) {
		return farmControlDeviceService.saveFarmControlDevice(farmControlDevice);
	}
}
