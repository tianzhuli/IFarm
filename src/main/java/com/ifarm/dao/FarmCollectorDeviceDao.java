package com.ifarm.dao;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.ifarm.bean.FarmCollectorDevice;

@Repository
public class FarmCollectorDeviceDao extends BaseDao<FarmCollectorDevice> {

	public void saveFarmCollectorDevice(FarmCollectorDevice farmCollectorDevice) {
		Session session = getSession();
		session.save(farmCollectorDevice);
	}

	public List<?> builderDeviceOrder(Integer farmId) {
		Session session = getSession();
		String sql = "SELECT f.deviceOrderNo FROM farm_collector_device f WHERE f.farmId=? ORDER BY f.deviceOrderNo DESC LIMIT 0,1";
		SQLQuery sqlQuery = session.createSQLQuery(sql);
		sqlQuery.setParameter(0, farmId);
		return sqlQuery.list();
	}

}
