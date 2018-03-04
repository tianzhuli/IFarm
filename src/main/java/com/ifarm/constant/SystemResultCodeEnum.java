package com.ifarm.constant;

public class SystemResultCodeEnum {

	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String TIMEOUT = "TIMEOUT";

	/**
	 * 拦截器返回的状态
	 */
	public static final String NO_USER = "null";
	public static final String NO_AUTH = "no_auth";
	public static final String EXPIRED_TOKEN = "lose efficacy";
	
	/**
	 * 设备添加返回状态
	 */
	public static final String NO_ID = "no_id";
	public static final String CATEGORY_ERROR = "category_error";
	public static final String ID_EXIST = "exist";
	
	/**
	 * 用户权限
	 */
	public static final String USER_SUB_FULL = "full_subUser";
}
