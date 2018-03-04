package com.ifarm.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class CollectByteArrayCodecFactory implements ProtocolCodecFactory {

	private ByteArrayCollectDecoder decoder;
	private ByteArrayCollectEncoder encoder;

	public CollectByteArrayCodecFactory() {
		decoder = new ByteArrayCollectDecoder();
		encoder = new ByteArrayCollectEncoder();
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return decoder;
	}

}
