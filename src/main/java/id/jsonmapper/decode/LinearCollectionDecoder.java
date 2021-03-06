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

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import id.jsonmapper.Converter;
import id.jsonmapper.support.JSONArray;

/**
 * @author indroneel
 *
 */

abstract class LinearCollectionDecoder {

	private DecoderRegistry decoderReg;

	protected LinearCollectionDecoder(DecoderRegistry reg) {
		decoderReg = reg;
	}

	protected final Object convertOne(JSONArray jsonArr, int position,
			Class<?> compType, Converter conv) {

		if(compType.isArray()) {
			return decoderReg.arrayDecoder().convert(jsonArr.getJSONArray(position),
				compType, conv);
		}

		if(conv != null) {
			return conv.json2Object(jsonArr.getJSONObject(position), compType);
		}

		if(compType.equals(Boolean.TYPE) || compType.equals(Boolean.class)) {
			return jsonArr.getBoolean(position);
		}

		if(compType.equals(Byte.TYPE) || compType.equals(Byte.class)) {
			String value = jsonArr.getString(position);
			return Byte.decode(value);
		}

		if(compType.equals(Character.TYPE) || compType.equals(Character.class)) {
			String value = jsonArr.getString(position);
			if(value.length() == 1) {
				return value.charAt(0);
			}
		}

		if(compType.equals(Short.TYPE) || compType.equals(Short.class)) {
			Double value = jsonArr.getDouble(position);
			return value.shortValue();
		}

		if(compType.equals(Integer.TYPE) || compType.equals(Integer.class)) {
			Double value = jsonArr.getDouble(position);
			return value.intValue();
		}

		if(compType.equals(Long.TYPE) || compType.equals(Long.class)) {
			return jsonArr.getLong(position);
		}

		if(compType.equals(Float.TYPE) || compType.equals(Float.class)) {
			Double value = jsonArr.getDouble(position);
			return value.floatValue();
		}

		if(compType.equals(Double.TYPE) || compType.equals(Double.class)) {
			return jsonArr.getDouble(position);
		}

		if(compType.equals(String.class)) {
			return jsonArr.getString(position);
		}

		if(compType.equals(BigDecimal.class)) {
			return jsonArr.getBigDecimal(position);
		}

		if(compType.equals(BigInteger.class)) {
			return jsonArr.getBigInteger(position);
		}

		if(!compType.isInterface() && !Modifier.isAbstract(compType.getModifiers())) {
			return decoderReg.objectDecoder().convert(jsonArr.getJSONObject(position), compType);
		}
		return null;
	}
}
