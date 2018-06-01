package com.ifarm.util;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ifarm.bean.ControlTask;
import com.ifarm.bean.WFMControlTask;
import com.ifarm.constant.ControlTaskEnum;

public class ControlTaskUtil {
	
	public static String queryTasks(LinkedBlockingQueue<ControlTask> list, JSONArray array) {
		Iterator<ControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			array.add((iterator.next().queryCurrentTask()));
		}
		return array.toString();
	}
	
	public static String queryExecutingTasks(LinkedBlockingQueue<ControlTask> list, JSONArray array) {
		Iterator<ControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			ControlTask controlTask = iterator.next();
			if (ControlTaskEnum.EXECUTING.equals(controlTask.getTaskState())) {
				array.add(controlTask.queryCurrentTask());
			}
		}
		return array.toString();
	}

	public static String queryTasks(LinkedBlockingQueue<ControlTask> list, String controlType, JSONArray array) {
		Iterator<ControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			ControlTask controlTask = iterator.next();
			if (controlType.equals(controlTask.getControlType())) {
				array.add((controlTask.queryCurrentTask()));
			}
		}
		return array.toString();
	}
	
	public static String queryWfmTasks(LinkedBlockingQueue<WFMControlTask> list, String controlType, JSONArray array) {
		Iterator<WFMControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			WFMControlTask controlTask = iterator.next();
			if (controlType.equals(controlTask.getControlType())) {
				array.add((controlTask.queryCurrentTask()));
			}
		}
		return array.toString();
	}
	
	public static String queryWfmTasks(LinkedBlockingQueue<WFMControlTask> list, JSONArray array) {
		Iterator<WFMControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			array.add((iterator.next().queryCurrentTask()));
		}
		return array.toString();
	}
	
	public static String queryWfmExecutingTasks(LinkedBlockingQueue<WFMControlTask> list, JSONArray array) {
		Iterator<WFMControlTask> iterator = list.iterator();
		while (iterator.hasNext()) {
			WFMControlTask wfmControlTask = iterator.next();
			if (ControlTaskEnum.EXECUTING.equals(wfmControlTask.getTaskState())) {
				array.add((wfmControlTask.queryCurrentTask()));
			}
		}
		return array.toString();
	}

	public static WFMControlTask fromControlTask(ControlTask controlTask) throws Exception {
		WFMControlTask wTask = fromJson(JsonObjectUtil.fromOriginalBean(controlTask));
		return wTask;
	}
	
	/**
	 * 
	 * @param jsonObject
	 * @return
	 * @throws Exception 该异常在contrlhandler处被处理，避免加入到cache中
	 */
	public static WFMControlTask fromJson(JSONObject jsonObject) throws Exception{
		WFMControlTask controlStrategy = new WFMControlTask();
		Field[] fields = controlStrategy.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			String key = fields[i].getName();
			if (jsonObject.containsKey(key)) {
					if (fields[i].getType() == Integer.class) {
						fields[i].set(controlStrategy, jsonObject.getInt(key));
					} else if (fields[i].getType() == String.class) {
						fields[i].set(controlStrategy, jsonObject.getString(key));
					} else if (fields[i].getType() == Double.class) {
						fields[i].set(controlStrategy, jsonObject.getDouble(key));
					} else if (fields[i].getType() == Boolean.class) {
						fields[i].set(controlStrategy, jsonObject.getBoolean(key));
					} else if (fields[i].getType() == Long.class) {
						fields[i].set(controlStrategy, jsonObject.getLong(key));
					} else if (fields[i].getType() == Timestamp.class) {
						fields[i].set(controlStrategy, Timestamp.valueOf(jsonObject.getString(key)));
					} else {
						if (key.equals("format")) {
							continue;
						}// 临时弥补一下
						fields[i].set(controlStrategy, jsonObject.get(key));
					}
				
			}
		}
		return controlStrategy;
	}

}
