package com.hbaspecto.pecas.sd.orm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.sessionjdbc.SSessionJdbc;

import com.hbaspecto.pecas.sd.SpaceTypesI;

/**
 * Business rules class for table space_types_group.<br>
 * Will not be regenerated by SimpleORMGenerator, add any business rules to this
 * class
 **/

public class SpaceTypesGroup extends SpaceTypesGroup_gen implements
		java.io.Serializable {

	// TODO figure out a better way to set the year field
	private static Integer currentYear = null;

	static Logger logger = Logger.getLogger(SpaceTypesGroup.class);
	private static HashMap<Integer, SpaceTypesGroup> spaceTypesGroupHash = new HashMap<Integer, SpaceTypesGroup>();
	private static HashMap<Integer, Double> constructionTargets = new HashMap<Integer, Double>();

	public static SpaceTypesGroup getSpaceTypeGroupByID(int spaceGroupID) {
		SpaceTypesGroup theOne = spaceTypesGroupHash.get(spaceGroupID);
		if (theOne == null) {
			final SSessionJdbc session = SSessionJdbc.getThreadLocalSession();
			theOne = session.find(SpaceTypesGroup_gen.meta, spaceGroupID);
			if (theOne != null) {
				spaceTypesGroupHash.put(spaceGroupID, theOne);
			}
		}
		return theOne;
	}

	public static double getTargetConstructionQuantity(int spaceGroupID) {
		/*
		 * The following query gets all the commodities in a group together with
		 * their total production (TPc). SELECT commodity, sum(internal_bought) as
		 * sum from exchange_results WHERE LUZ in (SELECT luz_number from luzs) AND
		 * commodity in (SELECT cc_commodity_name FROM construction_commodities
		 * WHERE space_types_group_id = spaceGroupID) GROUP BY commodity
		 */

		if (getCurrentYear() == null) {
			final String msg = "set current year for space type groups";
			logger.fatal(msg);
			throw new RuntimeException(msg);
		}
		Double target;
		target = constructionTargets.get(spaceGroupID);

		if (target != null) {
			return target.doubleValue();
		}
		else {
			target = 0.0;

			final SSessionJdbc session = SSessionJdbc.getThreadLocalSession();
			final Iterator<ConstructionCommodities> cc = ConstructionCommodities
					.getConstCommodityBySpaceTypesGroup(session, spaceGroupID).iterator();

			while (cc.hasNext()) {
				final ConstructionCommodities cc_instance = cc.next();
				// FIXME exchange_results table is hard coded, shouldn't we use
				// sd_rents?
				final double tp = ((Double) session
						.rawQuerySingle(
								" SELECT sum(internal_bought) as sum  from exchange_results "
										+ " WHERE LUZ in (SELECT luz_number from luzs) AND commodity = ? ",
								false, new String(cc_instance.get_CcCommodityName())))
						.doubleValue();
				target += tp * cc_instance.get_ConvertingFactor();
			}

			// Now find space types in the group;
			final SQuery<SpaceTypesI> query = new SQuery<SpaceTypesI>(
					SpaceTypesI_gen.meta).eq(SpaceTypesI_gen.SpaceTypeGroupId,
					spaceGroupID);

			final SQueryResult<SpaceTypesI> sts = session.query(query);
			for (final SpaceTypesI st : sts) {
				final SiteSpecTotals ss = session.find(SiteSpecTotals_gen.meta,
						st.get_SpaceTypeId(), getCurrentYear());
				if (ss == null) {
					logger.warn("No site spec for " + st);
				}
				else {
					if (ss.get_SpaceQuantity() > 0) {
						target -= ss.get_SpaceQuantity();
					}
				}
			}
			if (target < 0) {
				logger.warn("Construction target for space type group " + spaceGroupID
						+ " is zero");
				target = 0.0;
			}
			constructionTargets.put(spaceGroupID, target);
			return target;
		}
	}

	public static double getObtainedConstructionQuantity(int spaceTypeGroupId) {

		final List<SpaceTypesI> spaceTypes = SpaceTypesI
				.getSpaceTypesBySpaceTypeGroup(spaceTypeGroupId);
		double obtainedQnty = 0;

		final Iterator<SpaceTypesI> itr = spaceTypes.iterator();
		SpaceTypesI sp;
		while (itr.hasNext()) {
			sp = itr.next();
			obtainedQnty += sp.cumulativeAmountOfDevelopment
					* sp.get_ConvertingFactorForSpaceTypeGroup();
		}
		return obtainedQnty;
	}

	public static Integer getCurrentYear() {
		return currentYear;
	}

	public static void setCurrentYear(Integer currentYear) {
		SpaceTypesGroup.currentYear = currentYear;
	}
}
