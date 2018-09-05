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

package rd.jsonmapper.encode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.support.JSONObject;

/**
 * @author indroneel
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
			if(!(key instanceof CharSequence)) {
				continue;
			}
			Object jsonItem = convertOne(map.get(key));
			if(jsonItem != null) {
				jsonObj.put(((CharSequence) key).toString(), jsonItem);
			}
		}
		return jsonObj;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private Object convertOne(Object item) {
		if(item == null) {
			return null;
		}
		Class<?> itemCls = item.getClass();
		if(itemCls.equals(Boolean.TYPE) || itemCls.equals(Boolean.class)) {
			return (Boolean) item;
		}
		else if(itemCls.equals(Byte.TYPE) || itemCls.equals(Byte.class)) {
			return new Integer((Byte) item);
		}
		else if(itemCls.equals(Character.TYPE) || itemCls.equals(Character.class)) {
			return "" + item;
		}
		else if(itemCls.equals(Short.TYPE) || itemCls.equals(Short.class)) {
			return new Integer((short) item);
		}
		else if(itemCls.equals(Integer.TYPE) || itemCls.equals(Integer.class)) {
			return (Integer) item;
		}
		else if(itemCls.equals(Long.TYPE) || itemCls.equals(Long.class)) {
			return (Long) item;
		}
		else if(itemCls.equals(Float.TYPE) || itemCls.equals(Float.class)) {
			return new Double( (float) item);
		}
		else if(itemCls.equals(Double.TYPE) || itemCls.equals(Double.class)) {
			return (Double) item;
		}
		else if(item instanceof CharSequence) {
			return item.toString();
		}
		else if(item instanceof List) {
			return encoderReg.arrayEncoder().convertList((List<?>) item);
		}
		else if(item instanceof Set) {
			return encoderReg.arrayEncoder().convertSet((Set<?>) item);
		}
		else if(item.getClass().isArray()) {
			return encoderReg.arrayEncoder().convertArray((Object[]) item);
		}
		else if(item instanceof Map) {
			return encoderReg.mapEncoder().convert((Map<?, ?>) item);
		}
		else {
			return encoderReg.objectEncoder().convert(item);
		}
	}
}
