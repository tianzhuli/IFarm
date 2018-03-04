package com.ifarm.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.ifarm.bean.ControlTask;

@Repository
public class ControlTaskDao extends BaseDao<ControlTask> {
	public void saveControlTask(ControlTask controlTask) {
		Session session = getSession();
		session.save(controlTask);
	}

	public void updateControlTask(ControlTask controlTask) {
		Session session = getSession();
		session.update(controlTask);
	}

	public void delteControlTask(ControlTask controlTask) {
		Session session = getSession();
		session.delete(controlTask);
	}
}
