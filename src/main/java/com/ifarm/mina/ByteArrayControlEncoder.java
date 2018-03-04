package com.ifarm.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.ifarm.util.ByteConvert;

public class ByteArrayControlEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		byte[] bytes = (byte[]) message;
		byte[] lens = ByteConvert.intToByte2(bytes.length);
		byte checkNum = ByteConvert.checekByte(bytes, 0, bytes.length - 1);
		IoBuffer buffer = IoBuffer.allocate(256);
		buffer.setAutoExpand(true);
		buffer.put((byte) 0x68);
		buffer.put(lens[0]);
		buffer.put(lens[1]);
		buffer.put((byte) 0x68);
		buffer.put(bytes);
		buffer.put(checkNum);
		buffer.put((byte) 0x16);
		buffer.flip();
		out.write(buffer);
		out.flush();
		buffer.free();
	}
}
