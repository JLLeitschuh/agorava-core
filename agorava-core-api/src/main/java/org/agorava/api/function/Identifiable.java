/*
 * Copyright 2013-2016 Agorava
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

package org.agorava.api.function;

import java.io.Serializable;

/**
 * Provides String id to implementations.
 * <p>This is a <a href="http://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html#package.description">functional interface</a>
 * whose functional method is {@link #getId()}.
 *
 * @author Antoine Sabot-Durand
 * @author Werner Keil
 */
public interface Identifiable extends Serializable {

    /**
     * @return a unique id
     */
    String getId();
}
