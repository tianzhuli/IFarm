package com.ifarm.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.ifarm.annotation.AuthPassport;
import com.ifarm.nosql.dao.ManagerTokenDao;

public class ManagerInterceptor implements HandlerInterceptor {
	@Autowired
	private ManagerTokenDao managerTokenDao;

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// TODO Auto-generated method stub
		String managerId = request.getParameter("managerId");
		AuthPassport authPassport = ((HandlerMethod) handler).getMethodAnnotation(AuthPassport.class);
		if (authPassport != null && authPassport.validate() == false) {
			System.out.println("验证是管理员登录操作");
			if (managerId == null) {
				return false;
			} else {
				return true;
			}
		}
		String token = request.getParameter("token");
		if (token == null || managerId == null) {
			return false;
		}
		if (!managerTokenDao.getManagerToken(managerId).equals(token)) {
			System.out.println("管理员验证失败");
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.print("lose efficacy");
			out.flush();
			out.close();
			return false;
		}
		return true;
	}

}
