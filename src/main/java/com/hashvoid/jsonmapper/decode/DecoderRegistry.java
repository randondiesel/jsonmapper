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

/**
 * Provides a common context for decoders to refer to each other during JSON processing. Allows for
 * decoders to be maintained as singleton entities for better optimizations.
 * <p>
 *
 * @author randondiesel
 */

interface DecoderRegistry {

	ObjectDecoder objectDecoder();

	FieldDecoder fieldDecoder();

	ListDecoder listDecoder();

	ArrayDecoder arrayDecoder();

	MapDecoder mapDecoder();
}
