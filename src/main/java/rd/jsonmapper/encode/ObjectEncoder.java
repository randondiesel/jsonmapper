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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import rd.jsonmapper.JSON;
import rd.jsonmapper.support.JSONException;
import rd.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

class ObjectEncoder {

	private EncoderRegistry encoderReg;

	public ObjectEncoder(EncoderRegistry reg) {
		encoderReg = reg;
	}

	public JSONObject convert(Object obj) {

		JSONObject jsonObj = new JSONObject();
		List<Field> allFields = new LinkedList<>();
		collateAllFields(obj.getClass(), allFields);
		for(Field field : allFields) {
			try {
				encoderReg.fieldEncoder().convert(obj, field, jsonObj);
			}
			catch(ReflectiveOperationException | JSONException exep) {
				exep.printStackTrace();
			}
		}
		return jsonObj;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void collateAllFields(Class<?> type, List<Field> fieldList) {
		boolean traverse = type.getClassLoader() != null;
		for(Field field : type.getDeclaredFields()) {
			if(!Modifier.isStatic(field.getModifiers())) {
				JSON ann = field.getAnnotation(JSON.class);
				if(ann != null && ann.value() != null) {
					fieldList.add(field);
				}
			}
		}
		if(type.getSuperclass() != null && traverse) {
			collateAllFields(type.getSuperclass(), fieldList);
		}
	}
}
