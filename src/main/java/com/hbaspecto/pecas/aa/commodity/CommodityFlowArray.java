/*
 * Copyright 2005 HBA Specto Incorporated
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
/* Generated by Together */

package com.hbaspecto.pecas.aa.commodity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import com.hbaspecto.discreteChoiceModelling.AggregateAlternative;
import com.hbaspecto.pecas.ChoiceModelOverflowException;
import com.hbaspecto.pecas.OverflowException;
import com.hbaspecto.pecas.aa.travelAttributes.TimeAndDistanceTravelUtilityCalculator;
import com.hbaspecto.pecas.aa.travelAttributes.TransportKnowledge;
import com.hbaspecto.pecas.aa.travelAttributes.TravelUtilityCalculatorInterface;
import com.hbaspecto.pecas.zones.AbstractZone;

/**
 * This is the amount of commodity moving from a location to an exchange location. Thus the quantity should be a positive number for sellingZUtility
 * (producers of the commodity) and a negative number for buyingZUtility (consumers of the commodity).
 */
public class CommodityFlowArray
        implements AggregateAlternative /* CompositeAlternativeInterface */
{

    private static Logger                  logger                   = Logger.getLogger("com.pb.models.pecas");
    /**
     * Attribute describing the type of flow. theCommodityZUtility has attributes describing the commodity being transferred, whether the commodity is
     * being bought or sold in the exchange zone, and what zone it is being brought in from (for selling) or being shipped out to (for buying).
     */
    public CommodityZUtility               theCommodityZUtility;
    public double                          dispersionParameter      = 1.0;
    final boolean                          timeAndDistanceUtilities;
    final double                           valueOfTime;
    final double                           costOfDistance;
    final TravelUtilityCalculatorInterface travelUtilityCalculatorInterface;
    private double[]                       aggregateQuantityWeights = new double[0];

    /**
     * The market where the flow is going to or coming from. Sellers (sellingZUtility) ship a commodity to a market and sell it there; buyers go to
     * the market, buy commodities, and ship them back. theExchange represents a market at a spatial location.
     */
    // public Exchange[] theExchanges;

    CommodityFlowArray(CommodityZUtility where, TravelUtilityCalculatorInterface tci)
    {
        theCommodityZUtility = where;
        travelUtilityCalculatorInterface = tci;
        if (tci instanceof TimeAndDistanceTravelUtilityCalculator)
        {
            timeAndDistanceUtilities = true;
            final TimeAndDistanceTravelUtilityCalculator tc = (TimeAndDistanceTravelUtilityCalculator) tci;
            valueOfTime = tc.valueOfTime;
            costOfDistance = tc.costOfDistance;
        } else
        {
            timeAndDistanceUtilities = false;
            valueOfTime = 0;
            costOfDistance = 0;
        }

    }

    private double buyingUtilityConsideringPriceSizeAndTransport(double rUtility,
            Exchange theExchange)
    {
        final Commodity c = theCommodityZUtility.myCommodity;
        final double bob = c.getBuyingUtilitySizeCoefficient() * 1 / dispersionParameter
                * Math.log(theExchange.getBuyingSizeTerm()) + c.getBuyingUtilityPriceCoefficient()
                * theExchange.getPrice() + c.getBuyingUtilityTransportCoefficient() * rUtility;
        if (Double.isNaN(bob))
        {
            logger.warn("buying utility is NaN for " + theCommodityZUtility + " to " + theExchange);
            logger.warn("rutility:" + rUtility + "  price:" + theExchange.getPrice() + "  Size:"
                    + theExchange.getBuyingSizeTerm());
        }
        if (Double.isInfinite(bob) && theExchange.getBuyingSizeTerm() != 0)
        {
            if (bob > 0)
            {
                logger.warn("buying utility is infinite for " + theCommodityZUtility + " to "
                        + theExchange);
                logger.warn("rutility:" + rUtility + "  price:" + theExchange.getPrice()
                        + "  Size:" + theExchange.getBuyingSizeTerm());
            } else if (logger.isDebugEnabled())
            {
                logger.debug("buying utility is infinite for " + theCommodityZUtility + " to "
                        + theExchange);
                logger.debug("rutility:" + rUtility + "  price:" + theExchange.getPrice()
                        + "  Size:" + theExchange.getBuyingSizeTerm());
            }
        }
        return bob;
    }

    private double buyingUtilityConsideringPriceAndTransport(double rUtility, Exchange theExchange)
    {
        final Commodity c = theCommodityZUtility.myCommodity;
        final double bob = c.getBuyingUtilityPriceCoefficient() * theExchange.getPrice()
                + c.getBuyingUtilityTransportCoefficient() * rUtility;
        return bob;
    }

    private double[] buyingUtilityComponents(double[] transportComponents, Exchange theExchange)
    {
        final double[] components = new double[transportComponents.length + 2];
        final Commodity c = theCommodityZUtility.myCommodity;
        components[0] = c.getBuyingUtilityPriceCoefficient() * theExchange.getPrice();
        components[1] = c.getBuyingUtilitySizeCoefficient() * 1 / dispersionParameter
                * Math.log(theExchange.getBuyingSizeTerm());
        for (int i = 2; i < components.length; i++)
        {
            components[i] = transportComponents[i - 2] * c.getBuyingUtilityTransportCoefficient();
        }
        return components;
    }

    private double sellingUtilityConsideringPriceAndTransport(double rUtility, Exchange theExchange)
    {
        // calculates SUc,z,k or BUc,z,k which is the utility of selling to or
        // buying from exchange
        // location 'k', a commodity 'c' that was produced or consumed in zone
        // 'z'.
        final Commodity c = theCommodityZUtility.myCommodity;
        // I don't see the dispersion parameter in the equations contained in
        // the documentation for aa.
        // the value is set to 1.0 however so maybe it doesn't make a
        // difference.
        final double bob = c.getSellingUtilityPriceCoefficient() * theExchange.getPrice()
                + c.getSellingUtilityTransportCoefficient() * rUtility;
        return bob;
    }

    private double sellingUtilityConsideringPriceSizeAndTransport(double rUtility,
            Exchange theExchange)
    {
        // calculates SUc,z,k or BUc,z,k which is the utility of selling to or
        // buying from exchange
        // location 'k', a commodity 'c' that was produced or consumed in zone
        // 'z'.
        final Commodity c = theCommodityZUtility.myCommodity;
        // I don't see the dispersion parameter in the equations contained in
        // the documentation for aa.
        // the value is set to 1.0 however so maybe it doesn't make a
        // difference.
        final double bob = c.getSellingUtilitySizeCoefficient() * 1 / dispersionParameter
                * Math.log(theExchange.getSellingSizeTerm())
                + c.getSellingUtilityPriceCoefficient() * theExchange.getPrice()
                + c.getSellingUtilityTransportCoefficient() * rUtility;
        if (Double.isNaN(bob))
        {
            logger.warn("selling utility is NaN for " + theCommodityZUtility + " to exchange "
                    + theExchange);
            logger.warn("rutility:" + rUtility + "  price:" + theExchange.getPrice() + "  Size:"
                    + theExchange.getSellingSizeTerm());
        }
        if (Double.isInfinite(bob) && theExchange.getSellingSizeTerm() != 0)
        {
            if (bob > 0)
            {
                logger.warn("selling utility is infinite for " + theCommodityZUtility
                        + " to exchange " + theExchange);
                logger.warn("rutility:" + rUtility + "  price:" + theExchange.getPrice()
                        + "  Size:" + theExchange.getSellingSizeTerm());
            } else if (logger.isDebugEnabled())
            {
                logger.debug("selling utility is infinite for " + theCommodityZUtility
                        + " to exchange " + theExchange);
                logger.debug("rutility:" + rUtility + "  price:" + theExchange.getPrice()
                        + "  Size:" + theExchange.getSellingSizeTerm());
            }
        }
        return bob;
    }

    private double[] sellingUtilityComponents(double[] transportComponents, Exchange theExchange)
    {
        final double[] components = new double[transportComponents.length + 2];
        final Commodity c = theCommodityZUtility.myCommodity;
        components[0] = c.getSellingUtilityPriceCoefficient() * theExchange.getPrice();
        components[1] = c.getSellingUtilitySizeCoefficient() * 1 / dispersionParameter
                * Math.log(theExchange.getSellingSizeTerm());
        for (int i = 2; i < components.length; i++)
        {
            components[i] = transportComponents[i - 2] * c.getSellingUtilityTransportCoefficient();
        }
        return components;
    }

    public double calcUtilityForExchange(Exchange theExchange)
    {
        if (theCommodityZUtility instanceof SellingZUtility)
        {
            // calculates rUtility = TRANc,z,k which is the utility of
            // transporting commodity 'c' from zone 'z' to
            // exchange zone 'k'. It is the weighted sum of time/cost across all
            // modes
            double rUtility;
            rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    theCommodityZUtility.getTaz(),
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                    theCommodityZUtility.getMyTravelPreferences(),
                    theCommodityZUtility.getUseRouteChoice());
            // TRANc,z,k is needed for the calculation of SUc,z,k which is the
            // utility for selling
            // to exchange zone 'k' a commodity 'c' that was produced in zone
            // 'z' . It is SUc,z,k that is
            // being returned from this method.
            return sellingUtilityConsideringPriceSizeAndTransport(rUtility, theExchange);
        } else
        {
            // calculates rUtility = TRANc,k,z which is the utility of
            // transporting commodity 'c' from exhange zone 'k' to
            // zone 'z'. It is the weighted sum of time/cost across all modes
            double rUtility;
            rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                    theCommodityZUtility.getTaz(), theCommodityZUtility.getMyTravelPreferences(),
                    theCommodityZUtility.getUseRouteChoice());
            // TRANc,k,z is needed for the calculation of BUc,z,k which is the
            // utility for buying
            // from exchange location 'k' a unit of commodity 'c' consumed in
            // zone 'z'. It is BUc,z,k that is being
            // returned from this method.
            return buyingUtilityConsideringPriceSizeAndTransport(rUtility, theExchange);
        }
    }

    public double calcUtilityForExchangeWithoutSize(Exchange theExchange)
    {
        if (theCommodityZUtility instanceof SellingZUtility)
        {
            // calculates rUtility = TRANc,z,k which is the utility of
            // transporting commodity 'c' from zone 'z' to
            // exchange zone 'k'. It is the weighted sum of time/cost across all
            // modes
            double rUtility;
            rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    theCommodityZUtility.getTaz(),
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                    theCommodityZUtility.getMyTravelPreferences(),
                    theCommodityZUtility.getUseRouteChoice());
            // TRANc,z,k is needed for the calculation of SUc,z,k which is the
            // utility for selling
            // to exchange zone 'k' a commodity 'c' that was produced in zone
            // 'z' . It is SUc,z,k that is
            // being returned from this method.
            return sellingUtilityConsideringPriceAndTransport(rUtility, theExchange);
        } else
        {
            // calculates rUtility = TRANc,k,z which is the utility of
            // transporting commodity 'c' from exhange zone 'k' to
            // zone 'z'. It is the weighted sum of time/cost across all modes
            double rUtility;
            rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                    theCommodityZUtility.getTaz(), theCommodityZUtility.getMyTravelPreferences(),
                    theCommodityZUtility.getUseRouteChoice());
            // TRANc,k,z is needed for the calculation of BUc,z,k which is the
            // utility for buying
            // from exchange location 'k' a unit of commodity 'c' consumed in
            // zone 'z'. It is BUc,z,k that is being
            // returned from this method.
            return buyingUtilityConsideringPriceAndTransport(rUtility, theExchange);
        }
    }

    public double[] calcUtilityComponentsForExchange(Exchange theExchange)
    {
        if (theCommodityZUtility instanceof SellingZUtility)
        {
            // calculates rUtility = TRANc,z,k which is the utility of
            // transporting commodity 'c' from zone 'z' to
            // exchange zone 'k'. It is the weighted sum of time/cost across all
            // modes
            final double[] utilityComponentsTemp = TransportKnowledge.globalTransportKnowledge
                    .getUtilityComponents(theCommodityZUtility.getTaz(),
                            AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                            theCommodityZUtility.getMyTravelPreferences(),
                            theCommodityZUtility.getUseRouteChoice());
            return sellingUtilityComponents(utilityComponentsTemp, theExchange);
        } else
        {
            // calculates rUtility = TRANc,k,z which is the utility of
            // transporting commodity 'c' from exhange zone 'k' to
            // zone 'z'. It is the weighted sum of time/cost across all modes
            final double[] utilityComponentsTemp = TransportKnowledge.globalTransportKnowledge
                    .getUtilityComponents(
                            AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                            theCommodityZUtility.getTaz(),
                            theCommodityZUtility.getMyTravelPreferences(),
                            theCommodityZUtility.getUseRouteChoice());
            // TRANc,k,z is needed for the calculation of BUc,z,k which is the
            // utility for buying
            // from exchange location 'k' a unit of commodity 'c' consumed in
            // zone 'z'. It is BUc,z,k that is being
            // returned from this method.
            return buyingUtilityComponents(utilityComponentsTemp, theExchange);
        }
    }

    public double calcUtilityForTravelPreferences(TravelUtilityCalculatorInterface tp,
            Exchange theExchange)
    {
        if (theCommodityZUtility instanceof SellingZUtility)
        {
            final double rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    theCommodityZUtility.getTaz(),
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID), tp,
                    false);
            return sellingUtilityConsideringPriceSizeAndTransport(rUtility, theExchange);
        } else
        {
            final double rUtility = TransportKnowledge.globalTransportKnowledge.getUtility(
                    AbstractZone.findZoneByUserNumber(theExchange.exchangeLocationUserID),
                    theCommodityZUtility.myTaz, tp, false);
            return buyingUtilityConsideringPriceSizeAndTransport(rUtility, theExchange);
        }
    }

    public double[] getUtilityComponents(double higherLevelDispersionParameter)
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        if (com.exchangeType == 'p' && theCommodityZUtility instanceof SellingZUtility)
        {
            final double[] bob = calcUtilityComponentsForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
            final double[] fred = new double[bob.length + 1];
            System.arraycopy(bob, 0, fred, 1, bob.length);

            // expected value of random component is zero when there is only one
            // alternative
            fred[0] = 0;
            return fred;
        }
        if (com.exchangeType == 'c' && theCommodityZUtility instanceof BuyingZUtility)
        {
            final double[] bob = calcUtilityComponentsForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
            final double[] fred = new double[bob.length + 1];
            System.arraycopy(bob, 0, fred, 1, bob.length);
            // expected value of random component is zero when there is only one
            // alternative
            fred[0] = 0;
            return fred;
        }
        if (com.exchangeType == 'n')
        {
            final double[] bob = calcUtilityComponentsForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
            final double[] fred = new double[bob.length + 1];
            System.arraycopy(bob, 0, fred, 1, bob.length);
            // expected value of random component is zero when there is only one
            // alternative
            fred[0] = 0;
            return fred;
        }

        if (com.getAllExchanges().size() == 1)
        {
            final Exchange x = com.getAllExchanges().get(0);
            final double[] bob = calcUtilityComponentsForExchange(x);
            final double[] fred = new double[bob.length + 1];
            System.arraycopy(bob, 0, fred, 1, bob.length);
            fred[0] = 0;
            return fred;
        }

        // For all other Commodity exchange types, calculate CUSellc,z =
        // composite utility of selling
        // commodity 'c' in zone 'z' independent of producing activity.
        // CUSellc,z = (1/lambda)ln(sum(e^(lambda* SUc,z,k)over k)).
        Iterator it = com.getAllExchanges().iterator();
        double sum = 0;
        double[] components = null;
        while (it.hasNext())
        {
            final Exchange k = (Exchange) it.next();
            final double bob = calcUtilityForExchange(k); // returns SUc,z,k for
            // each k
            sum += Math.exp(dispersionParameter * bob); // summing over k
        }

        final double logsum = 1 / dispersionParameter * Math.log(sum); // calculating
        // the
        // logsum
        // =
        // CUSellc,z

        it = com.getAllExchanges().iterator();
        while (it.hasNext())
        {
            final Exchange k = (Exchange) it.next();
            final double bob = calcUtilityForExchange(k);
            final double[] fred = calcUtilityComponentsForExchange(k);
            if (components == null)
            {
                components = new double[fred.length + 1];
            }
            for (int componentNum = 0; componentNum < fred.length; componentNum++)
            {
                components[componentNum + 1] += fred[componentNum]
                        * (Math.exp(dispersionParameter * bob) / sum);
            }
        }

        double totalComponentUtility = 0;
        for (int componentNum = 1; componentNum < components.length; componentNum++)
        {
            totalComponentUtility += components[componentNum];
        }
        components[0] = logsum - totalComponentUtility;
        return components;

    }

    /**
     * @return the composite utility (log sum value) of all the alternatives
     */
    @Override
    public double getUtilityNoSizeEffect() throws ChoiceModelOverflowException
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        if (com.exchangeType == 'p' && theCommodityZUtility instanceof SellingZUtility)
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }
        if (com.exchangeType == 'c' && theCommodityZUtility instanceof BuyingZUtility)
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }
        if (com.exchangeType == 'n')
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }

        if (com.getAllExchanges().size() == 1)
        {
            final Exchange x = com.getAllExchanges().get(0);
            return calcUtilityForExchange(x);
        }

        // For all other Commodity exchange types, calculate CUSellc,z =
        // composite utility of selling
        // commodity 'c' in zone 'z' independent of producing activity.
        // CUSellc,z = (1/lambda)ln(sum(e^(lambda* SUc,z,k)over k)).
        Iterator it = com.getAllExchanges().iterator();
        double sum = 0;
        while (it.hasNext())
        {
            final Exchange k = (Exchange) it.next();
            final double bob = calcUtilityForExchange(k); // returns SUc,z,k for
            // each k
            sum += Math.exp(dispersionParameter * bob); // summing over k
        }
        final double bob = 1 / dispersionParameter * Math.log(sum); // calculating
        // the
        // logsum =
        // CUSellc,z

        // error checking:
        if (Double.isNaN(bob))
        { // write out the individual exchange utilities
          // to see what is going on.
            logger.error("composite utility is NaN for " + theCommodityZUtility);
            it = com.getAllExchanges().iterator();
            final StringBuffer exchangeUtilities = new StringBuffer();
            while (it.hasNext())
            {
                final Exchange x = (Exchange) it.next();
                final double fred = calcUtilityForExchange(x);
                exchangeUtilities.append(x.exchangeLocationUserID + ":" + fred + " ");
            }
            logger.error("individual exchange utilities: " + exchangeUtilities);

        }
        if (bob == Double.POSITIVE_INFINITY)
        {
            logger.error("composite utility is positive infinity for " + theCommodityZUtility);
            it = com.getAllExchanges().iterator();
            final StringBuffer exchangeUtilities = new StringBuffer();
            while (it.hasNext())
            {
                final Exchange x = (Exchange) it.next();
                final double fred = calcUtilityForExchange(x);
                exchangeUtilities.append(x.exchangeLocationUserID + ":" + fred + " ");
            }
            logger.error("individual exchange utilities: " + exchangeUtilities);
            throw new ChoiceModelOverflowException("CommodityZUtilityError for "
                    + theCommodityZUtility);
        }
        if (bob == Double.NEGATIVE_INFINITY)
        {
            logger.warn("composite utility is negative infinity for " + theCommodityZUtility);
        }

        return bob;

    }

    /**
     * @return the composite utility (log sum value) of all the alternatives
     */
    @Override
    public double getUtility(double higherLevelDispersionParameter)
            throws ChoiceModelOverflowException
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        if (com.exchangeType == 'p' && theCommodityZUtility instanceof SellingZUtility)
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }
        if (com.exchangeType == 'c' && theCommodityZUtility instanceof BuyingZUtility)
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }
        if (com.exchangeType == 'n')
        {
            return calcUtilityForExchange(com
                    .getExchange(theCommodityZUtility.myTaz.getZoneIndex()));
        }

        if (com.getAllExchanges().size() == 1)
        {
            final Exchange x = com.getAllExchanges().get(0);
            return calcUtilityForExchange(x);
        }

        // For all other Commodity exchange types, calculate CUSellc,z =
        // composite utility of selling
        // commodity 'c' in zone 'z' independent of producing activity.
        // CUSellc,z = (1/lambda)ln(sum(e^(lambda* SUc,z,k)over k)).
        Iterator it = com.getAllExchanges().iterator();
        double sum = 0;
        while (it.hasNext())
        {
            final Exchange k = (Exchange) it.next();
            final double bob = calcUtilityForExchange(k); // returns SUc,z,k for
            // each k
            sum += Math.exp(dispersionParameter * bob); // summing over k
        }
        final double bob = 1 / dispersionParameter * Math.log(sum); // calculating
        // the
        // logsum =
        // CUSellc,z

        // error checking:
        if (Double.isNaN(bob))
        { // write out the individual exchange utilities
          // to see what is going on.
            logger.error("composite utility is NaN for " + theCommodityZUtility);
            it = com.getAllExchanges().iterator();
            final StringBuffer exchangeUtilities = new StringBuffer();
            while (it.hasNext())
            {
                final Exchange x = (Exchange) it.next();
                final double fred = calcUtilityForExchange(x);
                exchangeUtilities.append(x.exchangeLocationUserID + ":" + fred + " ");
            }
            logger.error("individual exchange utilities: " + exchangeUtilities);

        }
        if (bob == Double.POSITIVE_INFINITY)
        {
            logger.error("composite utility is positive infinity for " + theCommodityZUtility);
            it = com.getAllExchanges().iterator();
            final StringBuffer exchangeUtilities = new StringBuffer();
            while (it.hasNext())
            {
                final Exchange x = (Exchange) it.next();
                final double fred = calcUtilityForExchange(x);
                exchangeUtilities.append(x.exchangeLocationUserID + ":" + fred + " ");
            }
            logger.error("individual exchange utilities: " + exchangeUtilities);
            throw new ChoiceModelOverflowException("CommodityZUtilityError for "
                    + theCommodityZUtility);
        }
        if (bob == Double.NEGATIVE_INFINITY)
        {
            logger.warn("composite utility is negative infinity for " + theCommodityZUtility);
        }

        return bob;

    }

    /**
     * @return partial derivatives of probability of choosing an exchange zone w.r.t. utility of that exchange zone.
     */
    public double[][] getChoiceDerivatives(double[][] arrayToPossiblyReuse)
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        double[] weights;
        double[][] returns;
        final Collection theExchanges = com.getAllExchanges();
        final Iterator it = theExchanges.iterator();
        weights = new double[theExchanges.size()];
        if (!(arrayToPossiblyReuse.length == theExchanges.size() && arrayToPossiblyReuse[0].length == theExchanges
                .size()))
        {
            returns = new double[theExchanges.size()][theExchanges.size()];
        } else
        {
            returns = arrayToPossiblyReuse;
        }
        if (com.exchangeType == 'p' && theCommodityZUtility instanceof SellingZUtility
                || com.exchangeType == 'c' && theCommodityZUtility instanceof BuyingZUtility
                || com.exchangeType == 'n')
        {
            int i;
            int j;
            for (i = 0; i < returns.length; i++)
            {
                for (j = 0; j < returns[i].length; j++)
                {
                    returns[i][j] = 0;
                }
            }
            return returns;
        }
        double sum = 0;
        int i = 0;
        while (it.hasNext())
        {
            final Exchange x = (Exchange) it.next();
            final double utility = calcUtilityForExchange(x);
            weights[i] = Math.exp(dispersionParameter * utility);
            if (Double.isNaN(weights[i]))
            {
                logger.error("hmm, Commodity Flow " + i
                        + " was such that LogitModel weight was NaN");
                throw new Error("NAN in weight for CommodityFlow " + i);
            }
            sum += weights[i];
            i++;
        }
        if (sum != 0)
        {
            int j;
            for (i = 0; i < weights.length; i++)
            {
                for (j = weights.length - 1; j >= 0; j--)
                {
                    if (i == j)
                    {
                        returns[i][j] = dispersionParameter
                                * (weights[i] / sum * (1 - weights[i] / sum));
                    } else
                    {
                        returns[i][j] = -dispersionParameter * (weights[i] / sum) * weights[j]
                                / sum;
                    }
                }
            }
        }
        return returns;
    }

    public double[] getChoiceProbabilities()
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        double[] weights;
        final List theExchanges = com.getAllExchanges();
        if (com.exchangeType == 'p' && theCommodityZUtility instanceof SellingZUtility
                || com.exchangeType == 'c' && theCommodityZUtility instanceof BuyingZUtility
                || com.exchangeType == 'n')
        {
            weights = new double[theExchanges.size()];
            final Exchange thisExchangeHere = com.getExchange(theCommodityZUtility.getTaz()
                    .getZoneIndex());
            final int index = theExchanges.indexOf(thisExchangeHere);
            weights[index] = 1;
            return weights;
        }
        final Iterator it = theExchanges.iterator();
        weights = new double[theExchanges.size()];
        double sum = 0;
        int i = 0;
        while (it.hasNext())
        {
            final Exchange x = (Exchange) it.next();
            final double utility = calcUtilityForExchange(x);
            weights[i] = Math.exp(dispersionParameter * utility);
            if (Double.isNaN(weights[i]))
            {
                logger.error("hmm, Commodity Flow " + i
                        + " was such that LogitModel weight was NaN");
                throw new Error("NAN in weight for CommodityFlow " + i);
            }
            sum += weights[i];
            i++;
        }
        if (sum != 0)
        {
            for (i = 0; i < weights.length; i++)
            {
                weights[i] /= sum;
            }
        }
        return weights;
    }

    @Override
    public void setAggregateQuantity(double amount, double derivative)
            throws ChoiceModelOverflowException
    {
        final Commodity com = theCommodityZUtility.getCommodity();
        final int taz = theCommodityZUtility.myTaz.getZoneIndex();
        char selling = 's';
        if (theCommodityZUtility instanceof BuyingZUtility)
        {
            selling = 'b';
        }
        // First deal with the special cases where the production/consumption
        // zone is the exchange zone
        // (or it is a non-transferable commodity)
        if (com.exchangeType == 'p' && selling == 's' || com.exchangeType == 'c' && selling == 'b'
                || com.exchangeType == 'n' || com.getAllExchanges().size() == 1)
        {
            Exchange x = null;
            if (com.getAllExchanges().size() == 1)
            {
                x = com.getAllExchanges().get(0);
            } else
            {
                x = theCommodityZUtility.getCommodity().getExchange(taz);
            }
            try
            {
                if (Double.isNaN(amount) || Double.isInfinite(amount))
                {
                    logger.warn("quantity for intrazonal flow " + taz + " selling:" + selling
                            + " to " + x);
                    logger.warn("   amount:" + amount);
                    throw new ChoiceModelOverflowException("Infinite or NaN flow");
                }
                if (selling == 's')
                {
                    x.setFlowQuantityAndDerivative(taz, selling, amount,
                            theCommodityZUtility.myCommodity.getSellingUtilityPriceCoefficient()
                                    * derivative);
                } else
                {
                    x.setFlowQuantityAndDerivative(taz, selling, amount,
                            theCommodityZUtility.myCommodity.getBuyingUtilityPriceCoefficient()
                                    * derivative);
                }
            } catch (final OverflowException e)
            {
                throw new ChoiceModelOverflowException(e.toString());
            }
        } else
        {
            final List theExchanges = com.getAllExchanges();
            synchronized (theExchanges)
            {
                double sum = 0;
                if (aggregateQuantityWeights.length != theExchanges.size())
                {
                    aggregateQuantityWeights = new double[theExchanges.size()];
                }
                final Iterator it = theExchanges.iterator();
                int i = 0;
                while (it.hasNext())
                {
                    final Exchange x = (Exchange) it.next();
                    final double utility = calcUtilityForExchange(x);
                    aggregateQuantityWeights[i] = Math.exp(dispersionParameter * utility);
                    // if (theCommodityZUtility instanceof SellingZUtility) {
                    // derivativeComponents[i] =weights[i]*
                    // theCommodityZUtility.myCommodity.getSellingUtilityPriceCoefficient();
                    // } else {
                    // derivativeComponents[i] =weights[i]*
                    // theCommodityZUtility.myCommodity.getBuyingUtilityPriceCoefficient();
                    // }
                    if (Double.isNaN(aggregateQuantityWeights[i]))
                    {
                        logger.error("hmm, Commodity Flow " + i
                                + " was such that LogitModel weight was NaN");
                        throw new Error("NAN in weight for CommodityFlow " + i);
                    }
                    sum += aggregateQuantityWeights[i];
                    i++;
                }
                if (sum != 0)
                {
                    for (i = 0; i < aggregateQuantityWeights.length; i++)
                    {
                        aggregateQuantityWeights[i] /= sum;
                    }
                }
            }
            for (int i = 0; i < aggregateQuantityWeights.length; i++)
            {
                final float quantity = (float) (amount * aggregateQuantityWeights[i]);
                final Exchange anEx = (Exchange) theExchanges.get(i);
                if (Double.isNaN(quantity) || Double.isInfinite(quantity))
                {
                    logger.warn("quantity for flow " + taz + " selling:" + selling + " to " + anEx);
                    logger.warn("   amount:" + amount + " * weight:" + aggregateQuantityWeights[i]
                            + " = " + quantity);
                }
                try
                {
                    if (theCommodityZUtility instanceof SellingZUtility)
                    {
                        anEx.setFlowQuantityAndDerivative(
                                taz,
                                selling,
                                quantity,
                                (derivative * aggregateQuantityWeights[i]
                                        * aggregateQuantityWeights[i] + amount
                                        * dispersionParameter * aggregateQuantityWeights[i]
                                        * (1 - aggregateQuantityWeights[i]))
                                        * theCommodityZUtility.myCommodity
                                                .getSellingUtilityPriceCoefficient());
                    } else
                    {
                        anEx.setFlowQuantityAndDerivative(
                                taz,
                                selling,
                                quantity,
                                (derivative * aggregateQuantityWeights[i]
                                        * aggregateQuantityWeights[i] + amount
                                        * dispersionParameter * aggregateQuantityWeights[i]
                                        * (1 - aggregateQuantityWeights[i]))
                                        * theCommodityZUtility.myCommodity
                                                .getBuyingUtilityPriceCoefficient());
                    }
                } catch (final OverflowException e)
                {
                    throw new ChoiceModelOverflowException(e.toString());
                }
            }
        }
    }

    public double[] getLogsumDerivativesWRTPrices()
    {
        // derivative of logsum is just the probabilities
        final double[] derivatives = getChoiceProbabilities();
        double multiplier = 0;
        if (theCommodityZUtility instanceof SellingZUtility)
        {
            multiplier = theCommodityZUtility.myCommodity.getSellingUtilityPriceCoefficient();
        }
        if (theCommodityZUtility instanceof BuyingZUtility)
        {
            multiplier = theCommodityZUtility.myCommodity.getBuyingUtilityPriceCoefficient();
        }
        for (int i = 0; i < derivatives.length; i++)
        {
            derivatives[i] *= multiplier;
        }
        return derivatives;
    }

}
