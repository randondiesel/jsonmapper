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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rd.jsonmapper.JSON;
import rd.jsonmapper.support.JSONArray;
import rd.jsonmapper.support.JSONException;
import rd.jsonmapper.support.JSONObject;

/**
 * @author randondiesel
 *
 */

class FieldEncoder {

	private EncoderRegistry encoderReg;

	public FieldEncoder(EncoderRegistry reg) {
		encoderReg = reg;
	}

	public void convert(Object parentObj, Field field, JSONObject jsonObj)
			throws ReflectiveOperationException, JSONException {

		JSON ann = field.getAnnotation(JSON.class);

		if(convertPrimitive(parentObj, field, jsonObj)) {
			return;
		}

		if(field.getType().equals(String.class)) {
			String fieldVal = (String) retrieve(parentObj, field);
			if(fieldVal != null && fieldVal.length() > 0) {
				jsonObj.put(ann.value(), fieldVal);
			}
			return;
		}

		if(field.getType().isArray()) {
			Object[] fieldVals = retrieveArray(parentObj, field);
			if(fieldVals != null && fieldVals.length > 0) {
				JSONArray jsonArr = encoderReg.arrayEncoder().convertArray(fieldVals);
				jsonObj.put(ann.value(), jsonArr);
			}
			return;
		}

		if(List.class.isAssignableFrom(field.getType())) {
			List<?> fieldVal = (List<?>) retrieve(parentObj, field);
			if(fieldVal != null && !fieldVal.isEmpty()) {
				JSONArray jsonArr = encoderReg.arrayEncoder().convertList(fieldVal);
				jsonObj.put(ann.value(), jsonArr);
			}
			return;
		}

		if(Set.class.isAssignableFrom(field.getType())) {
			Set<?> fieldVal = (Set<?>) retrieve(parentObj, field);
			if(fieldVal != null && !fieldVal.isEmpty()) {
				JSONArray jsonArr = encoderReg.arrayEncoder().convertSet(fieldVal);
				jsonObj.put(ann.value(), jsonArr);
			}
			return;
		}

		if(Map.class.isAssignableFrom(field.getType())) {
			Map<?, ?> fieldVal = (Map<?, ?>) retrieve(parentObj, field);
			if(fieldVal != null && !fieldVal.isEmpty()) {
				JSONObject jsonMap = encoderReg.mapEncoder().convert(fieldVal);
				jsonObj.put(ann.value(), jsonMap);
			}
			return;
		}

		Object fieldVal = retrieve(parentObj, field);
		if(fieldVal != null) {
			JSONObject json = encoderReg.objectEncoder().convert(fieldVal);
			jsonObj.put(ann.value(), json);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private boolean convertPrimitive(Object parentObj, Field field, JSONObject jsonObj)
			throws ReflectiveOperationException, JSONException {
		boolean flag = false;
		JSON ann = field.getAnnotation(JSON.class);
		Object fieldVal = null;
		Class<?> fldType = field.getType();
		if(fldType.equals(Boolean.TYPE) || fldType.equals(Boolean.class)) {
			fieldVal = (Boolean) retrieve(parentObj, field);
			flag = true;
		}
		else if(fldType.equals(Byte.TYPE) || fldType.equals(Byte.class)) {
			Byte primVal = (Byte) retrieve(parentObj, field);
			if(primVal != null) {
				fieldVal = new Integer(primVal);
			}
			flag = true;
		}
		else if(fldType.equals(Character.TYPE) || fldType.equals(Character.class)) {
			Character primVal = (Character) retrieve(parentObj, field);
			if(primVal != null) {
				fieldVal = "" + primVal;
			}
			flag = true;
		}
		else if(fldType.equals(Short.TYPE) || fldType.equals(Short.class)) {
			Short primVal = (Short) retrieve(parentObj, field);
			if(primVal != null) {
				fieldVal = new Integer(primVal);
			}
			flag = true;
		}
		else if(fldType.equals(Integer.TYPE) || fldType.equals(Integer.class)) {
			fieldVal = (Integer) retrieve(parentObj, field);
			flag = true;
		}
		else if(fldType.equals(Long.TYPE) || fldType.equals(Long.class)) {
			fieldVal = (Long) retrieve(parentObj, field);
			flag = true;
		}
		else if(fldType.equals(Float.TYPE) || fldType.equals(Float.class)) {
			Float primVal = (Float) retrieve(parentObj, field);
			if(primVal != null) {
				fieldVal = new Double(primVal);
			}
			flag = true;
		}
		else if(fldType.equals(Double.TYPE) || fldType.equals(Double.class)) {
			fieldVal = (Double) retrieve(parentObj, field);
			flag = true;
		}

		if(fieldVal != null) {
			jsonObj.put(ann.value(), fieldVal);
		}
		return flag;
	}

	private Object retrieve(Object parentObj, Field field)
			throws IllegalArgumentException, IllegalAccessException {
		boolean accessible = field.isAccessible();
		if(!accessible) {
			field.setAccessible(true);
		}
		Object fldVal = field.get(parentObj);
		if(!accessible) {
			field.setAccessible(false);
		}
		return fldVal;
	}

	private Object[] retrieveArray(Object parentObj, Field field)
			throws IllegalArgumentException, IllegalAccessException {
		boolean accessible = field.isAccessible();
		if(!accessible) {
			field.setAccessible(true);
		}
		Object fldVal = field.get(parentObj);
		int length = Array.getLength(fldVal);
		Object[] resultArr = new Object[length];
		for(int i=0; i<length; i++) {
			resultArr[i] = Array.get(fldVal, i);
		}
		if(!accessible) {
			field.setAccessible(false);
		}
		return resultArr;
	}
}