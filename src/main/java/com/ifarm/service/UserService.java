package com.ifarm.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifarm.annotation.FarmServiceLog;
import com.ifarm.bean.ControlTask;
import com.ifarm.bean.Page;
import com.ifarm.bean.User;
import com.ifarm.bean.WFMControlTask;
import com.ifarm.constant.AuthorityConstant;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.UserDao;
import com.ifarm.nosql.bean.UserToken;
import com.ifarm.nosql.dao.UserTokenDao;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.FileUtil;
import com.ifarm.util.JsonObjectUtil;
import com.ifarm.util.RandomUtil;

@Service
public class UserService {
	@Autowired
	private UserDao userDao;

	@Autowired
	private UserTokenDao userTokenDao;

	private static final Log user_log = LogFactory.getLog(UserService.class);

	public JSONObject userResgiter(User user) {
		JSONObject jsonObject = new JSONObject();
		if (user.getUserId() != null && user.getUserPwd() != null) {
			try {
				String userId = user.getUserId();
				if (userDao.getUserById(userId) == null) {
					String token = userGetToken(user.getUserId());
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					user.setUserRegisterTime(Timestamp.valueOf(simpleDateFormat.format(new Date())));
					userDao.saveUser(user);
					// 更新内存，这个应该有问题
					CacheDataBase.userControlResultMessageCache.put(userId, new LinkedBlockingQueue<String>());
					CacheDataBase.controlTaskStateCache.put(userId, new LinkedBlockingQueue<ControlTask>());
					CacheDataBase.wfmControlTaskStateCache.put(userId, new LinkedBlockingQueue<WFMControlTask>());
					jsonObject.put("message", "success");
					jsonObject.put("token", token);
					CacheDataBase.userToken.put(user.getUserId(), token);
				} else {
					jsonObject.put("message", "repeat");
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				jsonObject.put("message", "error");
			}
		} else {
			jsonObject.put("message", "format error");
		}
		return jsonObject;
	}

	@FarmServiceLog(value = "getToken", param = "user")
	public String userGetToken(String userId) {
		if (userId == null) {
			return "error";
		}
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("ifarm");
		stringBuffer.append(UUID.randomUUID());
		String token = stringBuffer.toString().replace("-", "");
		System.out.println(userId + ":" + token);
		CacheDataBase.userToken.put(userId, token);
		return token;
	}

	public String userGetSignature(String userId) {
		if (userId == null) {
			return "error";
		}
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("ifarm");
		stringBuffer.append(UUID.randomUUID());
		stringBuffer.append(Base64.encodeBase64String(userId.getBytes()));
		String token = stringBuffer.toString().replace("-", "");
		System.out.println(userId + ":" + token);
		CacheDataBase.userSignature.put(userId, token);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		UserToken userToken = new UserToken();
		userToken.setUserId(userId);
		userToken.setTokenId(token);
		userToken.setTokenTime(simpleDateFormat.format(new Date()));
		/*
		 * try { userTokenDao.saveUserToken(userToken); } catch (Exception e) {
		 * // TODO: handle exception e.printStackTrace(); }
		 */
		return token;
	}

	public String userLogin(User user, String token) {
		if (user.getUserId() == null || user.getUserPwd() == null) {
			return "invain";
		}
		String userToken = "";
		userToken = CacheDataBase.userToken.get(user.getUserId());
		if (!token.equals(userToken)) {
			return "errorToken";
		}
		String pwdBase64 = new String(Base64.decodeBase64(user.getUserPwd()));
		user.setUserPwd(pwdBase64);
		// ReflectTraverse.traverseObject(user);
		try {
			if (userDao.userLogin(user) > 0) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				user.setUserLastLoginTime(Timestamp.valueOf((simpleDateFormat.format(new Date()))));
				userDao.updateDynamic(user);
				String signature = userGetSignature(user.getUserId());
				// System.out.println(user.getUserId() + ":redis-token：" +
				// userTokenDao.getUserToken(user.getUserId()));
				return "success" + ":" + signature;
			} else {
				return "wrong";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "error";
		}
	}

	public String updateUser(User user) {
		if (userDao.updateDynamic(user)) {
			return "success";
		} else {
			return "error";
		}
	}

	public String getUserById(String userId) {
		User user = userDao.getUserById(userId);
		if (user != null) {
			if (user.getUserImageUrl() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user.getUserImageUrl(), userId);
				user.setUserImageUrl(userImagePath);
			}
			if (user.getUserBackImageUrl() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user.getUserBackImageUrl(), userId);
				user.setUserBackImageUrl(userImagePath);
			}
			return JsonObjectUtil.toJsonObjectString(user);
		} else {
			return "null";
		}
	}

	public String getUsersListAround(String userId, Page page) {
		if (page.getBeginIndex() == null || page.getCount() == null) {
			return "valid";
		}
		List<User> list = userDao.getUsersListAround(userId, page);
		return JsonObjectUtil.toJsonArrayString(list);
	}

	public String getAllUserList(User user) {
		List<User> list = userDao.getDynamicList(user);
		for (int i = 0; i < list.size(); i++) {
			User user2 = list.get(i);
			if (user2.getUserImageUrl() != null && user2.getUserId() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user2.getUserImageUrl(), user2.getUserId());
				user2.setUserImageUrl(userImagePath);
			}
			if (user2.getUserBackImageUrl() != null && user2.getUserId() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user2.getUserBackImageUrl(), user2.getUserId());
				user2.setUserBackImageUrl(userImagePath);
			}
		}
		return JsonObjectUtil.toJsonArrayString(list);
	}

	public String addSubUser(String userId) {
		JSONObject jsonObject = new JSONObject();
		User user = userDao.getUserById(userId);
		if (AuthorityConstant.FARMER.equals(user.getUserRole())) {
			Long subUserCount = userDao.subUserCount(userId);
			if (subUserCount > 3 && !AuthorityConstant.FARMER_VIP.equals(user.getUserRole())) {
				jsonObject.put("response", SystemResultCodeEnum.USER_SUB_FULL);
				return jsonObject.toString();
			}
			String subUserId = userId + "_" + RandomUtil.randomSixInteger();
			User subUser = new User(subUserId, AuthorityConstant.INIT_PWD);
			try {
				userDao.saveUser(subUser);
				jsonObject.put("response", SystemResultCodeEnum.SUCCESS);
			} catch (Exception e) {
				// TODO: handle exception
				user_log.error(e);
				jsonObject.put("response", SystemResultCodeEnum.ERROR);
			}
		} else {
			jsonObject.put("response", SystemResultCodeEnum.NO_AUTH);
		}
		return jsonObject.toString();
	}
}
