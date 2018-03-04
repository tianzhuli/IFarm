package com.ifarm.web;

import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifarm.bean.ControlTask;
import com.ifarm.bean.FarmControlSystem;
import com.ifarm.bean.FarmControlTerminal;
import com.ifarm.bean.WFMControlTask;
import com.ifarm.constant.ControlTaskEnum;
import com.ifarm.nosql.service.CombinationControlTaskService;
import com.ifarm.service.FarmControlSystemService;
import com.ifarm.service.FarmControlTerminalService;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.ControlHandlerUtil;
import com.ifarm.util.ControlTaskUtil;

@RequestMapping("farmControl")
@RestController
public class FarmControlController {
	@Autowired
	private FarmControlSystemService farmControlService;

	@Autowired
	private FarmControlTerminalService farmControlTerminalService;

	@Autowired
	private CombinationControlTaskService combinationControlTaskService;
	
	@RequestMapping("controlSystemList")
	public String farmControlSystemsDynamicList(FarmControlSystem farmControlSystem, String userId) {
		return farmControlService.farmControlSystemsDynamicList(farmControlSystem, userId);
	}

	@RequestMapping("wfmControlSystemList")
	public String wfmControlSystemList(String userId) {
		return farmControlService.farmWFMControlSystemsDynamicList(userId);
	}

	@RequestMapping("controlRegion")
	public String controlRegion(Integer farmId) {
		return farmControlService.farmControlRegion(farmId).toString();
	}

	@RequestMapping("updateControlSystem")
	public String updateControlSystem(FarmControlSystem farmControlSystem) {
		return farmControlService.updateControlSystemState(farmControlSystem);
	}

	@RequestMapping("authorityLimit")
	public String authorityLimit(@RequestParam("userId") String userId, @RequestParam("checkNum") Integer checkNum) {
		return farmControlService.authorityLimit(userId, checkNum);
	}

	@RequestMapping("combinationControlHistory")
	public String combinationControlHistory(String userId){
		return combinationControlTaskService.queryCombinationControlTask(userId);
	}
	
	@RequestMapping("farmControlTaskStrategy")
	public String farmControlTaskStrategy(ControlTask controlTask, String command) throws InterruptedException {
		String userId = controlTask.getUserId();
		JSONObject resultJson = new JSONObject();
		if ("queryTasks".equals(command)) {
			JSONArray array = new JSONArray();
			String controlType = controlTask.getControlType();
			if (CacheDataBase.controlTaskStateCache.containsKey(userId)) {
				if (CacheDataBase.controlTaskStateCache.get(userId).size() > 0) {
					if (controlType == null) {
						ControlTaskUtil.queryTasks(CacheDataBase.controlTaskStateCache.get(userId), array);
					} else {
						ControlTaskUtil.queryTasks(CacheDataBase.controlTaskStateCache.get(userId), controlType, array);
					}
				}
			}
			if (CacheDataBase.wfmControlTaskStateCache.containsKey(userId)) {
				if (CacheDataBase.wfmControlTaskStateCache.get(userId).size() > 0) {
					if (controlType == null) {
						ControlTaskUtil.queryWfmTasks(CacheDataBase.wfmControlTaskStateCache.get(userId), array);
					} else {
						ControlTaskUtil.queryWfmTasks(CacheDataBase.wfmControlTaskStateCache.get(userId), controlType, array);
					}
				}
			}
			resultJson.put("response", array);
			return resultJson.toString();
		} else if ("queryExecutingTasks".equals(command)) {
			JSONArray array = new JSONArray();
			if (CacheDataBase.controlTaskStateCache.containsKey(userId)) {
				if (CacheDataBase.controlTaskStateCache.get(userId).size() > 0) {
					ControlTaskUtil.queryExecutingTasks(CacheDataBase.controlTaskStateCache.get(userId), array);
				}
			}
			if (CacheDataBase.wfmControlTaskStateCache.containsKey(userId)) {
				if (CacheDataBase.wfmControlTaskStateCache.get(userId).size() > 0) {
					ControlTaskUtil.queryWfmExecutingTasks(CacheDataBase.wfmControlTaskStateCache.get(userId), array);
				}
			}
			resultJson.put("response", array);
			return resultJson.toString();
		} else if ("queryMessageCache".equals(command)) {
			JSONArray jsonArray = new JSONArray();
			LinkedBlockingQueue<String> list = CacheDataBase.userControlResultMessageCache.get(userId);
			if (list != null && list.size() > 0) {
				while (!list.isEmpty()) {
					jsonArray.add(list.take());
				}
			}
			resultJson.put("response", jsonArray);
			return resultJson.toString();
		} else if ("delete".equals(command)) {
			String result = ControlHandlerUtil.revolationTask(userId, controlTask.getControllerLogId(), controlTask.getControlType());
			return result;
		} else if ("deleteAll".equals(command)) {

		} else if ("execute".equals(command)) {
			try {
				String controlType = controlTask.getControlType();
				if ("wfm".equals(controlType)) {
					WFMControlTask wfmControlTask = ControlTaskUtil.fromControlTask(controlTask);
					wfmControlTask.setUserId(userId);
					resultJson = ControlHandlerUtil.wfmHandlerControlMessage(wfmControlTask, userId, farmControlService);
				} else {
					controlTask.setUserId(userId);
					resultJson = ControlHandlerUtil.handlerControlMessage(controlTask, userId, farmControlService);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				resultJson.put("response", ControlTaskEnum.ERROR);
			}
			return resultJson.toString();
		} else if ("userConfirmation".equals(command)) {
			resultJson.put(
					"response",
					ControlHandlerUtil.userConfirmationUpdate(userId, controlTask.getControllerLogId(), controlTask.getControlType(),
							controlTask.getUserConfirmation()));
			return resultJson.toString();
		}
		resultJson.put("response", ControlTaskEnum.NO_MESSAGE);
		return resultJson.toString();
	}

	@RequestMapping("farmControlOperationList")
	public String getFarmControlOperationList(FarmControlTerminal farmControlTerminal) {
		return farmControlTerminalService.getFarmControlOperationList(farmControlTerminal);
	}
}
