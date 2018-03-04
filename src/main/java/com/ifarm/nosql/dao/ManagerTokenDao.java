package com.ifarm.nosql.dao;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.ifarm.nosql.bean.ManagerToken;

@Repository
public class ManagerTokenDao {
	@Autowired
	private StringRedisTemplate redisTemplate;

	public void saveManagerToken(ManagerToken mToken) {
		redisTemplate.opsForValue().set(mToken.getManagerId() + "_" + "token", mToken.getToken(), 30, TimeUnit.MINUTES);
	}

	public String getManagerToken(String managerId) {
		return redisTemplate.opsForValue().get(managerId + "_token");
	}
}
