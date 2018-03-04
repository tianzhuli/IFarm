package com.ifarm.service;

import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.Manager;
import com.ifarm.dao.ManagerDao;
import com.ifarm.nosql.bean.ManagerToken;
import com.ifarm.nosql.dao.ManagerTokenDao;
import com.ifarm.util.JsonObjectUtil;

@Service
public class ManagerService {
	@Autowired
	private ManagerDao managerDao;

	@Autowired
	private ManagerTokenDao managerTokenDao;

	public String getAllManager() {
		return JsonObjectUtil.toJsonArrayString(managerDao.getAllManager());
	}

	public String updateManager(Manager manager) {
		Manager newManager = changeManager(manager);
		if (managerDao.updateManager(newManager)) {
			return "success";
		} else {
			return "error";
		}
	}

	public String managerGetSignature(String managerId) {
		if (managerId == null) {
			return "error";
		}
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("ifarm");
		stringBuffer.append(UUID.randomUUID());
		stringBuffer.append(Base64.encodeBase64String(managerId.getBytes()));
		String token = stringBuffer.toString().replace("-", "");
		System.out.println(managerId + ":" + token);
		return token;
	}

	public Manager getManagerById(String managerId) {
		return managerDao.getManagerById(managerId);
	}

	public Manager changeManager(Manager newManager) {
		Manager oldManager = managerDao.getManagerById(newManager.getManagerId());
		Field[] fields = oldManager.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
				if (fields[i].get(newManager) != null) {
					fields[i].set(oldManager, fields[i].get(newManager));
				}
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return oldManager;
	}

	public String getManager(Manager manager) {
		return JsonObjectUtil.toJsonArrayString(managerDao.getDynamicList(manager));
	}

	public String managerLogin(Manager manager) {
		if (managerDao.getManagerById(manager.getManagerId()) != null) {
			ManagerToken mToken = new ManagerToken();
			mToken.setManagerId(manager.getManagerId());
			String token = managerGetSignature(manager.getManagerId());
			mToken.setToken(token);
			managerTokenDao.saveManagerToken(mToken);
			return "success:" + token;
		} else {
			return "error";
		}
	}
}
