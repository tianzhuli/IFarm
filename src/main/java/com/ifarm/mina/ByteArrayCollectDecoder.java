package com.ifarm.mina;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.ifarm.bean.CollectorDeviceValue;
import com.ifarm.bean.DeviceValueBase;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.ConvertData;

public class ByteArrayCollectDecoder extends CumulativeProtocolDecoder {
	private static final Log decode_log = LogFactory.getLog(ByteArrayCollectDecoder.class);
	private ConvertData convertData = new ConvertData();
	int offset = 2; //底层的bug，服务器解决
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		Object isHeader = session.getAttribute("state");
		if (isHeader == null) {
			if (in.remaining() > 18) { // 包头最少14个字节,长度不需要知道
				byte[] bytes = new byte[18];
				in.get(bytes);
				if (bytes[0] != 0x68 || bytes[3] != 0x68 || bytes[bytes.length - 1] != 0x16) {
					decode_log.info("------数据包错误------");
					return false;
				}
				session.setAttribute("state", new Object());
				int num = convertData.getdataType3(bytes, 10);// 在线数量，解析表头
				/*long collectorId = convertData.getdataType5(bytes, 12);
				decode_log.info("集中器编号：" + collectorId);*/
				CacheDataBase.collectorValuesService.saveCollectorValues(bytes, 12);
				// 判断缓存里面是否有指令下发
				session.setAttribute("num", num);
				resolveCollectorData(session, in, num, out);
			}
		} else {
			int num = (int) session.getAttribute("num");
			resolveCollectorData(session, in, num, out);

		}
		// 处理成功，让父类进行接收下个包
		return false;
	}

	public void resolveCollectorData(IoSession session, IoBuffer in, int num, ProtocolDecoderOutput out) {
		while (in.remaining() >= 6 && num > 0) {
			in.mark();
			byte[] numberIdArray = new byte[4];
			in.get(numberIdArray);
			byte[] sizeArray = new byte[4]; //4个字节，其中包括信号强度2个字节，一个采集状态一个字节，一个长度一个字节
			in.get(sizeArray);
			long id = convertData.byteToConvertLong(numberIdArray, 0, 4);
			int size = convertData.getdataType1(sizeArray, 3);
			if (in.remaining() >= size + offset) {
				byte[] data = new byte[size + offset];
				in.get(data);
				// 得到一组数据
				// 根据不同的设备类型应该要创建不同的对象
				DeviceValueBase collectorDeviceValue = new CollectorDeviceValue();
				collectorDeviceValue.setCollectData(data, size);
				collectorDeviceValue.setDeviceId(id);
				num--;
				CacheDataBase.dValueService.saveCollectorDeviceValues(collectorDeviceValue);
				if (CacheDataBase.collcetorDeviceMainValueCacheMap.containsKey(collectorDeviceValue.getDeviceId())) {
					List<DeviceValueBase> list = CacheDataBase.collcetorDeviceMainValueCacheMap.get(collectorDeviceValue.getDeviceId());
					if (list.size() >= CacheDataBase.cacheSize) {
						list.remove(0);
						list.add(collectorDeviceValue);
					} else {
						list.add(collectorDeviceValue);
					}
				} else {
					List<DeviceValueBase> list = new ArrayList<DeviceValueBase>();
					list.add(collectorDeviceValue);
					CacheDataBase.collcetorDeviceMainValueCacheMap.put(collectorDeviceValue.getDeviceId(), list);
				}
				CacheDataBase.collectorDeviceMainValueMap.put(collectorDeviceValue.getDeviceId(), collectorDeviceValue);
				session.setAttribute("num", num);
			} else {
				in.reset();
				return;
			}
		}
		if (num == 0) {
			out.write("");
		}
	}
}
