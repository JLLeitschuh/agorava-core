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

package org.agorava.cdi;

import org.agorava.api.atinject.Current;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Antoine Sabot-Durand
 */
public class CurrentLiteral extends AnnotationLiteral<Current> implements Current {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1224603757124841646L;
	public static CurrentLiteral INSTANCE = new CurrentLiteral();
}
