package com.ifarm.bean;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "production_device")
public class ProductionDevice {
	@Id
	private Integer deviceId;
	private String deviceVerification;
	private Timestamp createTime;
	private String deviceType;
	private String deviceCategory;
	private String deviceDescription;

	@Transient
	private static transient SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceVerification() {
		return deviceVerification;
	}

	public void setDeviceVerification(String deviceVerification) {
		this.deviceVerification = deviceVerification;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(String deviceCategory) {
		this.deviceCategory = deviceCategory;
	}

	public String getDeviceDescription() {
		return deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	public ProductionDevice() {

	}

	public ProductionDevice(String deviceVerification, String deviceType, String deviceCategory, String deviceDescription) {
		this.deviceVerification = deviceVerification;
		this.deviceType = deviceType;
		this.deviceCategory = deviceCategory;
		this.deviceDescription = deviceDescription;
		this.createTime = Timestamp.valueOf(formatter.format(new Date()));
	}
}
