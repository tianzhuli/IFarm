package com.ifarm.web;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.ifarm.bean.Farm;
import com.ifarm.service.FarmService;
import com.ifarm.service.UserLogService;
import com.ifarm.util.FileUtil;

@RequestMapping(value = "farm")
@Controller
public class FarmController {
	@Autowired
	private FarmService farmService;

	@Autowired
	private UserLogService userLogService;

	@RequestMapping(value = "addFarm")
	public @ResponseBody
	String addFarmColletor(@RequestParam("userId") String userId, HttpServletRequest request, Farm farm) {
		String message = farmService.saveFarm(farm);
		userLogService.saveUserLog(request, farm, userId, "add", "farm", message);
		return message;
	}

	@RequestMapping(value = "updateFarmCollector")
	public @ResponseBody
	String updateFarmColletor(@RequestParam("userId") String userId, HttpServletRequest request, Farm farm) {
		String message = farmService.updateFarm(farm);
		userLogService.saveUserLog(request, farm, userId, "update", "farm", message);
		return message;
	}

	@RequestMapping(value = "getUserAroundFarmList")
	public @ResponseBody
	String getUserAroundFarmList(@RequestParam("aroundPersonId") String aroundPersonId) {
		return farmService.getUserAroundFarmList(aroundPersonId);
	}

	@RequestMapping(value = "getFarmColletorsList")
	public @ResponseBody
	String getFarmColletorsList(String userId) {
		return farmService.getFarmsList(userId);
	}

	/**
	 * 
	 * @param flag
	 * @param request
	 * @param response
	 * @param farm
	 * @param flag两种标准
	 *            ，添加农场时图片添加为1,已经有农场了更新图片为2,添加农场时无图片为0
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "uploadFarm")
	public @ResponseBody
	String uploadFarm(@RequestParam("flag") Integer flag, @RequestParam("userId") String userId, HttpServletRequest request, HttpServletResponse response,
			Farm farm) {
		if (flag == 0) {
			return farmService.saveFarm(farm);
		} else {
			CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			String savePath = request.getServletContext().getRealPath("/images/farms");
			if (resolver.isMultipart(request)) {
				// 将request变成多部分request
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				// 获取multiRequest 中所有的文件名
				Iterator iter = multiRequest.getFileNames();
				while (iter.hasNext()) {
					// 一次遍历所有文件
					MultipartFile file = multiRequest.getFile(iter.next().toString());
					if (file != null) {
						String fileName = file.getOriginalFilename();
						String fileRealName = FileUtil.makeFileName(fileName);
						String path = FileUtil.makeRealPath(savePath, fileRealName, userId);
						// 上传
						try {
							file.transferTo(new File(path));
							if (flag == 1) {
								farm.setFarmImageUrl(fileRealName);
								farmService.saveFarm(farm);
							} else if (flag == 2) {
								farm.setFarmImageUrl(fileRealName);
								farmService.updateFarm(farm);
							}
						} catch (IllegalStateException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							userLogService.saveUserLog(request, farm, userId, "update", "farm", "error");
							return "error";
						}
					}
				}
			}
			userLogService.saveUserLog(request, farm, userId, "update", "farm", "success");
			return "success";
		}
	}
}
