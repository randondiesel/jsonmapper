/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hashvoid.jsonmapper.encode;

import java.util.Map;

import com.hashvoid.jsonmapper.support.JSONObject;

/**
 * @author randondiesel
 *
 */

class MapEncoder {

	private EncoderRegistry encoderReg;

	public MapEncoder(EncoderRegistry reg) {
		encoderReg = reg;
	}

	public JSONObject convert(Map<?, ?> map) {
		JSONObject jsonObj = new JSONObject();
		for(Object key : map.keySet()) {
			if(key instanceof String) {
				Object item = map.get(key);
				if(item != null) {
					Object jsonItem = encoderReg.objectEncoder().convert(item);
					if(jsonItem != null) {
						jsonObj.put((String) key, jsonItem);
					}
				}
			}
		}
		return jsonObj;
	}
}
