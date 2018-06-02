package com.ifarm.bean;

import com.ifarm.util.ByteConvert;

public class WFMControlCommand extends ControlCommand {
	
	private static final long serialVersionUID = 3603987618260925806L;
	private WFMControlTask wfmControlTask; // 一次完整使用控制操作的编号，包括开始到结束
	private Integer taskId;
	private String commandCategory; // 指令类别（立即执行、定时执行、手动停止、自动停止)
	private Integer level; // 命令等级（手动停止为4最高等级，其次为自动停止、往下以此为立即执行和定时执行）
	private Integer controlDeviceId;
	private int[] forwardbits;
	private int[] reversebits;
	private boolean isReceived;
	private String receivedResult;
	private long receiveTime;
	private String indentifying; // 标识，指该命令或者该设备控制了几个逻辑单元
	Long collectorId;

	public boolean isReceived() {
		return isReceived;
	}

	public void setReceived(boolean isReceived) {
		this.isReceived = isReceived;
	}

	public String getReceivedResult() {
		return receivedResult;
	}

	public void setReceivedResult(String receivedResult) {
		this.receivedResult = receivedResult;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public WFMControlCommand(WFMControlTask wfmControlTask, String commandCategory, Integer level, Integer controlDeviceId, Long collectorId) {
		this.wfmControlTask = wfmControlTask;
		this.taskId = wfmControlTask.getControllerLogId();
		this.commandCategory = commandCategory;
		this.level = level;
		this.controlDeviceId = controlDeviceId;
		this.collectorId = collectorId;
	}

	public WFMControlCommand() {

	}

	public WFMControlTask getWfmControlTask() {
		return wfmControlTask;
	}

	public void setWfmControlTask(WFMControlTask wfmControlTask) {
		this.wfmControlTask = wfmControlTask;
	}

	public int[] getForwardbits() {
		return forwardbits;
	}

	public void setForwardbits(int[] forwardbits) {
		this.forwardbits = forwardbits;
	}

	public int[] getReversebits() {
		return reversebits;
	}

	public void setReversebits(int[] reversebits) {
		this.reversebits = reversebits;
	}

	public Long getCollectorId() {
		return collectorId;
	}

	public void setCollectorId(Long collectorId) {
		this.collectorId = collectorId;
	}

	public String getCommandCategory() {
		return commandCategory;
	}

	public void setCommandCategory(String commandCategory) {
		this.commandCategory = commandCategory;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getControlDeviceId() {
		return controlDeviceId;
	}

	public void setControlDeviceId(Integer controlDeviceId) {
		this.controlDeviceId = controlDeviceId;
	}

	public String getIndentifying() {
		return indentifying;
	}

	public void setIndentifying(String indentifying) {
		this.indentifying = indentifying;
	}
	
	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public byte[] commandToByte() {
		byte[] arr = null;
		switch (this.commandCategory) {
		case "execution":
			arr = new byte[15];
			arr[0] = (byte) 0xFD;
			arr[1] = 0x0B;
			arr[2] = 0x01;
			arr[3] = (byte) 0xFF;
			arr[4] = (byte) 0xD0;
			arr[5] = 0x06;
			byte[] collectorArray = ByteConvert.convertTobyte(String.valueOf(this.controlDeviceId));
			arr[6] = collectorArray[0];
			arr[7] = collectorArray[1];
			arr[8] = collectorArray[2];
			arr[9] = collectorArray[3];
			byte[] bits = ByteConvert.terminalBitsConvert(forwardbits);
			arr[10] = bits[0];
			arr[11] = bits[1];
			arr[12] = bits[2];
			arr[13] = bits[3];
			arr[14] = ByteConvert.checekByte(arr, 4, 13);
			break;
		case "stop":
			arr = new byte[15];
			arr[0] = (byte) 0xFD;
			arr[1] = 0x0B;
			arr[2] = 0x01;
			arr[3] = (byte) 0xFF;
			arr[4] = (byte) 0xD0;
			arr[5] = 0x06;
			byte[] collectorArrayStop = ByteConvert.convertTobyte(String.valueOf(this.controlDeviceId));
			arr[6] = collectorArrayStop[0];
			arr[7] = collectorArrayStop[1];
			arr[8] = collectorArrayStop[2];
			arr[9] = collectorArrayStop[3];
			byte[] rbits = ByteConvert.terminalBitsConvert(reversebits);
			arr[10] = rbits[0];
			arr[11] = rbits[1];
			arr[12] = rbits[2];
			arr[13] = rbits[3];
			arr[14] = ByteConvert.checekByte(arr, 4, 13);
			break;
		default:
			arr = new byte[0];
			break;
		}
		return arr;
	}

	@Override
	public int compareTo(ControlCommand o) {
		// TODO Auto-generated method stub
		if (this.level < o.getLevel()) {
			return 1;
		} else if (this.level > o.getLevel()) {
			return -1;
		} else {
			return 0;
		}
	}

}
