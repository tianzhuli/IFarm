package com.ifarm.web;

import java.util.List;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ifarm.util.CacheDataBase;

@Controller
@RequestMapping(value = "sensorParameter")
public class SensorParameterController {
	@RequestMapping(value = "parameterType")
	public @ResponseBody
	String getsensorParameterType(@RequestParam("sensorId") String sensorId) {
		List<String> list = null;
		list = CacheDataBase.sensorParameterMap.get(sensorId);
		return JSONArray.fromObject(list).toString();
	}
}
