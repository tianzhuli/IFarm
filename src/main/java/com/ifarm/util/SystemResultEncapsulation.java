package com.ifarm.util;

import net.sf.json.JSONObject;

public class SystemResultEncapsulation {
	
	public static String resultCodeDecorate(String resultCode) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("response", resultCode);
		return jsonObject.toString();
	}
}
