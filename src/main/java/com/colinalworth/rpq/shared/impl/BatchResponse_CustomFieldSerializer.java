package com.colinalworth.rpq.shared.impl;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class BatchResponse_CustomFieldSerializer {
	public static void serialize(SerializationStreamWriter streamWriter, BatchResponse instance) throws SerializationException {
		streamWriter.writeObject(instance.getResponse());
		streamWriter.writeObject(instance.getCaught());
	}


	public static void deserialize(SerializationStreamReader streamReader, BatchResponse instance) throws SerializationException {
		instance.setResponse(streamReader.readObject());
		instance.setCaught((Throwable) streamReader.readObject());
	}
}
