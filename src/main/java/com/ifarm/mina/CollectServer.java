package com.ifarm.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.ifarm.util.CacheDataBase;

public class CollectServer {
	public static void init() throws IOException {
		CollectServer server = new CollectServer();
		server.start();
	}

	private void start() throws IOException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
		acceptor.setHandler(new CollectHandler());
		// 设置日志记录器
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CollectByteArrayCodecFactory()));
		acceptor.bind(new InetSocketAddress(CacheDataBase.port));
	}
	
	public static void main(String[] args) throws IOException {
		CacheDataBase.port = 9001;
		init();
		/*ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String("applicationContext.xml"));
		context.start();*/
	}
}
