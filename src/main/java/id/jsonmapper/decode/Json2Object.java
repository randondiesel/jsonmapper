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

package id.jsonmapper.decode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import id.jsonmapper.support.JSONArray;
import id.jsonmapper.support.JSONObject;
import id.jsonmapper.support.JSONTokener;

/**
 * @author indroneel
 *
 */

public class Json2Object {

	private DecoderRegistryImpl reg;
	private JSONTokener         tokener;
	private Object              jsonRoot;

	public Json2Object(InputStream in) throws IOException {
		byte[] buffer = new byte[0];
		byte[] chunk = new byte[1024];
		int amt = 0;
		while((amt = in.read(chunk)) >= 0) {
			if(amt == 0) {
				continue;
			}
			byte[] tempbuf = new byte[buffer.length + amt];
			System.arraycopy(buffer, 0, tempbuf, 0, buffer.length);
			System.arraycopy(chunk, 0, tempbuf, buffer.length, amt);
			buffer = tempbuf;
		}
		in.close();
		prepare(buffer);
	}

	public Json2Object(byte[] data) {
		prepare(data);
	}

	public Object convert(Type type) {
		if(jsonRoot == null) {
			return null;
		}

		if(type instanceof Class) {
			Class<?> ctype = (Class<?>) type;
			if(ctype.isArray() && jsonRoot instanceof JSONArray) {
				Object[] values = reg.arrayDecoder().convert((JSONArray) jsonRoot, ctype);
				if(values != null) {
					Object arrVal = Array.newInstance(ctype.getComponentType(), values.length);
					for(int i=0; i < values.length; i++) {
						Array.set(arrVal, i, values[i]);
					}
					return arrVal;
				}
				return null;
			}
			else if(!ctype.isArray() && jsonRoot instanceof JSONObject) {
				return reg.objectDecoder().convert((JSONObject) jsonRoot, ctype);
			}
		}
		else if(type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Type rtype = ptype.getRawType();
			if(jsonRoot instanceof JSONArray) {
				if(rtype.equals(List.class)) {
					return reg.listDecoder.convertList((JSONArray) jsonRoot, type);
				}
				if(rtype.equals(Set.class)) {
					return reg.listDecoder.convertSet((JSONArray) jsonRoot, type);
				}
			}
			else if(jsonRoot instanceof JSONObject) {
				if(rtype.equals(Map.class)) {
					return reg.mapDecoder.convert((JSONObject) jsonRoot, type);
				}
			}
		}
		return null;
	}

	public Object convert(String path, Type type) {
		if(jsonRoot == null || !(jsonRoot instanceof JSONObject)) {
			return null;
		}
		JSONObject nowVal = (JSONObject) jsonRoot;

		String[] pathElements = path.split("\\.");
		for(int i = 0; i < (pathElements.length - 1); i++) {
			try {
				nowVal = nowVal.getJSONObject(pathElements[i]);
			}
			catch(Exception exep) {
				return null;
			}
		}
		String name = pathElements[pathElements.length - 1];

		if(type instanceof Class) {
			Class<?> ctype = (Class<?>) type;
			if(ctype.equals(Boolean.TYPE) || ctype.equals(Boolean.class)) {
				try {
					return nowVal.getBoolean(name);
				}
				catch(Exception exep) {
					return ctype.equals(Boolean.TYPE) ? false : null;
				}
			}
			if(ctype.equals(Byte.TYPE) || ctype.equals(Byte.class)) {
				try {
					return Byte.decode(nowVal.getString(name));
				}
				catch(Exception exep) {
					return ctype.equals(Byte.TYPE) ? (byte) 0 : null;
				}
			}
			if(ctype.equals(Character.TYPE) || ctype.equals(Character.class)) {
				try {
					return nowVal.getString(name).charAt(0);
				}
				catch(Exception exep) {
					return ctype.equals(Character.TYPE) ? (char) 0 : null;
				}
			}
			if(ctype.equals(Short.TYPE) || ctype.equals(Short.class)) {
				try {
					Double value = nowVal.getDouble(name);
					return value.shortValue();
				}
				catch(Exception exep) {
					return ctype.equals(Short.TYPE) ? (short) 0 : null;
				}
			}
			if(ctype.equals(Integer.TYPE) || ctype.equals(Integer.class)) {
				try {
					Double value = nowVal.getDouble(name);
					return value.intValue();
				}
				catch(Exception exep) {
					return ctype.equals(Integer.TYPE) ? 0 : null;
				}
			}
			if(ctype.equals(Long.TYPE) || ctype.equals(Long.class)) {
				try {
					return nowVal.getLong(name);
				}
				catch(Exception exep) {
					return ctype.equals(Long.TYPE) ? (long) 0 : null;
				}
			}
			if(ctype.equals(Float.TYPE) || ctype.equals(Float.class)) {
				try {
					Double value = nowVal.getDouble(name);
					return value.floatValue();
				}
				catch(Exception exep) {
					return ctype.equals(Float.TYPE) ? (float) 0 : null;
				}
			}
			if(ctype.equals(Double.TYPE) || ctype.equals(Double.class)) {
				try {
					return nowVal.getDouble(name);
				}
				catch(Exception exep) {
					return ctype.equals(Double.TYPE) ? (double) 0 : null;
				}
			}
			//end primitives
			if(ctype.equals(String.class)) {
				try {
					return nowVal.getString(name);
				}
				catch(Exception exep) {
					return null;
				}
			}
			if(ctype.equals(BigInteger.class)) {
				try {
					return nowVal.getBigInteger(name);
				}
				catch(Exception exep) {
					return null;
				}
			}
			if(ctype.equals(BigDecimal.class)) {
				try {
					return nowVal.getBigDecimal(name);
				}
				catch(Exception exep) {
					return null;
				}
			}
			//then the rest
			if(ctype.isArray()) {
				try {
					JSONArray jarr = nowVal.getJSONArray(name);
					Object[] values = reg.arrayDecoder().convert(jarr, ctype);
					if(values != null) {
						Object arrVal = Array.newInstance(ctype.getComponentType(), values.length);
						for(int i=0; i < values.length; i++) {
							Array.set(arrVal, i, values[i]);
						}
						return arrVal;
					}
				}
				catch(Exception exep) {}
				return null;
			}
			//finally
			try {
				JSONObject jobj = nowVal.getJSONObject(name);
				return reg.objectDecoder().convert(jobj, ctype);
			}
			catch(Exception exep) {}
			return null;
		}

		if(type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Type rtype = ptype.getRawType();
			try {
				Object lastVal = nowVal.get(name);
				if(lastVal instanceof JSONArray) {
					if(rtype.equals(List.class)) {
						return reg.listDecoder.convertList((JSONArray) lastVal, type);
					}
					if(rtype.equals(Set.class)) {
						return reg.listDecoder.convertSet((JSONArray) lastVal, type);
					}
				}
				else if(lastVal instanceof JSONObject) {
					if(rtype.equals(Map.class)) {
						return reg.mapDecoder.convert((JSONObject) lastVal, type);
					}
				}
			}
			catch(Exception exep) {}
			return null;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void prepare(byte[] data) {
		reg = new DecoderRegistryImpl();
		reg.objDecoder = new ObjectDecoder(reg);
		reg.fldDecoder = new FieldDecoder(reg);
		reg.listDecoder = new ListDecoder(reg);
		reg.arrDecoder = new ArrayDecoder(reg);
		reg.mapDecoder = new MapDecoder(reg);

		if(data == null) {
			return;
		}
		tokener = new JSONTokener(new ByteArrayInputStream(data));
		jsonRoot = tokener.nextValue();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner class that represents a DecoderRegistry

	private class DecoderRegistryImpl implements DecoderRegistry {

		private ObjectDecoder objDecoder;
		private FieldDecoder  fldDecoder;
		private ListDecoder   listDecoder;
		private ArrayDecoder  arrDecoder;
		private MapDecoder    mapDecoder;

		@Override
		public ObjectDecoder objectDecoder() {
			return objDecoder;
		}

		@Override
		public FieldDecoder fieldDecoder() {
			return fldDecoder;
		}

		@Override
		public ListDecoder listDecoder() {
			return listDecoder;
		}

		@Override
		public ArrayDecoder arrayDecoder() {
			return arrDecoder;
		}

		@Override
		public MapDecoder mapDecoder() {
			return mapDecoder;
		}
	}
}
