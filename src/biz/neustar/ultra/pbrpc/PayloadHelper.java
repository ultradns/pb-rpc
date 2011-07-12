/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.zip.CRC32;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcPayload;

public class PayloadHelper {

	public boolean verify(RpcPayload payload) {
		if (payload.hasCrc()) {
			CRC32 crc = new CRC32();
			crc.update(payload.getData().toByteArray());
			return payload.getCrc() == crc.getValue();
		}
		return true;
	}

	public void setCrc(RpcPayload.Builder payload) {
		CRC32 crc = new CRC32();
		crc.update(payload.getData().toByteArray());
		payload.setCrc(crc.getValue());
	}
}
