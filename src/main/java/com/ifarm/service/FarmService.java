package com.ifarm.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.Farm;
import com.ifarm.dao.FarmDao;
import com.ifarm.dao.UserDao;
import com.ifarm.util.JsonObjectUtil;

@Service
public class FarmService {
	@Autowired
	private FarmDao farmDao;

	@Autowired
	private UserDao userDao;

	public String saveFarm(Farm farm) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		farm.setFarmCreateTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
		if (farmDao.saveBase(farm)) {
			return "success";
		} else {
			return "error";
		}
	}

	public String updateFarm(Farm farm) {
		if (farmDao.updateDynamic(farm)) {
			return "success";
		} else {
			return "error";
		}
	}

	public String getFarmsList(String userId) {
		if (userId.contains("_")) {
			userId = userId.split("_")[0];
		}//子用户可以查询对应农场
		List<Farm> list = farmDao.getFarmsList(userId);
		return JsonObjectUtil.toJsonArrayString(list);
	}

	public String getUserAroundFarmList(String aroundPersonId) {
		return JsonObjectUtil.toJsonArrayString(farmDao.getFarmsList(aroundPersonId));
	}
}
