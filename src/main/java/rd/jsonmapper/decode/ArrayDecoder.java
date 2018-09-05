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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.support.JSONArray;
import rd.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

class ArrayDecoder extends LinearCollectionDecoder {

	private DecoderRegistry decoderReg;

	public ArrayDecoder(DecoderRegistry reg) {
		super(reg);
		decoderReg = reg;
	}

	public Object[] convert(JSONArray jsonArr, Type genType) {
		if (genType instanceof GenericArrayType) {
			GenericArrayType genArrType = (GenericArrayType) genType;
			ArrayList<?> valueList = convertGenericArrayType(jsonArr, genArrType);
			return valueList.toArray();
		}
		else if (genType instanceof Class) {
			ArrayList<?> valueList = convertClassType(jsonArr, (Class<?>) genType);
			return valueList.toArray();
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private ArrayList<?> convertGenericArrayType(JSONArray jsonArr, GenericArrayType genArrType) {
		Type compGenType = genArrType.getGenericComponentType();
		if(!(compGenType instanceof ParameterizedType)) {
			return null;
		}

		ArrayList<Object> result = new ArrayList<>();
		ParameterizedType cpmzType = (ParameterizedType) compGenType;
		Type rawType = cpmzType.getRawType();
		if(rawType.equals(List.class)) {
			for(int i=0; i<jsonArr.length(); i++) {
				JSONArray subArr = jsonArr.getJSONArray(i);
				Object item = decoderReg.listDecoder().convertList(subArr, compGenType);
				result.add(item);
			}
		}
		else if(rawType.equals(Set.class)) {
			for(int i=0; i<jsonArr.length(); i++) {
				JSONArray subArr = jsonArr.getJSONArray(i);
				Object item = decoderReg.listDecoder().convertSet(subArr, compGenType);
				result.add(item);
			}
		}
		else if(rawType.equals(Map.class)) {
			for(int i=0; i<jsonArr.length(); i++) {
				JSONObject subJson = jsonArr.getJSONObject(i);
				Object item = decoderReg.mapDecoder().convert(subJson, compGenType);
				result.add(item);
			}
		}
		return result;
	}

	private ArrayList<?> convertClassType(JSONArray jsonArr, Class<?> clsType) {
		Class<?> compType = clsType.getComponentType();
		ArrayList<Object> valueList = new ArrayList<>();
		for(int i=0; i<jsonArr.length(); i++) {
			valueList.add(convertOne(jsonArr, i, compType));
		}
		return valueList;
	}
}
