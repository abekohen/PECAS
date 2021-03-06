package com.hbaspecto.pecas.sd.orm;

import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import simpleorm.dataset.SFieldBooleanBit;
import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldLong;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import com.hbaspecto.pecas.land.Parcels;
import com.hbaspecto.pecas.land.Tazs;
import com.hbaspecto.pecas.sd.SpaceTypesI;
import com.pb.common.util.ResourceUtil;

/**
 * Base class of table parcels.<br>
 * Do not edit as will be regenerated by running SimpleORMGenerator Generated on
 * Fri Sep 25 16:13:29 MDT 2009
 ***/
public abstract class Parcels_gen
        extends SRecordInstance
        implements java.io.Serializable
{

    static Logger                      logger = Logger.getLogger(Parcels_gen.class);

    public static SRecordMeta<Parcels> meta;

    // Columns in table
    public static SFieldString         ParcelId;

    public static SFieldLong           PecasParcelNum;

    public static SFieldInteger        YearBuilt;

    public static SFieldInteger        Taz;

    public static SFieldInteger        SpaceTypeId;

    public static SFieldDouble         SpaceQuantity;

    public static SFieldDouble         LandArea;

    public static SFieldInteger        AvailableServicesCode;

    public static SFieldBooleanBit     IsDerelict;

    public static SFieldBooleanBit     IsBrownfield;

    public Parcels_gen()
    {
        if (meta == null)
        {
            String msg = Parcels_gen.class.getName() + " was not initialized for ORM";
            logger.fatal(msg);
            throw new RuntimeException(msg);
        }
    }

    public static void init(ResourceBundle rb)
    {

        meta = new SRecordMeta<Parcels>(Parcels.class, ResourceUtil.getProperty(rb,
                "sdorm.parcels", "parcels"));

        // Columns in table
        ParcelId = new SFieldString(meta, ResourceUtil.getProperty(rb, "sdorm.parcels.parcel_id",
                "parcel_id"), 2147483647);

        PecasParcelNum = new SFieldLong(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.pecas_parcel_num", "pecas_parcel_num"), new SFieldFlags[] {
                SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY});

        YearBuilt = new SFieldInteger(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.year_built", "year_built"));

        Taz = new SFieldInteger(meta, ResourceUtil.getProperty(rb, "sdorm.parcels.taz", "taz"));

        SpaceTypeId = new SFieldInteger(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.space_type_id", "space_type_id"));

        SpaceQuantity = new SFieldDouble(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.space_quantity", "space_quantity"));

        LandArea = new SFieldDouble(meta, ResourceUtil.getProperty(rb, "sdorm.parcels.land_area",
                "land_area"));

        AvailableServicesCode = new SFieldInteger(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.available_services_code", "available_services_code"));

        IsDerelict = new SFieldBooleanBit(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.is_derelict", "is_derelict"));

        IsBrownfield = new SFieldBooleanBit(meta, ResourceUtil.getProperty(rb,
                "sdorm.parcels.is_brownfield", "is_brownfield"));

    }

    // Column getters and setters
    public String get_ParcelId()
    {
        return getString(ParcelId);
    }

    public void set_ParcelId(String value)
    {
        setString(ParcelId, value);
    }

    public long get_PecasParcelNum()
    {
        return getLong(PecasParcelNum);
    }

    public void set_PecasParcelNum(long value)
    {
        setLong(PecasParcelNum, value);
    }

    public int get_YearBuilt()
    {
        return getInt(YearBuilt);
    }

    public void set_YearBuilt(int value)
    {
        setInt(YearBuilt, value);
    }

    public int get_Taz()
    {
        return getInt(Taz);
    }

    public void set_Taz(int value)
    {
        setInt(Taz, value);
    }

    public int get_SpaceTypeId()
    {
        return getInt(SpaceTypeId);
    }

    public void set_SpaceTypeId(int value)
    {
        setInt(SpaceTypeId, value);
    }

    public double get_SpaceQuantity()
    {
        return getDouble(SpaceQuantity);
    }

    public void set_SpaceQuantity(double value)
    {
        setDouble(SpaceQuantity, value);
    }

    public double get_LandArea()
    {
        return getDouble(LandArea);
    }

    public void set_LandArea(double value)
    {
        setDouble(LandArea, value);
    }

    public int get_AvailableServicesCode()
    {
        return getInt(AvailableServicesCode);
    }

    public void set_AvailableServicesCode(int value)
    {
        setInt(AvailableServicesCode, value);
    }

    public boolean get_IsDerelict()
    {
        return getBoolean(IsDerelict);
    }

    public void set_IsDerelict(boolean value)
    {
        setBoolean(IsDerelict, value);
    }

    public boolean get_IsBrownfield()
    {
        return getBoolean(IsBrownfield);
    }

    public void set_IsBrownfield(boolean value)
    {
        setBoolean(IsBrownfield, value);
    }

    // Foreign key getters and setters
    public SpaceTypesI get_SPACE_TYPES_I(SSessionJdbc ses)
    {
        try
        {
            /**
             * Old code: return SpaceTypesI.findOrCreate(get_SpaceTypeId()); New
             * code below :
             **/
            return ses.findOrCreate(SpaceTypesI.meta, new Object[] {get_SpaceTypeId(),});
        } catch (SException e)
        {
            if (e.getMessage().indexOf("Null Primary key") > 0)
            {
                return null;
            }
            throw e;
        }
    }

    public void set_SPACE_TYPES_I(SpaceTypesI value)
    {
        set_SpaceTypeId(value.get_SpaceTypeId());
    }

    public Tazs get_TAZS(SSessionJdbc ses)
    {
        try
        {
            /**
             * Old code: return Tazs.findOrCreate(get_Taz()); New code below :
             **/
            return ses.findOrCreate(Tazs.meta, new Object[] {get_Taz(),});
        } catch (SException e)
        {
            if (e.getMessage().indexOf("Null Primary key") > 0)
            {
                return null;
            }
            throw e;
        }
    }

    public void set_TAZS(Tazs value)
    {
        set_Taz(value.get_TazNumber());
    }

    // Find and create
    public static Parcels findOrCreate(SSessionJdbc ses, long _PecasParcelNum)
    {
        return ses.findOrCreate(meta, new Object[] {new Long(_PecasParcelNum)});
    }

    public SRecordMeta<Parcels> getMeta()
    {
        return meta;
    }
}
