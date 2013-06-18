package com.hbaspecto.pecas.sd.orm;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldLong;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Base class of table most_recent_local_effect_with_taz.<br>
 * Do not edit as will be regenerated by running SimpleORMGenerator Generated on
 * Fri Sep 25 16:13:29 MDT 2009
 ***/
public abstract class MostRecentLocalEffectWithTaz_gen extends SRecordInstance
		implements java.io.Serializable {

	public static final SRecordMeta<MostRecentLocalEffectWithTaz> meta = new SRecordMeta<MostRecentLocalEffectWithTaz>(
			MostRecentLocalEffectWithTaz.class, "most_recent_local_effect_with_taz");

	// Columns in table
	public static final SFieldLong PecasParcelNum =
	// added manually
	new SFieldLong(meta, "pecas_parcel_num", new SFieldFlags[] {
			SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

	public static final SFieldInteger Taz = new SFieldInteger(meta, "taz");

	public static final SFieldInteger LocalEffectId =
	// changed manually
	new SFieldInteger(meta, "local_effect_id", new SFieldFlags[] {
			SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

	public static final SFieldDouble LocalEffectDistance = new SFieldDouble(meta,
			"local_effect_distance");

	public static final SFieldInteger CurrentLocalEffectYear = new SFieldInteger(
			meta, "current_local_effect_year");

	// Column getters and setters
	public long get_PecasParcelNum() {
		return getLong(PecasParcelNum);
	}

	public void set_PecasParcelNum(long value) {
		setLong(PecasParcelNum, value);
	}

	public int get_Taz() {
		return getInt(Taz);
	}

	public void set_Taz(int value) {
		setInt(Taz, value);
	}

	public int get_LocalEffectId() {
		return getInt(LocalEffectId);
	}

	public void set_LocalEffectId(int value) {
		setInt(LocalEffectId, value);
	}

	public double get_LocalEffectDistance() {
		return getDouble(LocalEffectDistance);
	}

	public void set_LocalEffectDistance(double value) {
		setDouble(LocalEffectDistance, value);
	}

	public int get_CurrentLocalEffectYear() {
		return getInt(CurrentLocalEffectYear);
	}

	public void set_CurrentLocalEffectYear(int value) {
		setInt(CurrentLocalEffectYear, value);
	}

	// Find and create
	public static MostRecentLocalEffectWithTaz findOrCreate(SSessionJdbc ses) {
		return ses.findOrCreate(meta, new Object[] {});
	}

	@Override
	public SRecordMeta<MostRecentLocalEffectWithTaz> getMeta() {
		return meta;
	}
}
