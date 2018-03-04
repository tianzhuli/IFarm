package com.ifarm.bean;

import com.ifarm.util.ByteConvert;

public class ControlCommand implements Comparable<ControlCommand> {
	private ControlTask controlTask; // 一次完整使用控制操作的编号，包括开始到结束
	private String commandCategory; // 指令类别（立即执行、定时执行、手动停止、自动停止)
	private Integer level; // 命令等级（手动停止为4最高等级，其次为自动停止、往下以此为立即执行和定时执行）
	private Integer controlDeviceId;
	private int[] controlTerminalbits;

	public ControlCommand(ControlTask controlTask, String commandCategory, Integer level, int[] controlTerminalbits, Integer controlDeviceId) {
		this.controlTask = controlTask;
		this.commandCategory = commandCategory;
		this.level = level;
		this.controlTerminalbits = controlTerminalbits;
		this.controlDeviceId = controlDeviceId;
	}

	public ControlCommand(ControlTask controlTask, String commandCategory, Integer level, Integer controlDeviceId) {
		this.controlTask = controlTask;
		this.commandCategory = commandCategory;
		this.level = level;
		this.controlDeviceId = controlDeviceId;
	}

	public ControlCommand() {

	}

	public ControlTask getControlTask() {
		return controlTask;
	}

	public void setControlTask(ControlTask controlTask) {
		this.controlTask = controlTask;
	}

	public int[] getControlTerminalbits() {
		return controlTerminalbits;
	}

	public void setControlTerminalbits(int[] controlTerminalbits) {
		this.controlTerminalbits = controlTerminalbits;
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
			byte[] bits = ByteConvert.terminalBitsConvert(controlTerminalbits);
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
			arr[10] = 0x00;
			arr[11] = 0x00;
			arr[12] = 0x00;
			arr[13] = 0x00;
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
