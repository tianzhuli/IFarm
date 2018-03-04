package com.ifarm.service;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.bean.FarmControlTerminal;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.FarmControlTerminalDao;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.JsonObjectUtil;
import com.ifarm.util.SystemResultEncapsulation;

@Service
@SuppressWarnings("rawtypes")
public class FarmControlTerminalService {
	@Autowired
	private FarmControlTerminalDao farmControlTerminalDao;
	
	String[] controlTerminalKeys = { "functionName", "functionCode" };
	
	private static final Log farmControlTerminalService_log = LogFactory.getLog(FarmControlTerminalService.class);
	
	public String getFarmControlOperationList(FarmControlTerminal farmControlTerminal) {
		List list = farmControlTerminalDao.getFarmControlOperationList(farmControlTerminal);
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Object[] objects = (Object[]) list.get(i);
			JSONObject jsonObject = new JSONObject();
			for (int j = 0; j < objects.length; j++) {
				jsonObject.put(controlTerminalKeys[j], objects[j]);
			}
			array.add(jsonObject);
		}
		return array.toString();
	}
	
	public String saveFarmControlSystem(FarmControlTerminal farmControlTerminal) {
		try {
			Integer terminalId= farmControlTerminalDao.saveFarmControlTerminal(farmControlTerminal);
			if (farmControlTerminal.getControlDeviceId()==null || farmControlTerminal.getSystemId()==null || farmControlTerminal.getControlDeviceBit()==null) {
				return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.NO_ID);
			}
			farmControlTerminalDao.saveFarmControlTerminal(farmControlTerminal);
			CacheDataBase.controlTeminalDetailMap.put(terminalId, JsonObjectUtil.fromBean(farmControlTerminal));
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.SUCCESS);
		} catch (Exception e) {
			// TODO: handle exception
			farmControlTerminalService_log.error(e.getMessage());
			farmControlTerminalService_log.error("添加控制系统终端", e);
			return SystemResultEncapsulation.resultCodeDecorate(SystemResultCodeEnum.ERROR);
		}
	}
}
