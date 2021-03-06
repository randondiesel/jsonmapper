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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import id.jsonmapper.Converter;
import id.jsonmapper.support.JSONArray;
import id.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

class ListDecoder extends LinearCollectionDecoder {

	private DecoderRegistry decoderReg;

	public ListDecoder(DecoderRegistry reg) {
		super(reg);
		decoderReg = reg;
	}

	public List<?> convertList(JSONArray jsonArr, Type genType, Converter conv) {
		if(!(genType instanceof ParameterizedType)) {
			//System.out.println("not parameterized");
			return null;
		}
		ParameterizedType pmzType = (ParameterizedType) genType;
		Type[] typeArgs = pmzType.getActualTypeArguments();
		if(typeArgs.length != 1) {
			return null;
		}

		if(typeArgs[0] instanceof WildcardType) {
			//System.out.println("parameter must not be wildcard");
			return null;
		}

		Type compGenType = typeArgs[0];

		if(compGenType instanceof Class) {
			ArrayList<Object> result = new ArrayList<>(jsonArr.length());
			for(int i=0; i<jsonArr.length(); i++) {
				result.add(convertOne(jsonArr, i, (Class<?>) compGenType, conv));
			}
			return result;
		}
		else if(compGenType instanceof ParameterizedType) {
			ArrayList<Object> result = new ArrayList<>(jsonArr.length());
			ParameterizedType cpmzType = (ParameterizedType) compGenType;
			Type rawType = cpmzType.getRawType();
			if(rawType.equals(List.class)) {
				for(int i=0; i<jsonArr.length(); i++) {
					JSONArray subArr = jsonArr.getJSONArray(i);
					result.add(convertList(subArr, compGenType, null));
				}
			}
			else if(rawType.equals(Set.class)) {
				for(int i=0; i<jsonArr.length(); i++) {
					JSONArray subArr = jsonArr.getJSONArray(i);
					result.add(convertSet(subArr, compGenType, null));
				}
			}
			else if(rawType.equals(Map.class)) {
				for(int i=0; i<jsonArr.length(); i++) {
					JSONObject subJson = jsonArr.getJSONObject(i);
					Object item = decoderReg.mapDecoder().convert(subJson, compGenType, null);
					result.add(item);
				}
			}
			return result;
		}
		return null;
	}

	public Set<?> convertSet(JSONArray jsonArr, Type genType, Converter conv) {
		List<?> theList = convertList(jsonArr, genType, conv);
		if(theList == null) {
			return null;
		}
		return new HashSet<Object>(theList);
	}
}
