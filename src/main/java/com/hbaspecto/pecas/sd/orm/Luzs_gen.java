package com.hbaspecto.pecas.sd.orm;

import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Base class of table luzs.<br>
 * Do not edit as will be regenerated by running SimpleORMGenerator Generated on
 * Fri Sep 25 16:13:29 MDT 2009
 ***/
abstract class Luzs_gen
        extends SRecordInstance
        implements java.io.Serializable
{

    public static final SRecordMeta<Luzs> meta      = new SRecordMeta<Luzs>(Luzs.class, "luzs");

    // Columns in table
    public static final SFieldInteger     LuzNumber = new SFieldInteger(meta, "luz_number",
                                                            new SFieldFlags[] {
            SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY});

    public static final SFieldString      LuzName   = new SFieldString(meta, "luz_name", 2147483647);

    // Column getters and setters
    public int get_LuzNumber()
    {
        return getInt(LuzNumber);
    }

    public void set_LuzNumber(int value)
    {
        setInt(LuzNumber, value);
    }

    public String get_LuzName()
    {
        return getString(LuzName);
    }

    public void set_LuzName(String value)
    {
        setString(LuzName, value);
    }

    // Find and create
    public static Luzs findOrCreate(SSessionJdbc ses, int _LuzNumber)
    {
        return ses.findOrCreate(meta, new Object[] {new Integer(_LuzNumber)});
    }

    public SRecordMeta<Luzs> getMeta()
    {
        return meta;
    }
}
