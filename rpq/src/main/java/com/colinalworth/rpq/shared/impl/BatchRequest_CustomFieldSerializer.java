package com.colinalworth.rpq.shared.impl;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class BatchRequest_CustomFieldSerializer {

	public static void serialize(SerializationStreamWriter streamWriter, BatchRequest instance) throws SerializationException {
		streamWriter.writeString(instance.getService());
		streamWriter.writeString(instance.getMethod());
		assert instance.getTypes().length == instance.getParams().length;
		streamWriter.writeInt(instance.getTypes().length);
		for (String type : instance.getTypes()) {
			streamWriter.writeString(type);
		}
//		streamWriter.writeInt(instance.getParams().length);
		for (Object param : instance.getParams()) {
			streamWriter.writeObject(param);
		}
	}


	public static void deserialize(SerializationStreamReader streamReader,
			BatchRequest instance) throws SerializationException {
		instance.setService(streamReader.readString());
		instance.setMethod(streamReader.readString());
		int len = streamReader.readInt();
		String[] types = new String[len];
		for (int i = 0; i < len; i++) {
			types[i] = streamReader.readString();
		}
		instance.setTypes(types);
//		len = streamReader.readInt();
		Object[] params = new Object[len];
		for (int i = 0; i < len; i++) {
			params[i] = streamReader.readObject();
		}
		instance.setParams(params);
	}
}
