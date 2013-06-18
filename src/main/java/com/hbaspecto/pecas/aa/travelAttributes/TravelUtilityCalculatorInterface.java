/*
 * Copyright  2005 HBA Specto Incorporated
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

package com.hbaspecto.pecas.aa.travelAttributes;

/**
 * A class that represents how the preferences for travel by different modes and
 * different times of day
 * 
 * @author J. Abraham
 */
public interface TravelUtilityCalculatorInterface {
	public double getUtility(int origin, int destination,
			TravelAttributesInterface travelConditions);

	/**
	 * @param fromZoneUserNumber
	 * @param toZoneUserNumber
	 */
	public double[] getUtilityComponents(int fromZoneUserNumber,
			int toZoneUserNumber, TravelAttributesInterface travelConditions);
}
