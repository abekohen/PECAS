package com.hbaspecto.pecas.sd.orm;

import org.apache.log4j.Logger;

import simpleorm.dataset.SQuery;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Business rules class for table local_effect_parameters.<br>
 * Will not be regenerated by SimpleORMGenerator, add any business rules to this
 * class
 **/

public class LocalEffectParameters extends LocalEffectParameters_gen implements
		java.io.Serializable {

	static boolean queried = false;

	public static LocalEffectParameters findInCache(int localEffectId,
			int spaceTypeID) {
		// first make sure everything is in cache.
		final SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		if (!queried) {
			ses.query(new SQuery<LocalEffectParameters>(meta));
			queried = true;
		}
		final LocalEffectParameters theOne = ses.getDataSet().find(meta,
				localEffectId, spaceTypeID);
		return theOne;
	}

	static final Logger logger = Logger.getLogger(LocalEffectParameters.class);

	public double applyFunction(double rent, double localEffectDistance) {
		localEffectDistance = Math.min(localEffectDistance, get_MaxDist());
		switch (get_FunctionType()) {
		case 1:
			if (localEffectDistance == get_MaxDist()) {
				rent *= get_ThetaParameter();
			}
			break;
		case 2:
			rent *= Math.exp(get_ThetaParameter()
					* (localEffectDistance / get_MaxDist()));
			break;
		case 3:
			rent *= Math.exp(get_ThetaParameter()
					* (1 - localEffectDistance / get_MaxDist()));
			break;
		case 4:
			rent *= Math.pow(get_ThetaParameter(), localEffectDistance
					/ get_MaxDist());
			break;
		case 5:
			rent *= Math.pow(get_ThetaParameter(), 1 - localEffectDistance
					/ get_MaxDist());
			break;
		case 6:
			rent *= Math.pow(1 - get_ThetaParameter(), localEffectDistance
					/ get_MaxDist());
			break;
		case 7:
			rent *= Math.pow(1 - get_ThetaParameter(), 1 - localEffectDistance
					/ get_MaxDist());
			break;
		case 8:
			rent *= Math.pow(localEffectDistance / get_MaxDist(),
					get_ThetaParameter());
			break;
		case 9:
			rent *= Math.pow(1 - localEffectDistance / get_MaxDist(),
					get_ThetaParameter());
			break;
		default:
			logger.fatal("Invalid local effect function " + get_FunctionType()
					+ " Only 1-9 are defined");
			throw new RuntimeException("Invalid local effect function "
					+ get_FunctionType() + " Only 1-9 are defined");

		}
		return rent;
	}

	public double applyFunctionForMaxDist(double rent) {
		switch (get_FunctionType()) {
		case 1:
		case 4:
			rent *= get_ThetaParameter();
			break;
		case 2:
			rent *= Math.exp(get_ThetaParameter());
			break;
		case 3:
		case 5:
		case 7:
		case 8:
			rent *= 1;
			break;
		case 6:
			rent *= 1 - get_ThetaParameter();
			break;
		case 9:
			rent *= 0;
			break;
		default:
			logger.fatal("Invalid local effect function " + get_FunctionType()
					+ " Only 1-9 are defined");
			throw new RuntimeException("Invalid local effect function "
					+ get_FunctionType() + " Only 1-9 are defined");

		}
		return rent;
	}

}
