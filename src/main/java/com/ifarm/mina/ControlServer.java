package com.ifarm.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.ifarm.util.CacheDataBase;

public class ControlServer {
	public static void init() throws IOException {
		ControlServer server = new ControlServer();
		server.start();
	}

	private void start() throws IOException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 300);
		acceptor.setHandler(new ControlByteIoHandler());
		// 设置日志记录器
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ControlByteArrayCodecFactory()));
		acceptor.bind(new InetSocketAddress(CacheDataBase.controlPort));
	}
}
