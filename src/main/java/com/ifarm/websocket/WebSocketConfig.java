package com.ifarm.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.ifarm.interceptor.ControlInterceptor;
import com.ifarm.interceptor.HandshakeInterceptor;
import com.ifarm.service.FarmControlSystemService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	@Autowired
	private FarmControlSystemService farmControlService;

	@Autowired
	private ControlHandler controlHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// TODO Auto-generated method stub
		System.out.println("启动websocket");
		registry.addHandler(new SensorHandler(), "/sensorMainValues").addInterceptors(new HandshakeInterceptor());
		registry.addHandler(new CollectorHandler(), "/collectorValues").addInterceptors(new HandshakeInterceptor());
		registry.addHandler(controlHandler, "/controlServer").addInterceptors(new ControlInterceptor());
	}

}
