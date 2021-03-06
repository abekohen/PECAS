package com.hbaspecto.pecas.sd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import com.hbaspecto.discreteChoiceModelling.Coefficient;
import com.hbaspecto.pecas.sd.estimation.CSVEstimationReader;
import com.hbaspecto.pecas.sd.estimation.EstimationTarget;
import com.hbaspecto.pecas.sd.estimation.SpaceTypeCoefficient;
import com.hbaspecto.pecas.sd.estimation.SpaceTypeTAZTarget;

@Ignore
// TODO: Fix test
public class TestCSVEstimationReader
{
    private static CSVEstimationReader reader;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException
    {
        reader = new CSVEstimationReader("testconfig\\readertesttargets.csv", false,
                "testconfig\\readertestparams.csv", false);
    }

    @Test
    public void testReadTargets()
    {
        final List<EstimationTarget> targets = reader.readTargets();
        assertEquals(20, targets.size());
        for (final EstimationTarget t : targets)
        {
            assertTrue(t instanceof SpaceTypeTAZTarget);
        }
        int k = 0;
        for (int i = 1; i <= 4; i++)
        {
            for (int j = 1; j <= 5; j++)
            {
                assertEquals("taztarg-" + i + "-" + j, targets.get(k).getName());
                k++;
            }
        }

        final double[] expected = {378.6815466, 883.5967928, 510.8600894, 305.8843212, 593.402637,
                868.8087966, 644.4314663, 516.4370728, 526.4464629, 504.5671652, 528.724377,
                533.5662869, 451.2276492, 774.1145464, 659.8574471, 557.7130335, 995.2170881,
                414.5615009, 727.2571633, 356.1280974};

        for (int i = 0; i < targets.size(); i++)
        {
            assertEquals(expected[i], targets.get(i).getTargetValue(), 0.00001);
        }

    }

    @Test
    public void testReadTargetVariance()
    {
        final double[][] expected = {
                {1433.997138, 571.1555382, 823.0018845, -223.4722664, -8.961768557, -518.6011338,
                        -201.36761, 177.6342282, -514.4256076, 95.9781135, 224.4255266, 175.231251,
                        -398.3566375, 740.9337755, 194.8507207, -212.612644, 523.9584425,
                        202.0551012, 1340.250055, 691.9020468},
                {571.1555382, 7807.432923, -1008.287223, -843.8506987, 1424.93334, -1097.470071,
                        -1921.131695, -158.0937964, 1298.340189, -260.3926929, -167.2177556,
                        1002.315411, 317.7381332, 108.5426395, 1010.300195, 181.3146389,
                        -3445.479737, -1674.505896, -52.19964763, 588.0228597},
                {823.0018845, -1008.287223, 2609.780309, 126.5282906, 241.7159261, 752.0969444,
                        403.800206, 621.5511409, 663.4556246, -179.0872087, 30.25613142,
                        611.5705785, -1415.608685, 162.3597052, 150.6347092, 696.1542498,
                        2060.45654, 50.4969518, 55.06493948, -99.98809961},
                {-223.4722664, -843.8506987, 126.5282906, 935.6521794, 104.2033995, 259.0174684,
                        123.991263, -334.9303078, 404.1834816, 46.18931145, 237.2315653,
                        -99.52051569, 243.784977, -98.55122038, 44.8800809, 243.2613568,
                        174.8469166, 499.6531378, -548.915649, -204.0789017},
                {-8.961768557, 1424.93334, 241.7159261, 104.2033995, 3521.266896, 937.2416658,
                        -924.8373046, 770.0685975, 723.0857555, -673.8962234, 1577.087209,
                        1416.156766, 352.655748, 351.9592518, 1368.70192, 256.0425573,
                        -1247.585607, -470.487452, -136.665057, -200.1971039},
                {-518.6011338, -1097.470071, 752.0969444, 259.0174684, 937.2416658, 7548.287251,
                        2325.966523, 1514.131477, 267.110491, 221.2184607, -336.3542435,
                        861.4603604, -701.3211951, -1588.468658, -91.7627839, 1438.61345,
                        644.0338574, -300.8118586, -1315.859183, 237.0102823},
                {-201.36761, -1921.131695, 403.800206, 123.991263, -924.8373046, 2325.966523,
                        4152.919148, -179.9412706, 131.2080733, -166.8706226, 160.4902146,
                        -459.5739406, 227.1033908, 915.5581279, -231.5381304, -1270.020176,
                        1215.915369, -52.0939731, -1695.432391, -19.32622407},
                {177.6342282, -158.0937964, 621.5511409, -334.9303078, 770.0685975, 1514.131477,
                        -179.9412706, 2667.072502, 708.2417258, 542.0296893, -699.5812085,
                        228.8868338, -550.7766761, -595.196204, 1738.310956, 485.8474989,
                        -910.5809417, 32.63691136, -137.324335, 267.707855},
                {-514.4256076, 1298.340189, 663.4556246, 404.1834816, 723.0857555, 267.110491,
                        131.2080733, 708.2417258, 2771.458783, 99.91502845, -109.4362706,
                        657.3776356, -330.4669272, -695.223341, 1640.020573, 1188.493562,
                        -1088.926741, -387.5314609, -1461.346485, -532.0513303},
                {95.9781135, -260.3926929, -179.0872087, 46.18931145, -673.8962234, 221.2184607,
                        -166.8706226, 542.0296893, 99.91502845, 2545.880242, -874.220241,
                        -1024.545996, -163.8161232, -687.0126441, 919.4059893, 375.4680045,
                        771.1535868, 608.9142074, 34.61837495, 505.2205011},
                {224.4255266, -167.2177556, 30.25613142, 237.2315653, 1577.087209, -336.3542435,
                        160.4902146, -699.5812085, -109.4362706, -874.220241, 2795.494668,
                        1321.665216, 368.7768491, 653.1795742, 176.0395102, -799.2756642,
                        825.2518195, 362.7876138, 1373.647146, -135.7935899},
                {175.231251, 1002.315411, 611.5705785, -99.52051569, 1416.156766, 861.4603604,
                        -459.5739406, 228.8868338, 657.3776356, -1024.545996, 1321.665216,
                        2846.929825, -899.3349413, -1644.452985, 891.2077462, 255.800972,
                        456.6066048, -105.004444, 1331.866803, -562.0645647},
                {-398.3566375, 317.7381332, -1415.608685, 243.784977, 352.655748, -701.3211951,
                        227.1033908, -550.7766761, -330.4669272, -163.8161232, 368.7768491,
                        -899.3349413, 2036.063914, 1028.535064, -291.7464344, -498.9561957,
                        -497.4657134, 149.5003759, -1118.815421, 62.57620478},
                {740.9337755, 108.5426395, 162.3597052, -98.55122038, 351.9592518, -1588.468658,
                        915.5581279, -595.196204, -695.223341, -687.0126441, 653.1795742,
                        -1644.452985, 1028.535064, 5992.533309, -1045.018061, -714.9630989,
                        -404.9392971, 773.0232886, -1038.922079, 553.5909559},
                {194.8507207, 1010.300195, 150.6347092, 44.8800809, 1368.70192, -91.7627839,
                        -231.5381304, 1738.310956, 1640.020573, 919.4059893, 176.0395102,
                        891.2077462, -291.7464344, -1045.018061, 4354.118504, -400.2315445,
                        -1099.101969, -156.3953706, 205.7109358, 809.0341982},
                {-212.612644, 181.3146389, 696.1542498, 243.2613568, 256.0425573, 1438.61345,
                        -1270.020176, 485.8474989, 1188.493562, 375.4680045, -799.2756642,
                        255.800972, -498.9561957, -714.9630989, -400.2315445, 3110.438277,
                        367.5552617, 111.7516927, -752.458078, -538.0752549},
                {523.9584425, -3445.479737, 2060.45654, 174.8469166, -1247.585607, 644.0338574,
                        1215.915369, -910.5809417, -1088.926741, 771.1535868, 825.2518195,
                        456.6066048, -497.4657134, -404.9392971, -1099.101969, 367.5552617,
                        9904.570524, 1461.740394, -222.7878862, -245.6970234},
                {202.0551012, -1674.505896, 50.4969518, 499.6531378, -470.487452, -300.8118586,
                        -52.0939731, 32.63691136, -387.5314609, 608.9142074, 362.7876138,
                        -105.004444, 149.5003759, 773.0232886, -156.3953706, 111.7516927,
                        1461.740394, 1718.61238, 223.6858082, -23.59906994},
                {1340.250055, -52.19964763, 55.06493948, -548.915649, -136.665057, -1315.859183,
                        -1695.432391, -137.324335, -1461.346485, 34.61837495, 1373.647146,
                        1331.866803, -1118.815421, -1038.922079, 205.7109358, -752.458078,
                        -222.7878862, 223.6858082, 5289.029815, 812.2123795},
                {691.9020468, 588.0228597, -99.98809961, -204.0789017, -200.1971039, 237.0102823,
                        -19.32622407, 267.707855, -532.0513303, 505.2205011, -135.7935899,
                        -562.0645647, 62.57620478, 553.5909559, 809.0341982, -538.0752549,
                        -245.6970234, -23.59906994, 812.2123795, 1268.272218}};

        final double[][] actual = reader.readTargetVariance(reader.readTargets());

        for (int i = 0; i < expected.length; i++)
        {
            for (int j = 0; j < expected[i].length; j++)
            {
                assertEquals(expected[i][j], actual[i][j], 0.00001);
            }
        }
    }

    @Test
    public void testReadCoeffs()
    {
        final List<Coefficient> coeffs = reader.readCoeffs();
        assertEquals(11, coeffs.size());

        assertEquals(SpaceTypeCoefficient.getNoChangeConst(1), coeffs.get(0));
        assertEquals(SpaceTypeCoefficient.getDemolishTransitionConst(2), coeffs.get(1));
        assertEquals(SpaceTypeCoefficient.getDerelictTransitionConst(3), coeffs.get(2));
        assertEquals(SpaceTypeCoefficient.getRenovateTransitionConst(4), coeffs.get(3));
        assertEquals(SpaceTypeCoefficient.getRenovateDerelictConst(5), coeffs.get(4));
        assertEquals(SpaceTypeCoefficient.getAddTransitionConst(6), coeffs.get(5));
        assertEquals(SpaceTypeCoefficient.getNewFromTransitionConst(7), coeffs.get(6));
        assertEquals(SpaceTypeCoefficient.getNoChangeDisp(8), coeffs.get(7));
        assertEquals(SpaceTypeCoefficient.getChangeOptionsDisp(9), coeffs.get(8));
        assertEquals(SpaceTypeCoefficient.getDemolishDerelictDisp(10), coeffs.get(9));
        assertEquals(SpaceTypeCoefficient.getRenovateAddNewDisp(11), coeffs.get(10));

        final double[] means = reader.readPriorMeans(coeffs);
        final double[] expected = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        for (int i = 0; i < means.length; i++)
        {
            assertEquals(expected[i], means[i], 0.00001);
        }
    }

    @Test
    public void testReadPriorVariance()
    {
        final double[][] expected = {
                {5692.05569, 168.5590372, 180.0028463, 2258.603439, -149.5245724, 431.1995136,
                        312.5100047, 679.3540565, 28.31513324, 2194.864372, 318.3385488},
                {168.5590372, 1762.236966, -66.70662323, 43.77163127, -4.017820458, 925.8082053,
                        111.5099105, 66.33452141, -1.078024019, 348.8506594, -37.90224574},
                {180.0028463, -66.70662323, 753.2937381, 125.4881497, -32.1852307, -243.6730692,
                        -20.9208297, 55.33248319, -1450.29631, 6.748369247, -303.6008081},
                {2258.603439, 43.77163127, 125.4881497, 3801.804222, 14.69378346, 354.4061672,
                        856.6727322, 730.3123023, -926.0641933, 1874.736017, -195.5375654},
                {-149.5245724, -4.017820458, -32.1852307, 14.69378346, 67.98402727, 28.10470235,
                        39.69703716, -10.68924375, 23.16038926, -41.90902355, -154.3163121},
                {431.1995136, 925.8082053, -243.6730692, 354.4061672, 28.10470235, 3823.986068,
                        -162.3289076, -202.3286526, 370.4667441, 348.5549896, -1065.196076},
                {312.5100047, 111.5099105, -20.9208297, 856.6727322, 39.69703716, -162.3289076,
                        329.9322502, 335.9686806, -186.4580379, 510.2406516, -72.9841178},
                {679.3540565, 66.33452141, 55.33248319, 730.3123023, -10.68924375, -202.3286526,
                        335.9686806, 899.7664101, -175.8524246, 745.7400946, -206.1348755},
                {28.31513324, -1.078024019, -1450.29631, -926.0641933, 23.16038926, 370.4667441,
                        -186.4580379, -175.8524246, 3930.679064, -143.8599562, 768.9818936},
                {2194.864372, 348.8506594, 6.748369247, 1874.736017, -41.90902355, 348.5549896,
                        510.2406516, 745.7400946, -143.8599562, 1497.759361, -4.687653855},
                {318.3385488, -37.90224574, -303.6008081, -195.5375654, -154.3163121, -1065.196076,
                        -72.9841178, -206.1348755, 768.9818936, -4.687653855, 1183.722723}};

        final double[][] actual = reader.readPriorVariance(reader.readCoeffs());

        for (int i = 0; i < expected.length; i++)
        {
            for (int j = 0; j < expected[i].length; j++)
            {
                assertEquals(expected[i][j], actual[i][j], 0.00001);
            }
        }
    }

}
