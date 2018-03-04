package com.ifarm.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ifarm.bean.FarmCollectorDevice;
import com.ifarm.service.FarmCollectorDeviceService;
import com.ifarm.service.UserLogService;

@Controller
@RequestMapping(value = "farmCollectorDevice")
public class FarmCollectorDeviceController {
	@Autowired
	private FarmCollectorDeviceService fDeviceService;

	@Autowired
	private UserLogService userLogService;

	@RequestMapping(value = "addCollectorDevice")
	public @ResponseBody
	String addFarmSensor(@RequestParam("userId") String userId, HttpServletRequest request, FarmCollectorDevice farmSensor) {
		String message = fDeviceService.saveCollectorDevice(farmSensor);
		userLogService.saveUserLog(request, farmSensor, userId, "add", "farmSensor", message);
		return message;
	}

	@RequestMapping(value = "updateCollectorDevice")
	public @ResponseBody
	String updateFarmSensor(@RequestParam("userId") String userId, HttpServletRequest request, FarmCollectorDevice farmSensor) {
		String message = fDeviceService.updateCollectorDevice(farmSensor);
		userLogService.saveUserLog(request, farmSensor, userId, "update", "farmSensor", message);
		return message;
	}

	@RequestMapping(value = "getCollectorDevicesList")
	public @ResponseBody
	String getFarmSensorsList(FarmCollectorDevice fCollectorDevice) {
		return fDeviceService.getFarmCollectorDevicesList(fCollectorDevice);
	}
	
	@RequestMapping(value = "collectorDeviceParamCode")
	public @ResponseBody 
	String getCollectorDeviceParam(String deviceType){
		return fDeviceService.getCollectorDeviceParamCode(deviceType).toString();
	}
	
	@RequestMapping(value = "collectorDeviceTypeList")
	public @ResponseBody 
	String getCollectorDeviceTypeList(){
		return fDeviceService.getCollectorDeviceTypeList();
	}
}
