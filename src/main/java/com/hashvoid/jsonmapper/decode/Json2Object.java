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

package com.hashvoid.jsonmapper.decode;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.hashvoid.jsonmapper.support.JSONException;
import com.hashvoid.jsonmapper.support.JSONObject;
import com.hashvoid.jsonmapper.support.JSONTokener;

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

	public <T> T convert(byte[] data, Class<T> type) {
		if(data == null) {
			return null;
		}
		JSONTokener tokener = new JSONTokener(new ByteArrayInputStream(data));
		Object nextVal = tokener.nextValue();
		if(nextVal != null && nextVal instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) nextVal;
			try {
				return reg.objectDecoder().convert(jsonObj, type);
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public <T> List<T> convertAll(byte[] data, Class<T> type) {
		JSONTokener tokener = new JSONTokener(new ByteArrayInputStream(data));
		ArrayList<T> result = new ArrayList<>();
		Object nextVal = null;
		while((nextVal = tokener.nextValue()) != null) {
			if(nextVal instanceof JSONObject) {
				JSONObject jsonObj = (JSONObject) nextVal;
				try {
					T obj = reg.objectDecoder().convert(jsonObj, type);
					if(obj != null) {
						result.add(obj);
					}
				}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
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
