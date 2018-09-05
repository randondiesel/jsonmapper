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

import rd.jsonmapper.support.JSONArray;

/**
 * @author indroneel
 *
 */

class ArrayEncoder {

	private EncoderRegistry encoderReg;

	public ArrayEncoder(EncoderRegistry reg) {
		encoderReg = reg;
	}

	public JSONArray convertArray(Object[] items) {
		JSONArray jsonArr = new JSONArray();
		for(Object item : items) {
			Object jsonItem = convertOne(item);
			if(jsonItem != null) {
				jsonArr.put(jsonItem);
			}
		}
		return jsonArr;
	}

	public JSONArray convertList(List<?> list) {
		return convertIterable(list);
	}

	public JSONArray convertSet(Set<?> set) {
		return convertIterable(set);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private JSONArray convertIterable(Iterable<?> collection) {
		JSONArray jsonArr = new JSONArray();
		for(Object item : collection) {
			Object jsonItem = convertOne(item);
			if(jsonItem != null) {
				jsonArr.put(jsonItem);
			}
		}
		return jsonArr;
	}

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
			return convertList((List<?>) item);
		}
		else if(item instanceof Set) {
			return convertSet((Set<?>) item);
		}
		else if(item instanceof Map) {
			return encoderReg.mapEncoder().convert((Map<?, ?>) item);
		}
		else {
			return encoderReg.objectEncoder().convert(item);
		}
	}
}
