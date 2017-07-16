/*
 * Copyright 2013 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.api.service;

import org.agorava.api.exception.ResponseException;
import org.agorava.api.rest.Response;

import java.io.Serializable;

/**
 * Manage JSON Serialization / De-serialization to and from Object
 *
 * @author Antoine Sabot-Durand
 */
public interface JsonMapperService extends Serializable {

    /**
     * Parse the content of the provided {@link org.agorava.api.rest.Response} to de-serialize to an object of the given
     * Class
     *
     * @param resp  the response to de-serialize
     * @param clazz the target class of the object
     * @return an object of the given Class with fields coming from the response
     * @throws org.agorava.api.exception.ResponseException if provided response is not valid
     */
    <T> T mapToObject(Response resp, Class<T> clazz) throws ResponseException;

    /**
     * Transforms an object to a JSON string
     *
     * @param object : the object to transform
     * @return a String representing the object in JSON format
     */
    String objectToJsonString(Object object);

}
