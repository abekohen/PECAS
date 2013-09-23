package com.hbaspecto.pecas.sd.orm;

import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Base class of table current_year_table.<br>
 * Do not edit as will be regenerated by running SimpleORMGenerator Generated on
 * Fri Sep 25 16:13:28 MDT 2009
 ***/
abstract class CurrentYearTable_gen
        extends SRecordInstance
        implements java.io.Serializable
{

    public static final SRecordMeta<CurrentYearTable> meta        = new SRecordMeta<CurrentYearTable>(
                                                                          CurrentYearTable.class,
                                                                          "current_year_table");

    // Columns in table
    public static final SFieldInteger                 CurrentYear = new SFieldInteger(meta,
                                                                          "current_year",
                                                                          new SFieldFlags[] {
            SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY              });

    // Column getters and setters
    public int get_CurrentYear()
    {
        return getInt(CurrentYear);
    }

    public void set_CurrentYear(int value)
    {
        setInt(CurrentYear, value);
    }

    // Find and create
    public static CurrentYearTable findOrCreate(SSessionJdbc ses, int _CurrentYear)
    {
        return ses.findOrCreate(meta, new Object[] {new Integer(_CurrentYear)});
    }

    public SRecordMeta<CurrentYearTable> getMeta()
    {
        return meta;
    }
}
