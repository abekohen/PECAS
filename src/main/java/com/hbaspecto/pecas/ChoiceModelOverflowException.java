/*
 *  Copyright 2005 HBA Specto Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.hbaspecto.pecas;

/**
 * @author jabraham
 * 
 * 
 */
public class ChoiceModelOverflowException extends OverflowException {

	public ChoiceModelOverflowException() {
		super();
	}

	public ChoiceModelOverflowException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ChoiceModelOverflowException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param string
	 */
	public ChoiceModelOverflowException(String string) {

		super(string);
	}

	public ChoiceModelOverflowException(String string, NoAlternativeAvailable e) {
		super(string, e);
	}

}
