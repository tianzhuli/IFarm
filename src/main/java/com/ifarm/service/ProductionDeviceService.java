package com.ifarm.service;

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.ProductionDevice;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.FarmCollectorDao;
import com.ifarm.dao.FarmCollectorDeviceDao;
import com.ifarm.dao.FarmControlDeviceDao;
import com.ifarm.dao.ProductionDeviceDao;
import com.ifarm.redis.util.ProductionDeviceUtil;
import com.ifarm.util.RandomUtil;

@Service
public class ProductionDeviceService {
	@Autowired
	private ProductionDeviceDao productionDeviceDao;

	@Autowired
	private ProductionDeviceUtil productionDeviceUtil;

	@Autowired
	private FarmCollectorDao farmCollectorDao;

	@Autowired
	private FarmCollectorDeviceDao farmCollectorDeviceDao;

	@Autowired
	private FarmControlDeviceDao farmControlDeviceDao;

	private static final Log productionDevice_log = LogFactory.getLog(ProductionDeviceService.class);

	private static final String concentrator = "concentrator"; // 集中器

	private static final String collectorDevice = "collectorDevice"; // 采集设备

	private static final String controlDevice = "controlDevice"; // 控制设备

	/**
	 * 
	 * @param deviceType
	 *            采集设备分为不同的类型，控制也是，
	 * @param deviceCategory
	 *            包括控制和采集、集中器三种
	 * @param deviceCreatePerson
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public JSONArray createProductionDevice(String deviceType, String deviceCategory, String deviceDescription, int batch) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < batch; i++) {
			JSONObject jsonObject = new JSONObject();
			String deviceVerification = RandomUtil.randomString();
			ProductionDevice proDevice = new ProductionDevice(deviceVerification, deviceType, deviceCategory, deviceDescription);
			int base = new Date().getYear() % 100 * 10;
			if (concentrator.equals(deviceCategory)) {
				base += 1;
			} else if (controlDevice.equals(deviceCategory)) {
				base += 2;
			} else if (collectorDevice.equals(deviceCategory)) {
				base += 3;
			}
			Integer deviceId = RandomUtil.randomInteger(base, 5);
			while (productionDeviceDao.existProductionDevice(deviceId)) {
				deviceId = RandomUtil.randomInteger(base, 5);
			}
			proDevice.setDeviceId(deviceId);
			productionDeviceDao.saveProductionDevice(proDevice);
			jsonObject.put("deviceId", deviceId);
			jsonObject.put("deviceVerification", deviceVerification);
			jsonObject.put("createTime", proDevice.getCreateTime().toString());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}

	public JSONObject batchProductionDevice(String deviceType, String deviceCategory, String deviceDescription, int batch) {
		JSONObject jsonObject = new JSONObject();
		try {
			JSONArray array = createProductionDevice(deviceType, deviceCategory, deviceDescription, batch);
			jsonObject.put("response", SystemResultCodeEnum.SUCCESS);
			jsonObject.put("devices", array);
		} catch (Exception e) {
			// TODO: handle exception
			productionDevice_log.error(e.getMessage());
			productionDevice_log.error("设备生产异常：", e);
			jsonObject.put("response", SystemResultCodeEnum.ERROR);
		}
		return jsonObject;
	}

	/**
	 * 添加设备到数据库，二维码扫描
	 * 
	 * @param productionDevice
	 * @return
	 */
	public JSONObject deviceCheck(ProductionDevice productionDevice) {
		JSONObject jsonObject = new JSONObject();
		Integer deviceId = productionDevice.getDeviceId();
		if (deviceId == null) {
			jsonObject.put("response", SystemResultCodeEnum.NO_ID);
			return jsonObject;
		}
		ProductionDevice originalProductionDevice = productionDeviceDao.queryProductionDevice(deviceId);
		String deviceCategory = productionDevice.getDeviceCategory();
		if (deviceCategory == null) {
			deviceCategory = originalProductionDevice.getDeviceCategory();
		}
		if (!originalProductionDevice.getDeviceCategory().equals(deviceCategory)) {
			jsonObject.put("response", SystemResultCodeEnum.CATEGORY_ERROR);
			return jsonObject;
		}
		jsonObject.put("response", SystemResultCodeEnum.SUCCESS);
		return jsonObject;
	}

}
