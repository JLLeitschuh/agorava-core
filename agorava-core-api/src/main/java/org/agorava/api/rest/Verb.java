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

package org.agorava.api.rest;

/**
 * An enumeration containing HTTP Verbs.
 *
 * @author Pablo Fernandez
 * @author Antoine Sabot-Durand
 */
public enum Verb {
    /**
     * get verb
     */
    GET,

    /**
     * post verb
     */
    POST,

    /**
     * put verb
     */
    PUT,

    /**
     * delete verb
     */
    DELETE,

    /**
     * head verb
     */
    HEAD,

    /**
     * options verb
     */
    OPTIONS,

    /**
     * trace verb
     */
    TRACE,

    /**
     * patch verb
     */
    PATCH
}
