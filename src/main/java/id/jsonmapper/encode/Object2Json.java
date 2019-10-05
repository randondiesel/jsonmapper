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

package id.jsonmapper.encode;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import id.jsonmapper.support.JSONArray;
import id.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

public class Object2Json {

	private EncoderRegistryImpl reg;

	public Object2Json() {
		reg = new EncoderRegistryImpl();
		reg.objEncoder = new ObjectEncoder(reg);
		reg.fldEncoder = new FieldEncoder(reg);
		reg.arrEncoder = new ArrayEncoder(reg);
		reg.mapEncoder = new MapEncoder(reg);
	}

	public byte[] convert(Object obj) {
		return convert(obj, false);
	}

	public byte[] convert(Object obj, boolean pretty) {
		if(obj instanceof Collection) {
			JSONArray jsonArr = null;
			if(obj instanceof List) {
				jsonArr = reg.arrayEncoder().convertList((List<?>) obj);
			}
			else if(obj instanceof Set) {
				jsonArr = reg.arrayEncoder().convertSet((Set<?>) obj);
			}
			else if(obj.getClass().isArray()) {
				jsonArr = reg.arrayEncoder().convertArray((Object[]) obj);
			}
			if(jsonArr != null) {
				return jsonArr.toString(2).getBytes();
			}
		}
		else {
			JSONObject jsonObj = reg.objectEncoder().convert(obj);
			if(jsonObj != null) {
				if(pretty) {
					return jsonObj.toString(2).getBytes();
				}
				else {
					return jsonObj.toString().getBytes();
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner class that represents a EncoderRegistry

	private class EncoderRegistryImpl implements EncoderRegistry {

		private ObjectEncoder objEncoder;
		private FieldEncoder  fldEncoder;
		private ArrayEncoder  arrEncoder;
		private MapEncoder    mapEncoder;

		@Override
		public ObjectEncoder objectEncoder() {
			return objEncoder;
		}

		@Override
		public FieldEncoder fieldEncoder() {
			return fldEncoder;
		}

		@Override
		public ArrayEncoder arrayEncoder() {
			return arrEncoder;
		}

		@Override
		public MapEncoder mapEncoder() {
			return mapEncoder;
		}
	}
}
