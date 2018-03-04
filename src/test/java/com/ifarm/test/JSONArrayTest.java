package com.ifarm.test;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class JSONArrayTest {
	public static void main(String[] args) {
		JSONArray array = new JSONArray();
		update(array);
		System.out.println(array);
		try {
			JSONObject jsonObject = JSONObject.fromObject(array.toString());
			System.out.println(jsonObject);
		} catch (JSONException e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}	
	}
	
	public static void update(JSONArray array) {
		array.add("123");
	}
}
