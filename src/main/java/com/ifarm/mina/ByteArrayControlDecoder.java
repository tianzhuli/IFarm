package com.ifarm.mina;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.ifarm.util.ConvertData;

public class ByteArrayControlDecoder extends CumulativeProtocolDecoder {
	private static final Log decode_log = LogFactory.getLog(ByteArrayControlDecoder.class);
	ConvertData convertData = new ConvertData();

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		if (in.remaining() > 0) {
			in.mark(); // 标记位置
			if (in.remaining() < 4) {
				return false;
			}
			byte[] header = new byte[4];
			byte[] bytes = null;
			in.get(header);
			if (header[0] != 0x68 || header[3] != 0x68) {
				decode_log.info("------数据包错误------");
				return false;
			}
			int len = convertData.getdataType3(header, 1);
			if (len + 2 > in.remaining()) {
				in.reset(); // 可能出现了拆包现象
				return false;
			} else {
				bytes = new byte[len + 2];
				in.get(bytes);
				if (bytes[bytes.length - 1] != 0x16) {
					return false; // 错误包
				}
			}
			byte[] arr = new byte[len];
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < header.length; i++) {
				buffer.append(Integer.toHexString(header[i] & 0xff) + " ");
			}
			for (int i = 0; i < bytes.length; i++) {
				buffer.append(Integer.toHexString(bytes[i] & 0xff) + " ");
			}
			decode_log.debug("设备发送的数据包:" + buffer.toString());
			for (int i = 0; i < len; i++) {
				arr[i] = bytes[i];
			}
			out.write(arr);
			if (in.remaining() > 0) {
				return true; // 黏包了
			}
		}
		// 处理成功，让父类进行接收下个包
		return false;
	}
}
