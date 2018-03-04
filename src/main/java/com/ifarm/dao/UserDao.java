package com.ifarm.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.ifarm.bean.Page;
import com.ifarm.bean.User;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.FileUtil;

@Repository
public class UserDao extends BaseDao<User> {
	public long userLogin(User user) {
		Session session = getSession();
		String hql = "select count(*) from User as u where u.userId=? and userPwd=?";
		Query query = session.createQuery(hql);
		query.setString(0, user.getUserId());
		query.setString(1, user.getUserPwd());
		return (long) query.list().get(0);
	}

	public void saveUser(User user) {
		Session session = getSession();
		session.save(user);
	}

	public User getUserById(String userId) {
		Session session = getSession();
		User user = (User) session.get(User.class, userId);
		if (user != null) {
			session.evict(user);
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	public List<User> getUsersListAround(String userId, Page page) {
		String hql = "select new User(u.userId,u.userName,u.userSex,u.userRegisterTime,u.userLastLoginTime,u.userBackImageUrl,u.userImageUrl,u.userSignature) from User u where u.userId<>? order by u.userLastLoginTime";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter(0, userId);
		query.setFirstResult(page.getBeginIndex());
		query.setMaxResults(page.getCount());
		List<User> list = query.list();
		for (int i = 0; i < list.size(); i++) {
			User user = (User) list.get(i);
			session.evict(user);
			if (user.getUserImageUrl() != null && user.getUserId() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user.getUserImageUrl(), user.getUserId());
				user.setUserImageUrl(userImagePath);
			}
			if (user.getUserBackImageUrl() != null && user.getUserId() != null) {
				String userImagePath = FileUtil.makeRealPathUrl(CacheDataBase.userImagePath, user.getUserBackImageUrl(), user.getUserId());
				user.setUserBackImageUrl(userImagePath);
			}
		}
		return list;
	}

	public Long subUserCount(String userId) {
		String sql = "SELECT COUNT(*) FROM `user` WHERE userId LIKE " + userId + "_%'";
		SQLQuery query = getSession().createSQLQuery(sql);
		return (Long) query.list().get(0);
	}
}
