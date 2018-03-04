package com.ifarm.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.ifarm.bean.WFMControlTask;

@Repository
public class WFMControlTaskDao extends BaseDao<WFMControlTask> {
	public void saveControlTask(WFMControlTask wfmControlTask) {
		Session session = getSession();
		session.save(wfmControlTask);
	}

	public void updateControlTask(WFMControlTask wfmControlTask) {
		Session session = getSession();
		session.update(wfmControlTask);
	}

	public void delteControlTask(WFMControlTask wfmControlTask) {
		Session session = getSession();
		session.delete(wfmControlTask);
	}
}
