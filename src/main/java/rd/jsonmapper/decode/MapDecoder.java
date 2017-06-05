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

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.support.JSONArray;
import rd.jsonmapper.support.JSONObject;

/**
 * @author randondiesel
 *
 */

class MapDecoder {

	private DecoderRegistry decoderReg;

	public MapDecoder(DecoderRegistry reg) {
		decoderReg = reg;
	}

	public Map<?, ?> convert(JSONObject jsonObj, Type genType) {
		if(!(genType instanceof ParameterizedType)) {
			System.out.println("not parameterized");
			return null;
		}
		ParameterizedType pmzType = (ParameterizedType) genType;
		Type[] typeArgs = pmzType.getActualTypeArguments();
		if(typeArgs.length != 2) {
			return null;
		}

		if(!typeArgs[0].equals(String.class)) {
			System.out.println("first parameter must be string");
			return null;
		}

		if(typeArgs[1] instanceof WildcardType) {
			System.out.println("second parameter must not be wildcard");
			return null;
		}

		Type compGenType = typeArgs[1];
		if(compGenType instanceof Class) {
			HashMap<String, Object> result = new HashMap<>();
			for(String key : jsonObj.keySet()) {
				Object value = convertOne(jsonObj, key, (Class<?>) compGenType);
				result.put(key, value);
			}
			return result;
		}
		else if(compGenType instanceof ParameterizedType) {
			HashMap<String, Object> result = new HashMap<>();
			ParameterizedType cpmzType = (ParameterizedType) compGenType;
			Type rawType = cpmzType.getRawType();
			for(String key : jsonObj.keySet()) {
				if(rawType.equals(List.class)) {
					JSONArray subArr = jsonObj.getJSONArray(key);
					Object value = decoderReg.listDecoder().convertList(subArr, compGenType);
					result.put(key, value);
				}
				if(rawType.equals(Set.class)) {
					JSONArray subArr = jsonObj.getJSONArray(key);
					Object value = decoderReg.listDecoder().convertSet(subArr, compGenType);
					result.put(key, value);
				}
				if(rawType.equals(Map.class)) {
					JSONObject subJson = jsonObj.getJSONObject(key);
					Object value = convert(subJson, compGenType);
					result.put(key, value);
				}
			}
			return result;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private Object convertOne(JSONObject json, String key, Class<?> compType) {

		if(compType.equals(Boolean.TYPE) || compType.equals(Boolean.class)) {
			return json.getBoolean(key);
		}

		if(compType.equals(Byte.TYPE) || compType.equals(Byte.class)) {
			String value = json.getString(key);
			return Byte.decode(value);
		}

		if(compType.equals(Character.TYPE) || compType.equals(Character.class)) {
			String value = json.getString(key);
			if(value.length() == 1) {
				return value.charAt(0);
			}
		}

		if(compType.equals(Short.TYPE) || compType.equals(Short.class)) {
			Integer value = json.getInt(key);
			return value.shortValue();
		}

		if(compType.equals(Integer.TYPE) || compType.equals(Integer.class)) {
			return json.getInt(key);
		}

		if(compType.equals(Long.TYPE) || compType.equals(Long.class)) {
			return json.getLong(key);
		}

		if(compType.equals(Float.TYPE) || compType.equals(Float.class)) {
			Double value = json.getDouble(key);
			return value.floatValue();
		}

		if(compType.equals(Double.TYPE) || compType.equals(Double.class)) {
			return json.getDouble(key);
		}

		if(compType.equals(Number.class)) {
			return json.getDouble(key);
		}

		if(compType.equals(String.class)) {
			return json.getString(key);
		}

		if(compType.isArray()) {
			return decoderReg.arrayDecoder().convert(json.getJSONArray(key), compType);
		}

		if(!compType.isInterface() && !Modifier.isAbstract(compType.getModifiers())) {
			return decoderReg.objectDecoder().convert(json.getJSONObject(key), compType);
		}

		return null;
	}
}
