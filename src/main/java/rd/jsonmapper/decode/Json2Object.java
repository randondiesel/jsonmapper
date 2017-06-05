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

package rd.jsonmapper.decode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.support.JSONArray;
import rd.jsonmapper.support.JSONObject;
import rd.jsonmapper.support.JSONTokener;

/**
 * @author randondiesel
 *
 */

public class Json2Object {

	private DecoderRegistryImpl reg;

	public Json2Object() {
		reg = new DecoderRegistryImpl();
		reg.objDecoder = new ObjectDecoder(reg);
		reg.fldDecoder = new FieldDecoder(reg);
		reg.listDecoder = new ListDecoder(reg);
		reg.arrDecoder = new ArrayDecoder(reg);
		reg.mapDecoder = new MapDecoder(reg);
	}

	public Object convert(InputStream in, Type type) throws IOException {
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
		return convert(buffer, type);
	}

	public Object convert(byte[] data, Type type) {
		if(data == null) {
			return null;
		}
		JSONTokener tokener = new JSONTokener(new ByteArrayInputStream(data));
		Object nextVal = tokener.nextValue();
		if(nextVal == null) {
			return null;
		}

		if(type instanceof Class) {
			Class<?> ctype = (Class<?>) type;
			if(ctype.isArray() && nextVal instanceof JSONArray) {
				Object[] values = reg.arrayDecoder().convert((JSONArray) nextVal, ctype);
				if(values != null) {
					Object arrVal = Array.newInstance(ctype.getComponentType(), values.length);
					for(int i=0; i < values.length; i++) {
						Array.set(arrVal, i, values[i]);
					}
					return arrVal;
				}
				return null;
			}
			else if(!ctype.isArray() && nextVal instanceof JSONObject) {
				return reg.objectDecoder().convert((JSONObject) nextVal, ctype);
			}
		}
		else if(type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Type rtype = ptype.getRawType();
			if(nextVal instanceof JSONArray) {
				if(rtype.equals(List.class)) {
					return reg.listDecoder.convertList((JSONArray) nextVal, type);
				}
				if(rtype.equals(Set.class)) {
					return reg.listDecoder.convertSet((JSONArray) nextVal, type);
				}
			}
			else if(nextVal instanceof JSONObject) {
				if(rtype.equals(Map.class)) {
					return reg.mapDecoder.convert((JSONObject) nextVal, type);
				}
			}
		}
		return null;
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
