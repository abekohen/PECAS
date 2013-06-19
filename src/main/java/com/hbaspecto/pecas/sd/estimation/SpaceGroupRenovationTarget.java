package com.hbaspecto.pecas.sd.estimation;

import org.apache.log4j.Logger;

public class SpaceGroupRenovationTarget extends RenovationTarget implements
		ExpectedValue {
	static Logger logger = Logger.getLogger(SpaceGroupRenovationTarget.class);

	private final int[] spaceTypes;
	public static final String NAME = "renotarg";

	public SpaceGroupRenovationTarget(int[] spacetypes) {
		spaceTypes = spacetypes;
	}

	public SpaceGroupRenovationTarget(String[] pieces) {
		spaceTypes = new int[pieces.length - 1];
		for (int i = 1; i < pieces.length; i++) {
			int type = 0;
			try {
				type = Integer.valueOf(pieces[i]).intValue();
			}
			catch (final NumberFormatException e) {
				logger.error("Can't interpret space type " + pieces[i] + " in "
						+ this.getClass().getName());
			}
			spaceTypes[i - 1] = type;
		}
	}

	public int[] getSpacetype() {
		return spaceTypes;
	}

	@Override
	public boolean appliesToCurrentParcel() {
		return true;
	}

	@Override
	public String getName() {
		final StringBuffer buf = new StringBuffer(NAME);
		for (final int type : spaceTypes) {
			buf.append("-");
			buf.append(type);
		}
		return buf.toString();
	}

	@Override
	public double getModelledRenovateQuantityForParcel(int checkSpaceType,
			double quantity) {
		// applies to all parcels so don't need to check that.
		for (final int spaceType : spaceTypes) {
			if (spaceType == checkSpaceType) {
				return quantity;
			}
		}
		return 0;
	}

	@Override
	public double getModelledRenovateDerivativeForParcel(int checkSpaceType,
			double quantity) {
		for (final int spaceType : spaceTypes) {
			if (spaceType == checkSpaceType) {
				return 1;
			}
		}
		return 0;
	}

}