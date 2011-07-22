/**
 *  Copyright (c) 2011 NeuStar, Inc.
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NeuStar, the Neustar logo and related names and logos are registered
 *  trademarks, service marks or tradenames of NeuStar, Inc. All other
 *  product names, company names, marks, logos and symbols may be trademarks
 *  of their respective owners.
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
