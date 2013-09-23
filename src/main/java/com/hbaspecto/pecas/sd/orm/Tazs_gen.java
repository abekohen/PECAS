package com.hbaspecto.pecas.sd.orm;

import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import com.hbaspecto.pecas.land.Tazs;

/**
 * Base class of table tazs.<br>
 * Do not edit as will be regenerated by running SimpleORMGenerator Generated on
 * Fri Sep 25 16:13:29 MDT 2009
 ***/
public abstract class Tazs_gen
        extends SRecordInstance
        implements java.io.Serializable
{

    public static final SRecordMeta<Tazs> meta       = new SRecordMeta<Tazs>(Tazs.class, "tazs");

    // Columns in table
    public static final SFieldInteger     TazNumber  = new SFieldInteger(meta, "taz_number",
                                                             new SFieldFlags[] {
            SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

    public static final SFieldInteger     LuzNumber  = new SFieldInteger(
                                                             meta,
                                                             "luz_number",
                                                             new SFieldFlags[] {SFieldFlags.SMANDATORY});

    public static final SFieldInteger     CountyFips = new SFieldInteger(meta, "county_fips");

    // Column getters and setters
    public int get_TazNumber()
    {
        return getInt(TazNumber);
    }

    public void set_TazNumber(int value)
    {
        setInt(TazNumber, value);
    }

    public int get_LuzNumber()
    {
        return getInt(LuzNumber);
    }

    public void set_LuzNumber(int value)
    {
        setInt(LuzNumber, value);
    }

    public int get_CountyFips()
    {
        return getInt(CountyFips);
    }

    public void set_CountyFips(int value)
    {
        setInt(CountyFips, value);
    }

    // Foreign key getters and setters
    public Luzs get_LUZS(SSessionJdbc ses)
    {
        try
        {
            /**
             * Old code: return Luzs.findOrCreate(get_LuzNumber()); New code
             * below :
             **/
            return ses.findOrCreate(Luzs.meta, new Object[] {get_LuzNumber(),});
        } catch (SException e)
        {
            if (e.getMessage().indexOf("Null Primary key") > 0)
            {
                return null;
            }
            throw e;
        }
    }

    public void set_LUZS(Luzs value)
    {
        set_LuzNumber(value.get_LuzNumber());
    }

    // Find and create
    public static Tazs findOrCreate(SSessionJdbc ses, int _TazNumber)
    {
        return ses.findOrCreate(meta, new Object[] {new Integer(_TazNumber)});
    }

    public SRecordMeta<Tazs> getMeta()
    {
        return meta;
    }
}
