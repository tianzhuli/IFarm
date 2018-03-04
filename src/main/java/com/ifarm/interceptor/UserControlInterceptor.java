package com.ifarm.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.ifarm.bean.User;
import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.dao.UserDao;
import com.ifarm.util.CacheDataBase;

public class UserControlInterceptor implements HandlerInterceptor{
	@Autowired
	private UserDao userDao;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// TODO Auto-generated method stub
		String signature = request.getParameter("signature");
		String userId = request.getParameter("userId");
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		if (signature != null || userId != null) {
			String sign = "";
			sign = CacheDataBase.userSignature.get(userId);
			if (sign != null && signature != null && signature.equals(sign)) {
				User user = userDao.getUserById(userId);
				if (ControlAuthConstans.ONLY_SEE.equals(user.getUserRole())) {
					out.print(SystemResultCodeEnum.NO_AUTH);
					out.flush();
					out.close();
					return false;
				}
				return true;
			} else {	
				out.print(SystemResultCodeEnum.EXPIRED_TOKEN);
				out.flush();
				out.close();
				return false;
			}
		} else {
			out.print(SystemResultCodeEnum.NO_USER);
			out.flush();
			out.close();
			return false;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
