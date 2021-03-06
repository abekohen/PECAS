/*
 * Copyright 2005 HBA Specto Incorporated
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/* Generated by Together */

package com.hbaspecto.pecas.aa.technologyChoice;

import com.hbaspecto.pecas.NoAlternativeAvailable;
import com.hbaspecto.pecas.aa.commodity.AbstractCommodity;

/**
 * This class describes amounts of commodities. It is used as an abstract base
 * class for ProductionFunctions and ConsumptionFunctions
 * 
 * @author John Abraham
 */
public interface CommodityQuantities
{
    // protected float[] commodityQuantities = new float[0];
    int size();

    /**
     * sorts internally so that when calcAmounts() or overallUtility() are
     * called with commodity utilities passed in as an array of doubles, the
     * CommodityQuantities object knows which utility vlaue applies to which
     * commodity. This also affects subsequent calls to commodityAt() and must
     * be called before commodityAt() is called.
     */
    void doFinalSetupAndSetCommodityOrder(java.util.List commodityList);

    /**
     * returns the commodity at the position i from the last sort, i.e. the
     * commodity that was at position i in the variable commodityCollection that
     * was passed the last time sortToMatch was called
     * 
     * @associates <{com.pb.models.pecas.AbstractCommodity}>
     */
    AbstractCommodity commodityAt(int i);

    /**
     * This function calculates the amount of each commodity given the utility
     * of each commodity. sortToMatch must have been previously called to
     * establish the order and number of commodities
     * 
     * @param buyingZUtilities
     *            Utility of buying commodities here
     * @param sellingZUtilities
     *            Utility of selling commodities here
     * @param zoneIndex
     *            index of the zone
     * @return an array specifying the amount of each commodity
     * @throws NoAlternativeAvailable
     */
    double[] calcAmounts(double[] buyingZUtilities, double[] sellingZUtilities, int zoneIndex)
            throws NoAlternativeAvailable;

}
