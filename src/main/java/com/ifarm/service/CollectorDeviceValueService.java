package com.ifarm.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.DeviceValueBase;
import com.ifarm.bean.FarmCollectorDevice;
import com.ifarm.dao.CollectorDeviceValueDao;
import com.ifarm.dao.DeviceHistoryValuesDao;
import com.ifarm.dao.FarmCollectorDeviceDao;
import com.ifarm.log.RedisLog;
import com.ifarm.redis.util.CollectorDeviceRedisUtil;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.CreateExcel;
import com.ifarm.util.JsonObjectUtil;

@Service
public class CollectorDeviceValueService {
	@Autowired
	private DeviceHistoryValuesDao deviceHistoryValuesDao;

	@Autowired
	private FarmCollectorDeviceDao fCollectorDeviceDao;

	@Autowired
	private CollectorDeviceValueDao collectorDeviceValueDao;
	
	@Autowired
	private CollectorDeviceRedisUtil cDeviceRedisUtil;

	private SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void saveCollectorDeviceValues(DeviceValueBase collectorDeviceValue) {
		collectorDeviceValue.setUpdateTime(Timestamp.valueOf(dFormat.format(new Date())));
		collectorDeviceValueDao.saveBase(collectorDeviceValue);
		try {
			cDeviceRedisUtil.saveCollectorDeviceValue(CacheDataBase.collectorDeviceDetail.get(collectorDeviceValue.getDeviceId()).getLong("farmId"), collectorDeviceValue.getDeviceId(), collectorDeviceValue);
		} catch (Exception e) {
			// TODO: handle exception
			RedisLog.COLLECTOR_DEVICE_VALUE_LOG.error(e.toString());
		}
	}

	public JSONArray getCollectorDeviceValues(FarmCollectorDevice fDevice) {
		JSONArray collectorDeviceValueArray = new JSONArray();
		Long deviceId = fDevice.getDeviceId();
		Integer farmId = fDevice.getFarmId();
		if (deviceId != null) {
			DeviceValueBase deviceValue = CacheDataBase.collectorDeviceMainValueMap.get(deviceId);
			JSONObject collectorDevice = CacheDataBase.collectorDeviceDetail.get(deviceId);
			if (collectorDevice == null) {
				return collectorDeviceValueArray;
			}
			JSONObject jsonObject = new JSONObject();
			JSONObject deviceValueJsonObject = JsonObjectUtil.fromBean(deviceValue);
			if (deviceValueJsonObject == null) {
				deviceValueJsonObject = new JSONObject();
				deviceValueJsonObject.put("deviceId", deviceId);
			}
			packagingDeviceDetail(deviceValueJsonObject, collectorDevice);
			List<String> list = new ArrayList<String>();
			list.add(deviceValueJsonObject.toString());
			packagingDeviceValue(jsonObject, list, collectorDevice.getString("deviceType"));
			collectorDeviceValueArray.add(jsonObject);
		} else if (deviceId == null && farmId != null) {
			if (CacheDataBase.collectorDeviceTypeDetail.containsKey(fDevice.getFarmId())) {
				ConcurrentMap<String, JSONArray> map = CacheDataBase.collectorDeviceTypeDetail.get(fDevice.getFarmId());
				for (Entry<String, JSONArray> entry : map.entrySet()) {
					JSONObject jsonObject = new JSONObject();
					String type = entry.getKey();
					JSONArray array = entry.getValue();
					List<String> list = new ArrayList<String>();
					Long device = null;
					for (int i = 0; i < array.size(); i++) {
						JSONObject json = array.getJSONObject(i);
						device = json.getLong("deviceId");
						if (device != null) {
							JSONObject deviceValueJsonObject = JsonObjectUtil.fromBean(CacheDataBase.collectorDeviceMainValueMap.get(device));
							JSONObject collectorDevice = CacheDataBase.collectorDeviceDetail.get(device);
							if (collectorDevice == null) {
								continue;
							}
							if (deviceValueJsonObject == null) {
								deviceValueJsonObject = new JSONObject();
								deviceValueJsonObject.put("deviceId", device);
							}
							packagingDeviceDetail(deviceValueJsonObject, collectorDevice);
							list.add(deviceValueJsonObject.toString());
						}
					}
					packagingDeviceValue(jsonObject, list, type);
					collectorDeviceValueArray.add(jsonObject);
				}
			}
		}
		return collectorDeviceValueArray;
	}

	public JSONObject getCollectorDeviceCacheValues(FarmCollectorDevice fDevice, String paramType) {
		JSONObject jsonObject = new JSONObject();
		Long deviceId = fDevice.getDeviceId();
		String deviceType = CacheDataBase.collectorDeviceDetail.get(deviceId).getString("deviceType");
		if (deviceId != null) {
			List<DeviceValueBase> list = CacheDataBase.collcetorDeviceMainValueCacheMap.get(deviceId);
			if (list != null) {
				int size = list.size();
				ArrayList<Double> values = new ArrayList<>();
				ArrayList<String> times = new ArrayList<>();
				for (int i = 0; i < size; i++) {
					DeviceValueBase dValue = list.get(i);
					try {
						values.add(dValue.getDynamicParamValue(paramType));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					times.add(dValue.getUpdateTime().toString());
				}
				jsonObject.put("value", values);
				jsonObject.put("time", times);
				jsonObject.put("unit", getCollectDeviceUnit(deviceType, paramType));
			}
		}
		return jsonObject;
	}
	
	public String getCollectDeviceUnit(String deviceType,String paramType) {
		if (CacheDataBase.collectorDeviceUnit.containsKey(paramType)) {
			return CacheDataBase.collectorDeviceUnit.get(paramType);
		}
		String[] params = CacheDataBase.collectorDeviceTitle.get(deviceType+"UpperCode").split(",");
		String[] units = CacheDataBase.collectorDeviceTitle.get(deviceType +"Unit").split(",");
		for (int i = 0; i < params.length; i++) {
			if (paramType.equals(params[i])) {
				CacheDataBase.collectorDeviceUnit.put(paramType, units[i]);
				return units[i];
			}
		}
		return "";
	}

	public JSONArray screenCollectorDeviceValueByDistrictOrType(Integer farmId, String deviceDistrict, String deviceType) {
		JSONArray collectorDeviceValueArray = new JSONArray();
		if (CacheDataBase.collectorDeviceTypeDetail.containsKey(farmId)) {
			ConcurrentMap<String, JSONArray> map = CacheDataBase.collectorDeviceTypeDetail.get(farmId);
			if (deviceType != null) {
				JSONObject jsonObject = new JSONObject();
				JSONArray array = map.get(deviceType);
				List<String> list = new ArrayList<String>();
				Long device = null;
				for (int i = 0; i < array.size(); i++) {
					JSONObject json = array.getJSONObject(i);
					device = json.getLong("deviceId");
					if (device != null) {
						JSONObject collectorDevice = CacheDataBase.collectorDeviceDetail.get(device);
						if (collectorDevice == null
								|| (deviceDistrict != null && !collectorDevice.getString("deviceDistrict").equals(deviceDistrict))) {
							continue;
						}
						JSONObject deviceValueJsonObject = JsonObjectUtil.fromBean(CacheDataBase.collectorDeviceMainValueMap.get(device));
						if (deviceValueJsonObject == null) {
							deviceValueJsonObject = new JSONObject();
							deviceValueJsonObject.put("deviceId", device);
						}
						packagingDeviceDetail(deviceValueJsonObject, collectorDevice);
						list.add(deviceValueJsonObject.toString());
					}
				}
				packagingDeviceValue(jsonObject, list, deviceType);
				collectorDeviceValueArray.add(jsonObject);
			} else {
				for (Entry<String, JSONArray> entry : map.entrySet()) {
					JSONObject jsonObject = new JSONObject();
					String type = entry.getKey();
					JSONArray array = entry.getValue();
					List<String> list = new ArrayList<String>();
					Long device = null;
					for (int i = 0; i < array.size(); i++) {
						JSONObject json = array.getJSONObject(i);
						device = json.getLong("deviceId");
						if (device != null) {
							JSONObject deviceValueJsonObject = JsonObjectUtil.fromBean(CacheDataBase.collectorDeviceMainValueMap.get(device));
							JSONObject collectorDevice = CacheDataBase.collectorDeviceDetail.get(device);
							if (collectorDevice == null
									|| (deviceDistrict != null && !collectorDevice.getString("deviceDistrict").equals(deviceDistrict))) {
								continue;
							}
							if (deviceValueJsonObject == null) {
								deviceValueJsonObject = new JSONObject();
								deviceValueJsonObject.put("deviceId", device);
							}
							packagingDeviceDetail(deviceValueJsonObject, collectorDevice);
							list.add(deviceValueJsonObject.toString());
						}
					}
					packagingDeviceValue(jsonObject, list, type);
					collectorDeviceValueArray.add(jsonObject);
				}
			}
		}

		return collectorDeviceValueArray;
	}

	public JSONObject packagingDeviceDetail(JSONObject deviceValueJsonObject,JSONObject collectorDevice) {
		deviceValueJsonObject.put("deviceOrderNo", collectorDevice.get("deviceOrderNo"));
		deviceValueJsonObject.put("deviceDistrict", collectorDevice.get("deviceDistrict"));
		deviceValueJsonObject.put("deviceLocation", collectorDevice.get("deviceLocation"));
		deviceValueJsonObject.put("deviceType", collectorDevice.get("deviceType"));
		return deviceValueJsonObject;
	}
	
	/**
	 * 封装实施数据
	 * 
	 * @param jsonObject
	 * @param list
	 * @param type
	 * @return
	 */
	public JSONObject packagingDeviceValue(JSONObject jsonObject, List<String> list, String type) {
		jsonObject.put("data", list);
		jsonObject.put("type", type);
		if (CacheDataBase.collectorDeviceTitle.containsKey(type)) {
			jsonObject.put("header", CacheDataBase.collectorDeviceTitle.get(type));
		} else {
			jsonObject.put("header", "");
		}
		if (CacheDataBase.collectorDeviceTitle.containsKey(type + "Code")) {
			jsonObject.put("code", CacheDataBase.collectorDeviceTitle.get(type + "Code"));
		} else {
			jsonObject.put("code", "");
		}
		if (CacheDataBase.collectorDeviceTitle.containsKey(type + "Unit")) {
			jsonObject.put("unit", CacheDataBase.collectorDeviceTitle.get(type + "Unit"));
		} else {
			jsonObject.put("unit", "");
		}
		return jsonObject;
	}

	/**
	 * 获取采集设备的历史数据，可能涉及到不同设备类型字段不同
	 * 
	 * @param fCollectorDevice
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public JSONArray getHistoryCollectorDeviceValues(FarmCollectorDevice fCollectorDevice, Timestamp beginTime, Timestamp endTime) {
		List<Object> list = deviceHistoryValuesDao.getHistorySensorValues(fCollectorDevice, beginTime, endTime);
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Object[] objects = (Object[]) list.get(i);
			JSONArray jsonArray = new JSONArray();
			for (int j = 0; j < objects.length; j++) {
				if (objects[j] != null) {
					jsonArray.add(objects[j].toString());
				} else {
					jsonArray.add("");
				}
			}
			array.add(jsonArray);
		}
		return array;
	}

	public JSONObject getHistorySensorValuesDynamic(FarmCollectorDevice fCollectorDevice, Timestamp beginTime, Timestamp endTime) {
		JSONArray jsonArray = getHistoryCollectorDeviceValues(fCollectorDevice, beginTime, endTime);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("key", "农场编号,农场名称,设备号,设备编号,设备区域,设备类型,设备位置,光照强度,空气温度,空气湿度,土壤温度,土壤湿度,更新时间".split(","));
		jsonObject.put("data", jsonArray);
		return jsonObject;
	}

	public HSSFWorkbook getHistorySensorValuesExcel(FarmCollectorDevice fCollectorDevice, Timestamp beginTime, Timestamp endTime) {
		JSONArray jsonArray = getHistoryCollectorDeviceValues(fCollectorDevice, beginTime, endTime);
		CreateExcel excel = new CreateExcel(jsonArray, "农场编号,农场名称,设备号,设备编号,设备区域,设备类型,设备位置,光照强度,空气温度,空气湿度,土壤温度,土壤湿度,更新时间");
		return excel.getExcel();
	}

	public JSONArray getSensorDistrict(FarmCollectorDevice fCollectorDevice) {
		JSONArray array = new JSONArray();
		if (fCollectorDevice.getFarmId() != null) {
			if (fCollectorDevice.getDeviceDistrict() != null) {
				array = CacheDataBase.collectorDeviceDistrictDetail.get(fCollectorDevice.getFarmId()).get(fCollectorDevice.getDeviceDistrict());
			} else {
				Set<String> set = CacheDataBase.collectorDeviceDistrictDetail.get(fCollectorDevice.getFarmId()).keySet();
				array = JSONArray.fromObject(set);
			}
		}
		return array;
	}

}
