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
package com.hbaspecto.pecas.sd;

import java.util.Iterator;

//import com.pb.despair.ld.DevelopmentType;

/**
 * A class that represents a the allowed zoning in a Grid Cell
 * 
 * @author John Abraham
 */
public interface ZoningRulesIInterface {

	public abstract String getName();

	public void noLongerAllowDevelopmentType(SpaceTypeInterface dt);

	public double getAllowedFAR(SpaceTypeInterface dt);

	public Iterator getAllowedSpaceTypes();

	public int size();
}