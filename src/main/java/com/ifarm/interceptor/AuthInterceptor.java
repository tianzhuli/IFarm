package com.ifarm.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.ifarm.constant.SystemResultCodeEnum;
import com.ifarm.util.CacheDataBase;

public class AuthInterceptor implements HandlerInterceptor {

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
		String signature = request.getParameter("signature");
		String userId = request.getParameter("userId");
		if (signature != null || userId != null) {
			String sign = "";
			sign = CacheDataBase.userSignature.get(userId);
			if (sign != null && signature != null && signature.equals(sign)) {
				return true;
			} else {
				response.setContentType("text/html;charset=utf-8");
				response.setCharacterEncoding("UTF-8");
				PrintWriter out = response.getWriter();
				out.print(SystemResultCodeEnum.EXPIRED_TOKEN);
				out.flush();
				out.close();
				return false;
			}
		} else {
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.print(SystemResultCodeEnum.NO_USER);
			out.flush();
			out.close();
			return false;
		}
	}
}
