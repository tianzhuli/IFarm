package com.ifarm.redis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductionDeviceUtil {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public JSONArray getProductionDeviceType(String deviceCategory) {
		if (deviceCategory == null) {
			deviceCategory = "";
		}
		JSONArray jsonArray = new JSONArray();
		HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
		Set<String> deviceTypeSet = stringRedisTemplate.keys(RedisContstant.PRODUCT_DEVICE_TYPE + deviceCategory + "*");
		Map<String, String> categoryMap = hashOperations.entries(RedisContstant.PRODUCT_DEVICE_CATEGORY);
		for (String key : deviceTypeSet) {
			Map<String, String> deviceTypeMap = hashOperations.entries(key);
			JSONArray deviceTypeArray = new JSONArray();
			for (String deviceTypeKey : deviceTypeMap.keySet()) {
				JSONObject deviceTypeJsonObject = new JSONObject();
				deviceTypeJsonObject.put("deviceType", deviceTypeKey);
				deviceTypeJsonObject.put("deviceTypeName", deviceTypeMap.get(deviceTypeKey));
				deviceTypeArray.add(deviceTypeJsonObject);
			}
			JSONObject jsonObject = new JSONObject();
			String[] arrayStrings = key.split("_");
			String categoryKey = arrayStrings[arrayStrings.length - 1];
			if (categoryMap.get(categoryKey) != null) {
				jsonObject.put("deviceCategory", categoryKey);
				jsonObject.put("deviceCategoryName", categoryMap.get(categoryKey));
				jsonObject.put("deviceType", deviceTypeArray);
				jsonArray.add(jsonObject);
			}
		}
		return jsonArray;
	}

	public void saveProductionDeviceType(HashMap<String, String> hashMap) {
		HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
		hashOperations.putAll(RedisContstant.PRODUCT_DEVICE_TYPE, hashMap);
	}

	public void saveProductionDeviceCategory(HashMap<String, String> hashMap) {
		HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
		hashOperations.putAll(RedisContstant.PRODUCT_DEVICE_CATEGORY, hashMap);
	}

}
