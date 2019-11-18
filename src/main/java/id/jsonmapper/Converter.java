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

package id.jsonmapper;

import java.util.Optional;

import id.jsonmapper.support.JSONObject;

/**
 * @author indroneel
 *
 */

public interface Converter {

	<T> T json2Object(Object jsonObj, Class<T> type);

	Optional<JSONObject> object2Json(Object obj);
}
