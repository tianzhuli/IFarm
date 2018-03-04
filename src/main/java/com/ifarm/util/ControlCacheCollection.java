package com.ifarm.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ifarm.bean.ControlCommand;
import com.ifarm.bean.ControlTask;
import com.ifarm.bean.WFMControlCommand;
import com.ifarm.bean.WFMControlTask;
import com.ifarm.constant.ControlTaskEnum;
import com.ifarm.service.ControlTaskService;
import com.ifarm.service.WFMControlTaskService;

/**
 * 
 * @author Administrator 控制缓存的自动清理，后期需要优化
 *         首先考虑已经有添加回复的task，就已经有开始执行时间starttime，之后获取当前时间
 *         -starttime-waittime-executiontime-偏差值>0即可回收
 *         此线程及其重要，关系到整个控制系统的正常运行，后期需要好好优化
 */
@Component
public class ControlCacheCollection implements Runnable {
	private ControlTaskService controlTaskService;
	private WFMControlTaskService wfmControlTaskService;
	private static final Log controlCacheCollection_log = LogFactory.getLog(ControlCacheCollection.class);
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private int offset = 0; // 这个就是一般的误差时间
	private int timeout = 0;
	volatile private boolean isStartCollect = true;

	@Autowired
	public void setTaskService(ControlTaskService taskService) {
		this.controlTaskService = taskService;
	}

	public ControlTaskService getTaskService() {
		return controlTaskService;
	}

	public WFMControlTaskService getWfmControlTaskService() {
		return wfmControlTaskService;
	}

	@Autowired
	public void setWfmControlTaskService(WFMControlTaskService wfmControlTaskService) {
		this.wfmControlTaskService = wfmControlTaskService;
	}

	public boolean isStartCollect() {
		return isStartCollect;
	}

	public void setStartCollect(boolean isStartCollect) {
		this.isStartCollect = isStartCollect;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * 
	 * @param task
	 * @param collectorId
	 * @param userId
	 * @param isStop
	 *            标志位,是否从cache中移除task
	 */
	public void isStopControlTask(ControlTask task, String userId, boolean isStop, boolean isUpdateTask) {
		try {
			if (isStop) {
				LinkedBlockingQueue<ControlTask> controlTasks = CacheDataBase.controlTaskStateCache.get(userId);
				controlTasks.remove(task); // 从cache中移除该任务
			}
			if (isUpdateTask) {
				getTaskService().updateControlTask(task);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void wfdIsStopControlTask(WFMControlTask wTask, String userId, boolean isStop, boolean isUpdateTask) {
		try {
			if (isStop) {
				LinkedBlockingQueue<WFMControlTask> controlTasks = CacheDataBase.wfmControlTaskStateCache.get(userId);
				controlTasks.remove(wTask); // 从cache中移除该任务
			}
			if (isUpdateTask) {
				getWfmControlTaskService().updateControlTask(wTask);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 系统计算时间，下发停止指令
	 * 
	 * @param controlTask
	 * @throws Exception
	 */
	public void controlTaskStopCommandProduce(ControlTask controlTask) throws Exception {
		controlTask.setLevel(3);
		ControlCommand command = controlTask.buildCommand("stop");
		Long collectorId = controlTask.getCollectorId();
		if (collectorId != null) {
			PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
			if (pQueue != null) {
				pQueue.add(command);
				controlTask.setStartStopTime(System.currentTimeMillis() / 1000);
				controlTask.setTaskState(ControlTaskEnum.STOPPING);
			}
		}// 下发添加任务到设备
		CacheDataBase.ioControlData.notifyObservers(collectorId);
		controlCacheCollection_log.info("任务正在停止中......");
	}

	public void clearControlTaskCommand(ControlTask controlTask, Long collectorId) {
		if (collectorId != null) {
			PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
			if (pQueue != null) {
				Iterator<ControlCommand> iterator = pQueue.iterator();
				while (iterator.hasNext()) {
					ControlCommand command = iterator.next();
					if (controlTask.equals(command.getControlTask())) {
						pQueue.remove(command);
					}
				}
			}
		}
	}

	public void wfmClearControlTaskCommand(WFMControlTask wfmControlTask) {
		List<WFMControlCommand> list = wfmControlTask.getWfmControlCommands();
		for (int i = 0; i < list.size(); i++) {
			WFMControlCommand wCommand = list.get(i);
			Long collectorId = wCommand.getCollectorId();
			if (collectorId != null) {
				PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
				if (pQueue != null) {
					Iterator<ControlCommand> iterator = pQueue.iterator();
					while (iterator.hasNext()) {
						ControlCommand command = iterator.next();
						if (wCommand != null && wCommand.equals(command)) {
							pQueue.remove(command);
						}
					}
				}
			}
		}
	}

	public void wfmControlTaskStopCommandProduce(WFMControlTask wfmControlTask) throws Exception {
		wfmControlTask.setLevel(3);
		wfmControlTask.setStartStopTime(System.currentTimeMillis() / 1000);
		List<WFMControlCommand> list = wfmControlTask.getWfmControlCommands();
		for (int i = list.size() - 1; i >= 0; i--) {
			WFMControlCommand command = list.get(i);
			Long collectorId = command.getCollectorId();
			command.setCommandCategory("stop");
			if (collectorId != null) {
				PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
				pQueue.add(command);
				CacheDataBase.ioControlData.notifyObservers(collectorId); // 推送到长连接设备
			}
		}
		wfmControlTask.setTaskState(ControlTaskEnum.STOPPING);
		controlCacheCollection_log.info("任务正在停止中......");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("---回收线程开启---");
		while (true) {
			while (isStartCollect) {
				for (Entry<String, LinkedBlockingQueue<ControlTask>> entry : CacheDataBase.controlTaskStateCache.entrySet()) {
					String userId = entry.getKey();
					LinkedBlockingQueue<ControlTask> controlTasks = entry.getValue();
					if (controlTasks.size() > 0) {
						try {
							Iterator<ControlTask> iterator = controlTasks.iterator();
							while (iterator.hasNext()) {
								ControlTask controlTask = iterator.next();
								try {
									String taskState = controlTask.getTaskState();
									if (ControlTaskEnum.WAITTING.equals(taskState) || ControlTaskEnum.CONFICTING.equals(taskState)) {
										long timeDev = Timestamp.valueOf(format.format(new Date())).getTime() / 1000
												- controlTask.getStartExecutionTime().getTime() / 1000;
										if (timeDev > 0) { // 执行任务的时间到了
											if (!ControlHandlerUtil.judgeControlTaskConflict(controlTask, true)) {
												controlTask.setTaskState(ControlTaskEnum.BLOCKING); // 任务下发，不一定真的执行了
												controlTask.setStartExecutionTime(Timestamp.valueOf(format.format(new Date())));
												ControlCommand command = controlTask.buildCommand("execution");
												Long collectorId = controlTask.getCollectorId();
												// 这个地方需要考虑容错，假如上一个任务由于设备原因，延迟了30s，这个新的任务会对之前的任务有影响
												if (collectorId != null) {
													PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
													if (pQueue != null) {
														pQueue.add(command);
													}
												}// 下发添加任务到设备
												CacheDataBase.ioControlData.notifyObservers(collectorId);
												// CacheDataBase.userControlData.notifyObservers(userId,
												// controlTask.pushUserMessage());//
												// 通知用户
												controlCacheCollection_log.info(userId + "---发现到了时间应该执行的任务---");
											} else {
												// controlCacheCollection_log.info(userId
												// + "---任务冲突---");
												controlTask.setTaskState(ControlTaskEnum.CONFICTING);
											}
										}
									}
									Long collectorId = controlTask.getCollectorId();
									if (controlTask.isStopReceived()) {
										if (ControlTaskEnum.STOP_FAIL.equals(controlTask.getStopResult())) {
											controlTask.setResponseMessage(ControlTaskEnum.STOP_FAIL_RESPONSE);
											CacheDataBase.userControlData.notifyObservers(userId, controlTask.pushUserMessage());// 通知用户
											isStopControlTask(controlTask, userId, true, true);
											controlCacheCollection_log.info("停止失败的：" + userId + "集中器回收");
										} else if (ControlTaskEnum.STOP_SUCCESS.equals(controlTask.getStopResult())) {
											controlTask.setResponseMessage(ControlTaskEnum.STOP_SUCESS_RESPONSE);
											isStopControlTask(controlTask, userId, true, true);
											controlCacheCollection_log.info("已经收到停止指令的：" + userId + "集中器回收");
										}
										continue;
									}
									if (controlTask.isAddReceived()) {
										if (controlTask.getStartStopTime() != 0) { // 已经下发了停止指令
											long compare = Timestamp.valueOf(format.format(new Date())).getTime() / 1000 - controlTask.getStartStopTime();
											if (compare >= this.offset) {
												controlTask.setResponseMessage(ControlTaskEnum.STOP_TIMEOUT_RESPONSE);
												isStopControlTask(controlTask, userId, true, true);
												CacheDataBase.userControlData.notifyObservers(userId, controlTask.pushUserMessage());// 通知用户
												clearControlTaskCommand(controlTask, collectorId);
												controlCacheCollection_log.info(compare + "s长时间未收到停止回复：" + collectorId + "集中器");
											}
											continue;
										}
										if (ControlTaskEnum.EXECUTION_FAIL.equals(controlTask.getAddResult())) {
											controlTask.setResponseMessage(ControlTaskEnum.EXECUTION_FAIL_RESPONSE);
											isStopControlTask(controlTask, userId, true, true);
											CacheDataBase.userControlData.notifyObservers(userId, controlTask.pushUserMessage());// 通知用户
											controlCacheCollection_log.info("任务执行失败的：" + userId + "集中器回收");
											continue;
										} else if (ControlTaskEnum.EXEUTION_SUCCESS.equals(controlTask.getAddResult())) {
											long compare = System.currentTimeMillis() / 1000 - controlTask.getAddResultTime()
													- controlTask.getExecutionTime(); // this.offset是偏差系数
											if (compare >= 0) {
												controlTask.setResponseMessage(ControlTaskEnum.EXECUTION_COMPLETE_RESPONSE); // 任务执行完成
												controlCacheCollection_log.info("任务执行完成：" + userId + "集中器");
												controlTaskStopCommandProduce(controlTask);
												CacheDataBase.userControlData.notifyObservers(userId, controlTask.pushUserMessage());// 通知用户
												continue;
											}
										}
									} else {
										long current = Timestamp.valueOf(format.format(new Date())).getTime() / 1000
												- controlTask.getStartExecutionTime().getTime() / 1000;
										if (current >= this.offset * this.timeout) {
											controlTask.setResponseMessage(ControlTaskEnum.EXECUTION_TIMEOUT_RESPONSE);
											isStopControlTask(controlTask, userId, true, true);
											CacheDataBase.userControlData.notifyObservers(userId, controlTask.pushUserMessage());// 通知用户
											clearControlTaskCommand(controlTask, collectorId);
											controlCacheCollection_log.info(current + "s长时间未收到添加回复：" + collectorId + "集中器");
											continue;
										}
									}
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
									controlCacheCollection_log.error("回收对象异常：" + e);
									isStopControlTask(controlTask, userId, true, true);
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							controlCacheCollection_log.error("回收对象异常：" + e);
						}
					}
				}
				/**
				 * 水肥药一体化的回收
				 */
				for (Entry<String, LinkedBlockingQueue<WFMControlTask>> entry : CacheDataBase.wfmControlTaskStateCache.entrySet()) {
					String userId = entry.getKey();
					LinkedBlockingQueue<WFMControlTask> controlTasks = entry.getValue();
					if (controlTasks.size() > 0) {
						try {
							Iterator<WFMControlTask> iterator = controlTasks.iterator();
							while (iterator.hasNext()) {
								WFMControlTask wfmControlTask = iterator.next();
								try {
									String taskState = wfmControlTask.getTaskState();
									if (ControlTaskEnum.WAITTING.equals(taskState)) {
										long timeDev = Timestamp.valueOf(format.format(new Date())).getTime() / 1000
												- wfmControlTask.getStartExecutionTime().getTime() / 1000;
										if (timeDev > 0) { // 执行任务的时间到了
											if (!ControlHandlerUtil.wfmJudgeControlTaskConflict(wfmControlTask, true)) {
												wfmControlTask.setTaskState(ControlTaskEnum.BLOCKING); // 任务下发，不一定真的执行了
												wfmControlTask.setStartExecutionTime(Timestamp.valueOf(format.format(new Date())));
												List<WFMControlCommand> list = wfmControlTask.getWfmControlCommands();
												for (int i = 0; i < list.size(); i++) {
													WFMControlCommand wfmControlCommand = list.get(i);
													Long collectorId = wfmControlCommand.getCollectorId();
													if (collectorId != null) {
														PriorityBlockingQueue<ControlCommand> pQueue = CacheDataBase.controlCommandCache.get(collectorId);
														if (pQueue != null) {
															pQueue.add(wfmControlCommand);
														}
													}
													CacheDataBase.ioControlData.notifyObservers(collectorId); // 推送到长连接设备
												}
												controlCacheCollection_log.info(userId + "---发现到了时间应该执行的任务---");
											} else {
												// controlCacheCollection_log.info(userId
												// + "---任务冲突---");
												wfmControlTask.setTaskState(ControlTaskEnum.CONFICTING);
											}
										}
									}
									if (wfmControlTask.isStopReceived()) {
										if (ControlTaskEnum.STOP_FAIL.equals(wfmControlTask.getStopResult())) {
											wfmControlTask.setResponseMessage(ControlTaskEnum.STOP_FAIL_RESPONSE);
											wfdIsStopControlTask(wfmControlTask, userId, true, true);
											CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
											controlCacheCollection_log.info("停止失败的：" + userId + "集中器回收");
										} else if (ControlTaskEnum.STOP_SUCCESS.equals(wfmControlTask.getStopResult())) {
											wfmControlTask.setResponseMessage(ControlTaskEnum.STOP_SUCESS_RESPONSE);
											wfdIsStopControlTask(wfmControlTask, userId, true, true);
											controlCacheCollection_log.info("已经收到停止指令的：" + userId + "集中器回收");
										}
										// CacheDataBase.userControlData.notifyObservers(userId,
										// wfmControlTask.pushUserMessage());// 通知用户
										continue;
									}
									if (wfmControlTask.isAddReceived()) {
										if (wfmControlTask.getStartStopTime() != 0) { // 已经下发了停止指令
											long compare = Timestamp.valueOf(format.format(new Date())).getTime() / 1000
													- wfmControlTask.getStartStopTime();
											if (compare >= this.offset) {
												wfmControlTask.setResponseMessage(ControlTaskEnum.STOP_TIMEOUT_RESPONSE);
												wfdIsStopControlTask(wfmControlTask, userId, true, true);
												CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
												wfmClearControlTaskCommand(wfmControlTask);
												controlCacheCollection_log.info(compare + "s长时间未收到停止回复：" + wfmControlTask.getControllerLogId() + "任务");
											}
											continue;
										}
										if (ControlTaskEnum.EXECUTION_FAIL.equals(wfmControlTask.getAddResult())) {
											wfmControlTask.setResponseMessage(ControlTaskEnum.EXECUTION_FAIL_RESPONSE);
											wfdIsStopControlTask(wfmControlTask, userId, false, true);
											// 需要紧急停止，下发停止指令
											wfmControlTaskStopCommandProduce(wfmControlTask);
											CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
											controlCacheCollection_log.info("任务执行失败的：" + userId + "集中器回收");
											continue;
										} else if (ControlTaskEnum.EXEUTION_SUCCESS.equals(wfmControlTask.getAddResult())) {
											long compare = System.currentTimeMillis() / 1000 - wfmControlTask.getAddResultTime()
													- wfmControlTask.getExecutionTime(); // this.offset是偏差系数
											if (compare >= 0) {
												wfmControlTask.setResponseMessage(ControlTaskEnum.EXECUTION_COMPLETE_RESPONSE); // 任务执行完成
												controlCacheCollection_log.info("任务执行完成：" + userId + "集中器");
												wfmControlTaskStopCommandProduce(wfmControlTask);
												CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
												continue;
											}
										}
									} else {
										long current = Timestamp.valueOf(format.format(new Date())).getTime() / 1000
												- wfmControlTask.getStartExecutionTime().getTime() / 1000;
										if (current >= this.offset * this.timeout) {
											wfmControlTask.setResponseMessage(ControlTaskEnum.EXECUTION_TIMEOUT_RESPONSE);
											wfdIsStopControlTask(wfmControlTask, userId, true, true);
											CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
											wfmClearControlTaskCommand(wfmControlTask);
											// 考虑是否下发停止命令，有可能是部分设备未收到，其他设备已经开始运行了
											controlCacheCollection_log.info(current + "s长时间未收到添加回复：" + wfmControlTask.getControllerLogId() + "任务");
											continue;
										} /*else if (current >= this.offset) {
											List<WFMControlCommand> list = wfmControlTask.getWfmControlCommands();
											int count = 0;
											StringBuilder faultIndentifying = new StringBuilder();
											for (int i = 0; i < list.size(); i++) {
												WFMControlCommand wfmControlCommand = list.get(i);
												if (wfmControlCommand.isReceived()) {
													count++;
												} else {
													faultIndentifying.append(wfmControlCommand.getIndentifying());
												}
											}
											if (count > 0) {
												// 部分设备超过20s未收到回复
												wfmControlTask.setAddReceived(true);
												wfmControlTask.setResponseMessage(ControlTaskEnum.EXECUTION_TIMEOUT_RESPONSE);
												wfmControlTask.setFaultIndentifying(faultIndentifying.toString());
												CacheDataBase.userControlData.notifyObservers(userId, wfmControlTask.pushUserMessage());// 通知用户
												wfmControlTaskStopCommandProduce(wfmControlTask);
												controlCacheCollection_log.info(current + "s部分设备长时间未收到添加回复：" + faultIndentifying);
											}
										}*/
									}
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
									controlCacheCollection_log.error("回收对象异常：" + e);
									wfdIsStopControlTask(wfmControlTask, userId, true, true);
									//清除异常的任务
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							controlCacheCollection_log.error("回收对象异常：" + e);
						}
					}
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}