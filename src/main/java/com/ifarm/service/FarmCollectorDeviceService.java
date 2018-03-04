package com.ifarm.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.FarmCollectorDevice;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.FarmCollectorDeviceDao;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.JsonObjectUtil;
import com.ifarm.util.SystemResultEncapsulation;

@Service
public class FarmCollectorDeviceService {
	@Autowired
	private FarmCollectorDeviceDao farmCollectorDeviceDao;

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Log farmCollectorDevice_log = LogFactory.getLog(FarmCollectorDeviceService.class);

	public String saveCollectorDevice(FarmCollectorDevice fDevice) {
		Long deviceId = fDevice.getDeviceId();
		Integer farmId = fDevice.getFarmId();
		Long collectorId = fDevice.getCollectorId();
		if (deviceId == null || farmId == null || collectorId == null) {
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.NO_ID);
		}
		FarmCollectorDevice baseFarmCollectorDevice = farmCollectorDeviceDao.getTById(deviceId, FarmCollectorDevice.class);
		if (baseFarmCollectorDevice != null) {
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.ID_EXIST);
		}
		fDevice.setDeviceCreateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		fDevice.setDeviceOrderNo(builderDeviceOrder(farmId));
		try {
			farmCollectorDeviceDao.saveFarmCollectorDevice(fDevice);
			if (CacheDataBase.collectorDeviceAddCache.containsKey(fDevice.getCollectorId())) {
				CacheDataBase.collectorDeviceAddCache.get(fDevice.getCollectorId()).add(fDevice.getDeviceId());
			} else {
				List<Long> list = new ArrayList<Long>();
				list.add(fDevice.getDeviceId());
				CacheDataBase.collectorDeviceAddCache.put(fDevice.getCollectorId(), list);
			}
			// 将添加的设备加入到缓存,等待集中器通信时添加到集中器
			CacheDataBase.collectorDeviceDetail.put(deviceId, JsonObjectUtil.fromBean(fDevice));
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.SUCCESS);
		} catch (Exception e) {
			// TODO: handle exception
			farmCollectorDevice_log.error(e.getMessage());
			farmCollectorDevice_log.error("添加采集设备", e);
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.ERROR);
		}
	}

	public String updateCollectorDevice(FarmCollectorDevice fCollectorDevice) {
		if (farmCollectorDeviceDao.updateDynamic(fCollectorDevice)) {
			return SystemResultCodeEnum.SUCCESS;
		} else {
			return SystemResultCodeEnum.ERROR;
		}
	}

	public String getFarmCollectorDevicesList(FarmCollectorDevice fCollectorDevice) {
		return JsonObjectUtil.toJsonArrayString(farmCollectorDeviceDao.getDynamicList(fCollectorDevice));
	}

	public JSONObject getCollectorDeviceParamCode(String deviceType) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("header", CacheDataBase.collectorDeviceTitle.get(deviceType));
		jsonObject.put("code", CacheDataBase.collectorDeviceTitle.get(deviceType + "UpperCode"));
		return jsonObject;
	}

	public String getCollectorDeviceTypeList() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("header", CacheDataBase.collectorDeviceTitle.get("collectorDeviceTypeTitle"));
		jsonObject.put("code", CacheDataBase.collectorDeviceTitle.get("collectorDeviceTypeCode"));
		return jsonObject.toString();
	}

	public int builderDeviceOrder(Integer farmId) {
		int deviceOrder = 1;
		List<?> list = farmCollectorDeviceDao.builderDeviceOrder(farmId);
		if (list.size() == 0) {
			return deviceOrder;
		}
		deviceOrder = (Integer) list.get(0);
		return deviceOrder;
	}
}
