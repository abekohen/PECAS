package com.hbaspecto.pecas.sd.orm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simpleorm.dataset.SQuery;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Business rules class for table construction_commodities.<br>
 * Will not be regenerated by SimpleORMGenerator, add any business rules to this class
 **/

public class ConstructionCommodities
        extends ConstructionCommodities_gen
        implements java.io.Serializable
{

    public static ArrayList<String> getAllConstCommodityNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        SQuery<ConstructionCommodities> qry = new SQuery<ConstructionCommodities>(
                ConstructionCommodities.meta);

        SSessionJdbc session = SSessionJdbc.getThreadLocalSession();
        List<ConstructionCommodities> list = session.query(qry);
        Iterator<ConstructionCommodities> itr = list.iterator();
        while (itr.hasNext())
        {
            names.add(itr.next().get_CcCommodityName());
        }
        return names;
    }

    /*
     * public static ArrayList<String> getConstCommodityNamesBySpaceTypesGroup(int spaceTypesGroupID){ ArrayList<String> names = new
     * ArrayList<String>(); SQuery<ConstructionCommodities> qry = new
     * SQuery<ConstructionCommodities>(ConstructionCommodities.meta).eq(ConstructionCommodities.SpaceTypesGroupId, spaceTypesGroupID);
     * 
     * SSessionJdbc session = SSessionJdbc.getThreadLocalSession(); List<ConstructionCommodities> list = session.query(qry);
     * Iterator<ConstructionCommodities> itr = list.iterator(); while (itr.hasNext()){ names.add(itr.next().get_CcCommodityName()); } return names; }
     */
    public static List<ConstructionCommodities> getConstCommodityBySpaceTypesGroup(
            SSessionJdbc session, int spaceTypesGroupID)
    {

        SQuery<ConstructionCommodities> qry = new SQuery<ConstructionCommodities>(
                ConstructionCommodities.meta).eq(ConstructionCommodities.SpaceTypesGroupId,
                spaceTypesGroupID);
        List<ConstructionCommodities> list = session.query(qry);

        return list;
    }
}
