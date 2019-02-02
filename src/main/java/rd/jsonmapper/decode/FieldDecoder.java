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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.JSON;
import rd.jsonmapper.Raw;
import rd.jsonmapper.support.JSONArray;
import rd.jsonmapper.support.JSONException;
import rd.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

class FieldDecoder {

	private DecoderRegistry decoderReg;

	public FieldDecoder(DecoderRegistry reg) {
		decoderReg = reg;
	}

	public void convert(JSONObject parentJson, Object parentObj, Field field)
			throws ReflectiveOperationException, JSONException {

		if(convertRaw(parentJson, parentObj, field)) {
			return;
		}

		if(convertPrimitive(parentJson, parentObj, field)) {
			return;
		}

		JSON ann = field.getAnnotation(JSON.class);

		if(field.getType().equals(String.class)) {
			String fieldVal = parentJson.getString(ann.value());
			populate(parentObj, field, fieldVal);
			return;
		}

		if(field.getType().equals(BigDecimal.class)) {
			BigDecimal fieldVal = parentJson.getBigDecimal(ann.value());
			populate(parentObj, field, fieldVal);
			return;
		}

		if(field.getType().equals(BigInteger.class)) {
			BigInteger fieldVal = parentJson.getBigInteger(ann.value());
			populate(parentObj, field, fieldVal);
			return;
		}

		if(field.getType().isArray()) {
			convertArray(parentJson, parentObj, field);
			return;
		}

		if(field.getType().equals(List.class)) {
			JSONArray arr = parentJson.getJSONArray(ann.value());
			List<?> values = decoderReg.listDecoder().convertList(arr, field.getGenericType());
			populate(parentObj, field, values);
			return;
		}

		if(field.getType().equals(Set.class)) {
			JSONArray arr = parentJson.getJSONArray(ann.value());
			Set<?> values = decoderReg.listDecoder().convertSet(arr, field.getGenericType());
			populate(parentObj, field, values);
			return;
		}

		if(field.getType().equals(Map.class)) {
			JSONObject json = parentJson.getJSONObject(ann.value());
			Map<?, ?> value = decoderReg.mapDecoder().convert(json, field.getGenericType());
			populate(parentObj, field, value);
			return;
		}

		//map it as a regular object
		if(!field.getType().isInterface()
				&& !Modifier.isAbstract(field.getType().getModifiers())) {
			JSONObject fldJson = parentJson.getJSONObject(ann.value());
			Object objVal = decoderReg.objectDecoder().convert(fldJson, field.getType());
			populate(parentObj, field, objVal);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private boolean convertRaw(JSONObject parentJson, Object parentObj, Field field)
			throws ReflectiveOperationException, JSONException {

		Raw rann = field.getAnnotation(Raw.class);
		if(rann == null) {
			return false;
		}

		JSON ann = field.getAnnotation(JSON.class);
		Class<?> fldType = field.getType();
		if(fldType.equals(String.class)) {
			String fieldVal = parentJson.getJSONObject(ann.value()).toString();
			populate(parentObj, field, fieldVal);
			return true;
		}
		else if(fldType.equals(byte[].class)) {
			String fieldVal = parentJson.getJSONObject(ann.value()).toString();
			populate(parentObj, field, fieldVal.getBytes());
			return true;
		}
		return false;
	}

	private boolean convertPrimitive(JSONObject parentJson, Object parentObj, Field field)
			throws ReflectiveOperationException, JSONException {
		boolean flag = false;
		JSON ann = field.getAnnotation(JSON.class);
		Object fieldVal = null;
		Class<?> fldType = field.getType();

		if(fldType.equals(Boolean.TYPE) || fldType.equals(Boolean.class)) {
			fieldVal = parentJson.getBoolean(ann.value());
			flag = true;
		}
		else if(fldType.equals(Byte.TYPE) || fldType.equals(Byte.class)) {
			fieldVal = Byte.decode(parentJson.getString(ann.value()));
			flag = true;
		}
		else if(fldType.equals(Character.TYPE) || fldType.equals(Character.class)) {
			String value = parentJson.getString(ann.value());
			if(value.length() == 1) {
				fieldVal = value.charAt(0);
			}
			flag = true;
		}
		else if(fldType.equals(Short.TYPE) || fldType.equals(Short.class)) {
			Double value = parentJson.getDouble(ann.value());
			fieldVal = value.shortValue();
			flag = true;
		}
		else if(fldType.equals(Integer.TYPE) || fldType.equals(Integer.class)) {
			Double value = parentJson.getDouble(ann.value());
			fieldVal = value.intValue();
			flag = true;
		}
		else if(fldType.equals(Long.TYPE) || fldType.equals(Long.class)) {
			fieldVal = parentJson.getLong(ann.value());
			flag = true;
		}
		else if(fldType.equals(Float.TYPE) || fldType.equals(Float.class)) {
			Double value = parentJson.getDouble(ann.value());
			fieldVal = value.floatValue();
			flag = true;
		}
		else if(fldType.equals(Double.TYPE) || fldType.equals(Double.class)) {
			fieldVal = parentJson.getDouble(ann.value());
			flag = true;
		}

		if(fieldVal != null) {
			populate(parentObj, field, fieldVal);
		}
		return flag;
	}

	private void convertArray(JSONObject parentJson, Object parentObj, Field field)
				throws ReflectiveOperationException {
		JSON ann = field.getAnnotation(JSON.class);
		JSONArray jsonArr = parentJson.getJSONArray(ann.value());
		Object[] values = decoderReg.arrayDecoder().convert(jsonArr, field.getGenericType());
		if(values != null) {
			Object arrVal = Array.newInstance(field.getType().getComponentType(), values.length);
			for(int j=0; j < values.length; j++) {
				Array.set(arrVal, j, values[j]);
			}
			populate(parentObj, field, arrVal);
		}
	}

	private void populate(Object parentObj, Field field, Object fldVal)
			throws IllegalArgumentException, IllegalAccessException {
		boolean accessible = field.isAccessible();
		if(!accessible) {
			field.setAccessible(true);
		}
		field.set(parentObj, fldVal);
		if(!accessible) {
			field.setAccessible(false);
		}
	}
}
