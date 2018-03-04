package com.ifarm.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.FarmControlDevice;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.FarmControlDeviceDao;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.JsonObjectUtil;
import com.ifarm.util.SystemResultEncapsulation;

@Service
public class FarmControlDeviceService {
	@Autowired
	private FarmControlDeviceDao farmControlDeviceDao;

	private static final Log farmControlDevice_log = LogFactory.getLog(FarmControlDeviceService.class);

	public String saveFarmControlDevice(FarmControlDevice farmControlDevice) {
		Integer deviceId = farmControlDevice.getControlDeviceId();
		if (deviceId == null) {
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.NO_ID);
		}
		FarmControlDevice baseFarmCollectorDevice = farmControlDeviceDao.getTById(deviceId, FarmControlDevice.class);
		if (baseFarmCollectorDevice != null) {
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.ID_EXIST);
		}
		try {
			farmControlDeviceDao.saveFarmCollectorDevice(farmControlDevice);
			CacheDataBase.controlDeviceDetailMap.put(deviceId, JsonObjectUtil.fromBean(farmControlDevice));
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.SUCCESS);
		} catch (Exception e) {
			// TODO: handle exception
			farmControlDevice_log.error(e.getMessage());
			farmControlDevice_log.error("控制设备添加异常", e);
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.ERROR);
		}
	}
}
