package com.hbaspecto.pecas.sd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hbaspecto.discreteChoiceModelling.Alternative;
import com.hbaspecto.discreteChoiceModelling.Coefficient;
import com.hbaspecto.discreteChoiceModelling.LogitModel;
import com.hbaspecto.pecas.ChoiceModelOverflowException;
import com.hbaspecto.pecas.NoAlternativeAvailable;
import com.hbaspecto.pecas.land.LandInventory;
import com.hbaspecto.pecas.land.PostgreSQLLandInventory;
import com.hbaspecto.pecas.land.SimpleORMLandInventory;
import com.hbaspecto.pecas.sd.estimation.ExpectedValue;
import com.hbaspecto.pecas.sd.estimation.RedevelopmentIntoSpaceTypeTarget;
import com.hbaspecto.pecas.sd.estimation.SpaceTypeCoefficient;
import com.hbaspecto.pecas.sd.estimation.SpaceTypeIntensityTarget;
import com.hbaspecto.pecas.sd.estimation.SpaceTypeTAZTarget;
import com.hbaspecto.pecas.sd.estimation.TransitionConstant;
import com.pb.common.util.ResourceUtil;

@Ignore
// TODO: Fix test
public class TestDevelopNewAlternative {
	private static LandInventory land;
	private static List<List<Alternative>> alts;
	private static double disp;

	private static String str(boolean b) {
		if (b) {
			return "TRUE";
		}
		else {
			return "FALSE";
		}
	}

	private static void setUpTestInventory(String url, String user,
			String password) throws Exception {
		final Connection conn = DriverManager.getConnection(url, user, password);
		final Statement statement = conn.createStatement();

		final int[] parcelnum = { 1, 2, 3, 4, 5, 6, 7, 8 };
		final String[] parcelid = { "1", "2", "3", "4", "5", "6", "7", "8" };
		final int[] yearbuilt = { 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000 };
		final int[] taz = { 11, 11, 12, 12, 21, 21, 22, 22 };
		final int[] spacetype = { 3, 3, 5, 5, 3, 5, 3, 95 };
		final double[] quantity = { 99000, 399000, 49500, 199500, 79200, 319200,
				33000, 0 };
		final double[] landarea = { 215496, 215496, 107748, 107748, 172397, 172397,
				71832, 71832 };
		final boolean[] derelict = { false, false, false, false, false, false,
				true, true };
		final boolean[] brownfield = { false, false, false, false, false, false,
				false, false };

		for (int i = 0; i < parcelnum.length; i++) {
			statement.execute("UPDATE parcels " + "SET parcel_id='" + parcelid[i]
					+ "', year_built=" + yearbuilt[i] + ", taz=" + taz[i]
					+ ", space_type_id=" + spacetype[i] + ", space_quantity="
					+ quantity[i] + ", land_area=" + landarea[i] + ", is_derelict="
					+ str(derelict[i]) + ", is_brownfield=" + str(brownfield[i])
					+ " WHERE pecas_parcel_num=" + parcelnum[i]);
		}

		// Spacetype constants.
		final int[] spacetypenum = { 3, 5, 95 };
		final double[] newconst = { -491, -109, -539 };
		final double[] addconst = { -589, -11.3, -1E+99 };
		final double[] renoconst = { -262, -18.8, -1E+99 };
		final double[] democonst = { -455, -35, -1E+99 };
		final double[] derelictconst = { -100, -100, -1E+99 };
		final double[] nochangeconst = { 0, 0, 0 };
		final double[] newtypedisp = { 0.02, 0.4, 0.1 };
		final double[] gydisp = { 0.02, 0.4, 0.1 };
		final double[] gzdisp = { 0.02, 0.4, 0.1 };
		final double[] gwdisp = { 0.02, 0.4, 0.1 };
		final double[] gkdisp = { 0.02, 0.4, 0.1 };
		final double[] nochangedisp = { 0.02, 0.4, 0.1 };
		final double[] intensitydisp = { 0.04, 0.5, 0.2 };
		final double[] steppoint = { 4.8, 3.6, 0.0 };
		final double[] belowstep = { 55.3, 70, 0.0 };
		final double[] abovestep = { -13, -22, 0.0 };
		final double[] stepamount = { 20, 15, 0.0 };
		final double[] minfar = { 0.5, 0.0, 0.0 };
		final double[] maxfar = { 20, 15, 0.0 };

		for (int i = 0; i < spacetypenum.length; i++) {
			statement.execute("UPDATE space_types_i " + "SET new_transition_const="
					+ newconst[i] + ", add_transition_const=" + addconst[i]
					+ ", renovate_transition_const=" + renoconst[i]
					+ ", renovate_derelict_const=" + renoconst[i]
					+ ", demolish_transition_const=" + democonst[i]
					+ ", derelict_transition_const=" + derelictconst[i]
					+ ", no_change_transition_const=" + nochangeconst[i]
					+ ", new_type_dispersion_parameter=" + newtypedisp[i]
					+ ", gy_dispersion_parameter=" + gydisp[i]
					+ ", gz_dispersion_parameter=" + gzdisp[i]
					+ ", gw_dispersion_parameter=" + gwdisp[i]
					+ ", gk_dispersion_parameter=" + gkdisp[i]
					+ ", nochange_dispersion_parameter=" + nochangedisp[i]
					+ ", intensity_dispersion_parameter=" + intensitydisp[i]
					+ ", step_point=" + steppoint[i] + ", below_step_point_adjustment="
					+ belowstep[i] + ", above_step_point_adjustment=" + abovestep[i]
					+ ", step_point_adjustment=" + stepamount[i] + ", min_intensity="
					+ minfar[i] + ", max_intensity=" + maxfar[i]
					+ " WHERE space_type_id=" + spacetypenum[i]);
		}

		final int[] spacetypenv = { 3, 5 };
		final double[][] transition = { { -86.3, 122 }, { -15.9, 95.8 },
				{ 376, 490 } };

		for (int i = 0; i < spacetypenum.length; i++) {
			for (int j = 0; j < spacetypenv.length; j++) {
				statement.execute("UPDATE transition_constants_i "
						+ "SET transition_constant=" + transition[i][j]
						+ " WHERE from_space_type_id=" + spacetypenum[i]
						+ " AND to_space_type_id=" + spacetypenv[j]);
			}
		}

		conn.close();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final ResourceBundle rb = ResourceUtil.getResourceBundle("sd");

		final String url = ResourceUtil.checkAndGetProperty(rb, "InputDatabase");
		final String user = ResourceUtil.checkAndGetProperty(rb,
				"InputDatabaseUser");
		final String password = ResourceUtil.checkAndGetProperty(rb,
				"InputDatabasePassword");

		setUpTestInventory(url, user, password);

		final String driver = ResourceUtil.checkAndGetProperty(rb,
				"InputJDBCDriver");
		final String schema = null;

		SimpleORMLandInventory.prepareSimpleORMSession(rb);
		final SimpleORMLandInventory sormland = new PostgreSQLLandInventory();
		sormland.setDatabaseConnectionParameter(rb, driver, url, user, password,
				schema);
		land = sormland;
		ZoningRulesI.land = land;
		land.init(2005);
		land.setToBeforeFirst();

		// Set up alternatives.
		int i = 0;
		alts = new ArrayList<List<Alternative>>();
		while (land.advanceToNext()) {
			final ZoningRulesI zoning = ZoningRulesI.getZoningRuleByZoningRulesCode(
					land.getSession(), land.getZoningRulesCode());

			final double interestRate = 0.0722;
			final double compounded = Math.pow(1 + interestRate, 30);
			final double amortizationFactor = interestRate * compounded
					/ (compounded - 1);
			ZoningRulesI.amortizationFactor = ResourceUtil.getDoubleProperty(rb,
					"AmortizationFactor", amortizationFactor);
			ZoningRulesI.servicingCostPerUnit = ResourceUtil.getDoubleProperty(rb,
					"ServicingCostPerUnit", 13.76);

			final Method getmodel = ZoningRulesI.class
					.getDeclaredMethod("getMyLogitModel");
			getmodel.setAccessible(true);
			LogitModel model = (LogitModel) getmodel.invoke(zoning);
			model = (LogitModel) model.getAlternatives().get(0);
			model = (LogitModel) model.getAlternatives().get(1);
			model = (LogitModel) model.getAlternatives().get(0);
			model = (LogitModel) model.getAlternatives().get(0);
			alts.add(model.getAlternatives());
			disp = model.getDispersionParameter();
			i++;
		}
	}

	@Test
	public void testGetUtility() {
		land.setToBeforeFirst();
		// Parcel 1 - low-density commercial.
		land.advanceToNext();
		double utility;
		try {
			utility = ((DevelopNewAlternative) alts.get(0).get(0)).getUtility(disp);
			assertEquals(-107.3574, utility, 0.001);
			// Parcel 2 - high-density commercial.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(1).get(0)).getUtility(disp);
			assertEquals(-54.6448, utility, 0.001);
			// Parcel 3 - low-density residential.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(2).get(0)).getUtility(disp);
			assertEquals(93.6918, utility, 0.001);
			// Parcel 4 - high-density residential.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(3).get(0)).getUtility(disp);
			assertEquals(94.6957, utility, 0.001);
			// Parcel 5 - develop commercial on mixed-use.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(4).get(0)).getUtility(disp);
			assertEquals(-36.3925, utility, 0.001);
			// Parcel 5 - develop residential on mixed-use.
			utility = ((DevelopNewAlternative) alts.get(4).get(1)).getUtility(disp);
			assertEquals(136.3158, utility, 0.001);
			// Parcel 6 - historical - can't build.
			land.advanceToNext();
			assertEquals(0, alts.get(5).size());
			// Parcel 7 - derelict.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(6).get(0)).getUtility(disp);
			assertEquals(-26.4240, utility, 0.001);
			// Parcel 8 - vacant.
			land.advanceToNext();
			utility = ((DevelopNewAlternative) alts.get(7).get(0)).getUtility(disp);
			assertEquals(505.1614, utility, 0.001);
		}
		catch (final ChoiceModelOverflowException e) {
			// TODO Auto-generated catch block
			fail("Overflow exception");
		}
	}

	@Test
	public void testGetExpectedTargetValues() throws NoAlternativeAvailable,
			ChoiceModelOverflowException {
		final List<ExpectedValue> targets = new ArrayList<ExpectedValue>();
		targets.add(new SpaceTypeTAZTarget(11, 3));
		targets.add(new SpaceTypeTAZTarget(11, 5));
		targets.add(new SpaceTypeTAZTarget(12, 3));
		targets.add(new SpaceTypeTAZTarget(12, 5));
		targets.add(new SpaceTypeTAZTarget(21, 3));
		targets.add(new SpaceTypeTAZTarget(21, 5));
		targets.add(new SpaceTypeTAZTarget(22, 3));
		targets.add(new SpaceTypeTAZTarget(22, 5));
		targets.add(new RedevelopmentIntoSpaceTypeTarget(3));
		targets.add(new RedevelopmentIntoSpaceTypeTarget(5));
		targets.addAll(new SpaceTypeIntensityTarget(3)
				.getAssociatedExpectedValues());
		targets.addAll(new SpaceTypeIntensityTarget(5)
				.getAssociatedExpectedValues());

		land.setToBeforeFirst();
		// Parcel 1 - low-density commercial.
		land.advanceToNext();
		Vector expvalues = ((DevelopNewAlternative) alts.get(0).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(160882, expvalues.get(0), 1);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(160882, expvalues.get(8), 1);
		assertEquals(0.0, expvalues.get(9), 0.00001);
		assertEquals(0.746564, expvalues.get(10), 0.00001);
		assertEquals(1.0, expvalues.get(11), 0.00001);
		assertEquals(0.0, expvalues.get(12), 0.00001);
		assertEquals(0.0, expvalues.get(13), 0.00001);
		// Parcel 2 - high-density commercial.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(1).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(960749, expvalues.get(0), 1);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(960749, expvalues.get(8), 1);
		assertEquals(0.0, expvalues.get(9), 0.00001);
		assertEquals(4.4583135, expvalues.get(10), 0.00001);
		assertEquals(1.0, expvalues.get(11), 0.00001);
		assertEquals(0.0, expvalues.get(12), 0.00001);
		assertEquals(0.0, expvalues.get(13), 0.00001);
		// Parcel 3 - low-density residential.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(2).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(25574, expvalues.get(3), 1);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(0.0, expvalues.get(8), 0.00001);
		assertEquals(25574, expvalues.get(9), 1);
		assertEquals(0.0, expvalues.get(10), 0.00001);
		assertEquals(0.0, expvalues.get(11), 0.00001);
		assertEquals(0.237353, expvalues.get(12), 0.00001);
		assertEquals(1.0, expvalues.get(13), 0.00001);
		// Parcel 4 - high-density residential.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(3).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(183279, expvalues.get(3), 1);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(0.0, expvalues.get(8), 0.00001);
		assertEquals(183279, expvalues.get(9), 1);
		assertEquals(0.0, expvalues.get(10), 0.00001);
		assertEquals(0.0, expvalues.get(11), 0.00001);
		assertEquals(1.700997, expvalues.get(12), 0.00001);
		assertEquals(1.0, expvalues.get(13), 0.00001);
		// Parcel 5 - develop commercial on mixed-use.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(4).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(683738, expvalues.get(4), 1);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(683738, expvalues.get(8), 1);
		assertEquals(0.0, expvalues.get(9), 0.00001);
		assertEquals(3.966066, expvalues.get(10), 0.00001);
		assertEquals(1.0, expvalues.get(11), 0.00001);
		assertEquals(0.0, expvalues.get(12), 0.00001);
		assertEquals(0.0, expvalues.get(13), 0.00001);
		// Parcel 5 - develop residential on mixed-use.
		expvalues = ((DevelopNewAlternative) alts.get(4).get(1))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(662487, expvalues.get(5), 1);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(0.0, expvalues.get(8), 0.00001);
		assertEquals(662487, expvalues.get(9), 1);
		assertEquals(0.0, expvalues.get(10), 0.00001);
		assertEquals(0.0, expvalues.get(11), 0.00001);
		assertEquals(3.842796, expvalues.get(12), 0.00001);
		assertEquals(1.0, expvalues.get(13), 0.00001);
		// Parcel 6 - historical - can't build.
		land.advanceToNext();
		// Parcel 7 - derelict.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(6).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(475342, expvalues.get(6), 1);
		assertEquals(0.0, expvalues.get(7), 0.00001);
		assertEquals(475342, expvalues.get(8), 1);
		assertEquals(0.0, expvalues.get(9), 0.00001);
		assertEquals(6.617406, expvalues.get(10), 0.00001);
		assertEquals(1.0, expvalues.get(11), 0.00001);
		assertEquals(0.0, expvalues.get(12), 0.00001);
		assertEquals(0.0, expvalues.get(13), 0.00001);
		// Parcel 8 - vacant.
		land.advanceToNext();
		expvalues = ((DevelopNewAlternative) alts.get(7).get(0))
				.getExpectedTargetValues(targets);
		assertEquals(0.0, expvalues.get(0), 0.00001);
		assertEquals(0.0, expvalues.get(1), 0.00001);
		assertEquals(0.0, expvalues.get(2), 0.00001);
		assertEquals(0.0, expvalues.get(3), 0.00001);
		assertEquals(0.0, expvalues.get(4), 0.00001);
		assertEquals(0.0, expvalues.get(5), 0.00001);
		assertEquals(0.0, expvalues.get(6), 0.00001);
		assertEquals(312770, expvalues.get(7), 1);
		assertEquals(0.0, expvalues.get(8), 0.00001);
		assertEquals(0.0, expvalues.get(9), 0.00001);
		assertEquals(0.0, expvalues.get(10), 0.00001);
		assertEquals(0.0, expvalues.get(11), 0.00001);
		assertEquals(4.354188, expvalues.get(12), 0.00001);
		assertEquals(1.0, expvalues.get(13), 0.00001);
	}

	@Test
	public void testGetUtilityDerivativeWRTParameters()
			throws NoAlternativeAvailable, ChoiceModelOverflowException {
		final List<Coefficient> coeffs = new ArrayList<Coefficient>();
		coeffs.add(SpaceTypeCoefficient.getStepPoint(3));
		coeffs.add(SpaceTypeCoefficient.getBelowStepPointAdj(3));
		coeffs.add(SpaceTypeCoefficient.getAboveStepPointAdj(3));
		coeffs.add(SpaceTypeCoefficient.getStepPointAmount(3));
		coeffs.add(SpaceTypeCoefficient.getIntensityDisp(3));
		coeffs.add(SpaceTypeCoefficient.getStepPoint(5));
		coeffs.add(SpaceTypeCoefficient.getBelowStepPointAdj(5));
		coeffs.add(SpaceTypeCoefficient.getAboveStepPointAdj(5));
		coeffs.add(SpaceTypeCoefficient.getStepPointAmount(5));
		coeffs.add(SpaceTypeCoefficient.getIntensityDisp(5));
		coeffs.add(TransitionConstant.getCoeff(3, 3));
		coeffs.add(TransitionConstant.getCoeff(3, 5));
		coeffs.add(TransitionConstant.getCoeff(5, 3));
		coeffs.add(TransitionConstant.getCoeff(5, 5));
		coeffs.add(TransitionConstant.getCoeff(95, 3));
		coeffs.add(TransitionConstant.getCoeff(95, 5));

		land.setToBeforeFirst();
		// Parcel 1 - low-density commercial.
		land.advanceToNext();
		Vector derivs = ((DevelopNewAlternative) alts.get(0).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(0.0, derivs.get(0), 0.00001);
		assertEquals(0.044794, derivs.get(1), 0.00001);
		assertEquals(0.0, derivs.get(2), 0.00001);
		assertEquals(0.0, derivs.get(3), 0.00001);
		assertEquals(433.394, derivs.get(4), 0.01);
		assertEquals(0.0, derivs.get(5), 0.00001);
		assertEquals(0.0, derivs.get(6), 0.00001);
		assertEquals(0.0, derivs.get(7), 0.00001);
		assertEquals(0.0, derivs.get(8), 0.00001);
		assertEquals(0.0, derivs.get(9), 0.00001);
		assertEquals(1.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 2 - high-density commercial.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(1).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(1.361625, derivs.get(0), 0.00001);
		assertEquals(0.208491, derivs.get(1), 0.00001);
		assertEquals(0.059008, derivs.get(2), 0.00001);
		assertEquals(0.022088, derivs.get(3), 0.00001);
		assertEquals(-1381.87, derivs.get(4), 0.1);
		assertEquals(0.0, derivs.get(5), 0.00001);
		assertEquals(0.0, derivs.get(6), 0.00001);
		assertEquals(0.0, derivs.get(7), 0.00001);
		assertEquals(0.0, derivs.get(8), 0.00001);
		assertEquals(0.0, derivs.get(9), 0.00001);
		assertEquals(1.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 3 - low-density residential.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(2).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(0.0, derivs.get(0), 0.00001);
		assertEquals(0.0, derivs.get(1), 0.00001);
		assertEquals(0.0, derivs.get(2), 0.00001);
		assertEquals(0.0, derivs.get(3), 0.00001);
		assertEquals(0.0, derivs.get(4), 0.00001);
		assertEquals(0.0, derivs.get(5), 0.00001);
		assertEquals(0.014241, derivs.get(6), 0.00001);
		assertEquals(0.0, derivs.get(7), 0.00001);
		assertEquals(0.0, derivs.get(8), 0.00001);
		assertEquals(2.787956, derivs.get(9), 0.00001);
		assertEquals(0.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(1.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 4 - high-density residential.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(3).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(0.0, derivs.get(0), 0.00001);
		assertEquals(0.0, derivs.get(1), 0.00001);
		assertEquals(0.0, derivs.get(2), 0.00001);
		assertEquals(0.0, derivs.get(3), 0.00001);
		assertEquals(0.0, derivs.get(4), 0.00001);
		assertEquals(0.148553, derivs.get(5), 0.00001);
		assertEquals(0.101200, derivs.get(6), 0.00001);
		assertEquals(0.000859, derivs.get(7), 0.00001);
		assertEquals(0.002895, derivs.get(8), 0.00001);
		assertEquals(-4.52141, derivs.get(9), 0.00001);
		assertEquals(0.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(1.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 5 - develop commercial on mixed-use.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(4).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(1.343148, derivs.get(0), 0.00001);
		assertEquals(0.209367, derivs.get(1), 0.00001);
		assertEquals(0.028597, derivs.get(2), 0.00001);
		assertEquals(0.022485, derivs.get(3), 0.00001);
		assertEquals(-1214.49, derivs.get(4), 0.1);
		assertEquals(0.0, derivs.get(5), 0.00001);
		assertEquals(0.0, derivs.get(6), 0.00001);
		assertEquals(0.0, derivs.get(7), 0.00001);
		assertEquals(0.0, derivs.get(8), 0.00001);
		assertEquals(0.0, derivs.get(9), 0.00001);
		assertEquals(1.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 5 - develop residential on mixed-use.
		derivs = ((DevelopNewAlternative) alts.get(4).get(1))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(0.0, derivs.get(0), 0.00001);
		assertEquals(0.0, derivs.get(1), 0.00001);
		assertEquals(0.0, derivs.get(2), 0.00001);
		assertEquals(0.0, derivs.get(3), 0.00001);
		assertEquals(0.0, derivs.get(4), 0.00001);
		assertEquals(3.267939, derivs.get(5), 0.00001);
		assertEquals(0.206762, derivs.get(6), 0.00001);
		assertEquals(0.023806, derivs.get(7), 0.00001);
		assertEquals(0.042389, derivs.get(8), 0.00001);
		assertEquals(-3.619530, derivs.get(9), 0.00001);
		assertEquals(0.0, derivs.get(10), 0.00001);
		assertEquals(1.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 6 - historical - can't build.
		land.advanceToNext();
		// Parcel 7 - derelict.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(6).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(2.435959, derivs.get(0), 0.00001);
		assertEquals(0.246306, derivs.get(1), 0.00001);
		assertEquals(0.150739, derivs.get(2), 0.00001);
		assertEquals(0.037558, derivs.get(3), 0.00001);
		assertEquals(-1606.66, derivs.get(4), 0.1);
		assertEquals(0.0, derivs.get(5), 0.00001);
		assertEquals(0.0, derivs.get(6), 0.00001);
		assertEquals(0.0, derivs.get(7), 0.00001);
		assertEquals(0.0, derivs.get(8), 0.00001);
		assertEquals(0.0, derivs.get(9), 0.00001);
		assertEquals(1.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(0.0, derivs.get(15), 0.00001);
		// Parcel 8 - vacant.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(7).get(0))
				.getUtilityDerivativesWRTParameters(coeffs);
		assertEquals(0.0, derivs.get(0), 0.00001);
		assertEquals(0.0, derivs.get(1), 0.00001);
		assertEquals(0.0, derivs.get(2), 0.00001);
		assertEquals(0.0, derivs.get(3), 0.00001);
		assertEquals(0.0, derivs.get(4), 0.00001);
		assertEquals(3.781225, derivs.get(5), 0.00001);
		assertEquals(0.208937, derivs.get(6), 0.00001);
		assertEquals(0.052314, derivs.get(7), 0.00001);
		assertEquals(0.046410, derivs.get(8), 0.00001);
		assertEquals(-5.903950, derivs.get(9), 0.00001);
		assertEquals(0.0, derivs.get(10), 0.00001);
		assertEquals(0.0, derivs.get(11), 0.00001);
		assertEquals(0.0, derivs.get(12), 0.00001);
		assertEquals(0.0, derivs.get(13), 0.00001);
		assertEquals(0.0, derivs.get(14), 0.00001);
		assertEquals(1.0, derivs.get(15), 0.00001);
	}

	@Test
	public void testGetExpectedTargetDerivativesWRTParameters()
			throws NoAlternativeAvailable, ChoiceModelOverflowException {
		final List<ExpectedValue> targets = new ArrayList<ExpectedValue>();
		targets.add(new SpaceTypeTAZTarget(11, 3));
		targets.add(new SpaceTypeTAZTarget(11, 5));
		targets.add(new SpaceTypeTAZTarget(12, 3));
		targets.add(new SpaceTypeTAZTarget(12, 5));
		targets.add(new SpaceTypeTAZTarget(21, 3));
		targets.add(new SpaceTypeTAZTarget(21, 5));
		targets.add(new SpaceTypeTAZTarget(22, 3));
		targets.add(new SpaceTypeTAZTarget(22, 5));
		targets.add(new RedevelopmentIntoSpaceTypeTarget(3));
		targets.add(new RedevelopmentIntoSpaceTypeTarget(5));
		targets.addAll(new SpaceTypeIntensityTarget(3)
				.getAssociatedExpectedValues());
		targets.addAll(new SpaceTypeIntensityTarget(5)
				.getAssociatedExpectedValues());

		final List<Coefficient> coeffs = new ArrayList<Coefficient>();
		coeffs.add(SpaceTypeCoefficient.getStepPoint(3));
		coeffs.add(SpaceTypeCoefficient.getBelowStepPointAdj(3));
		coeffs.add(SpaceTypeCoefficient.getAboveStepPointAdj(3));
		coeffs.add(SpaceTypeCoefficient.getStepPointAmount(3));
		coeffs.add(SpaceTypeCoefficient.getIntensityDisp(3));
		coeffs.add(SpaceTypeCoefficient.getStepPoint(5));
		coeffs.add(SpaceTypeCoefficient.getBelowStepPointAdj(5));
		coeffs.add(SpaceTypeCoefficient.getAboveStepPointAdj(5));
		coeffs.add(SpaceTypeCoefficient.getStepPointAmount(5));
		coeffs.add(SpaceTypeCoefficient.getIntensityDisp(5));

		land.setToBeforeFirst();
		// Parcel 1 - low-density commercial.
		land.advanceToNext();
		Matrix derivs = ((DevelopNewAlternative) alts.get(0).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(10.7711, derivs.get(0, 1), 0.001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(-18508, derivs.get(0, 4), 1);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(0.0, derivs.get(8, 0), 0.00001);
		assertEquals(10.7711, derivs.get(8, 1), 0.001);
		assertEquals(0.0, derivs.get(8, 2), 0.00001);
		assertEquals(0.0, derivs.get(8, 3), 0.00001);
		assertEquals(-18508, derivs.get(8, 4), 1);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(0.0, derivs.get(9, 6), 0.00001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(0.0, derivs.get(9, 9), 0.00001);
		assertEquals(0.0, derivs.get(10, 0), 0.00001);
		assertEquals(4.9983E-5, derivs.get(10, 1), 1.0E-9);
		assertEquals(0.0, derivs.get(10, 2), 0.00001);
		assertEquals(0.0, derivs.get(10, 3), 0.00001);
		assertEquals(-0.085887, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.0, derivs.get(12, 5), 0.00001);
		assertEquals(0.0, derivs.get(12, 6), 0.00001);
		assertEquals(0.0, derivs.get(12, 7), 0.00001);
		assertEquals(0.0, derivs.get(12, 8), 0.00001);
		assertEquals(0.0, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 2 - high-density commercial.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(1).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(38751, derivs.get(0, 0), 1);
		assertEquals(1588.8, derivs.get(0, 1), 0.1);
		assertEquals(2550.1, derivs.get(0, 2), 0.1);
		assertEquals(573.70, derivs.get(0, 3), 0.01);
		assertEquals(-1.11796E+7, derivs.get(0, 4), 100);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(38751, derivs.get(8, 0), 1);
		assertEquals(1588.8, derivs.get(8, 1), 0.1);
		assertEquals(2550.1, derivs.get(8, 2), 0.1);
		assertEquals(573.70, derivs.get(8, 3), 0.01);
		assertEquals(-1.11796E+7, derivs.get(8, 4), 100);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(0.0, derivs.get(9, 6), 0.00001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(0.0, derivs.get(9, 9), 0.00001);
		assertEquals(0.179821, derivs.get(10, 0), 0.00001);
		assertEquals(0.007373, derivs.get(10, 1), 0.00001);
		assertEquals(0.011834, derivs.get(10, 2), 0.00001);
		assertEquals(0.002662, derivs.get(10, 3), 0.00001);
		assertEquals(-51.8784, derivs.get(10, 4), 0.001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.0, derivs.get(12, 5), 0.00001);
		assertEquals(0.0, derivs.get(12, 6), 0.00001);
		assertEquals(0.0, derivs.get(12, 7), 0.00001);
		assertEquals(0.0, derivs.get(12, 8), 0.00001);
		assertEquals(0.0, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 3 - low-density residential.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(2).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(67.0325, derivs.get(3, 6), 0.001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(-2717.1, derivs.get(3, 9), 0.1);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(0.0, derivs.get(8, 0), 0.00001);
		assertEquals(0.0, derivs.get(8, 1), 0.00001);
		assertEquals(0.0, derivs.get(8, 2), 0.00001);
		assertEquals(0.0, derivs.get(8, 3), 0.00001);
		assertEquals(0.0, derivs.get(8, 4), 0.00001);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(67.0325, derivs.get(9, 6), 0.001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(-2717.1, derivs.get(9, 9), 0.1);
		assertEquals(0.0, derivs.get(10, 0), 0.00001);
		assertEquals(0.0, derivs.get(10, 1), 0.00001);
		assertEquals(0.0, derivs.get(10, 2), 0.00001);
		assertEquals(0.0, derivs.get(10, 3), 0.00001);
		assertEquals(0.0, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.0, derivs.get(12, 5), 0.00001);
		assertEquals(0.000622, derivs.get(12, 6), 0.00001);
		assertEquals(0.0, derivs.get(12, 7), 0.00001);
		assertEquals(0.0, derivs.get(12, 8), 0.00001);
		assertEquals(-0.025217, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 4 - high-density residential.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(3).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(19458, derivs.get(3, 5), 1);
		assertEquals(2769.4, derivs.get(3, 6), 0.1);
		assertEquals(115.43, derivs.get(3, 7), 0.01);
		assertEquals(342.46, derivs.get(3, 8), 0.01);
		assertEquals(-127897, derivs.get(3, 9), 1);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(0.0, derivs.get(8, 0), 0.00001);
		assertEquals(0.0, derivs.get(8, 1), 0.00001);
		assertEquals(0.0, derivs.get(8, 2), 0.00001);
		assertEquals(0.0, derivs.get(8, 3), 0.00001);
		assertEquals(0.0, derivs.get(8, 4), 0.00001);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(19458, derivs.get(9, 5), 1);
		assertEquals(2769.4, derivs.get(9, 6), 0.1);
		assertEquals(115.43, derivs.get(9, 7), 0.01);
		assertEquals(342.46, derivs.get(9, 8), 0.01);
		assertEquals(-127897, derivs.get(9, 9), 1);
		assertEquals(0.0, derivs.get(10, 0), 0.00001);
		assertEquals(0.0, derivs.get(10, 1), 0.00001);
		assertEquals(0.0, derivs.get(10, 2), 0.00001);
		assertEquals(0.0, derivs.get(10, 3), 0.00001);
		assertEquals(0.0, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.180588, derivs.get(12, 5), 0.00001);
		assertEquals(0.025703, derivs.get(12, 6), 0.00001);
		assertEquals(0.001071, derivs.get(12, 7), 0.00001);
		assertEquals(0.003178, derivs.get(12, 8), 0.00001);
		assertEquals(-1.18700, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 5 - develop commercial on mixed-use.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(4).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(21193, derivs.get(4, 0), 1);
		assertEquals(1082.5, derivs.get(4, 1), 0.1);
		assertEquals(508.90, derivs.get(4, 2), 0.01);
		assertEquals(326.51, derivs.get(4, 3), 0.01);
		assertEquals(-124818, derivs.get(4, 4), 1);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(21193, derivs.get(8, 0), 1);
		assertEquals(1082.5, derivs.get(8, 1), 0.1);
		assertEquals(508.90, derivs.get(8, 2), 0.01);
		assertEquals(326.51, derivs.get(8, 3), 0.01);
		assertEquals(-124818, derivs.get(8, 4), 1);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(0.0, derivs.get(9, 6), 0.00001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(0.0, derivs.get(9, 9), 0.00001);
		assertEquals(0.122932, derivs.get(10, 0), 0.00001);
		assertEquals(0.006279, derivs.get(10, 1), 0.00001);
		assertEquals(0.002952, derivs.get(10, 2), 0.00001);
		assertEquals(0.001894, derivs.get(10, 3), 0.00001);
		assertEquals(-0.724012, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.0, derivs.get(12, 5), 0.00001);
		assertEquals(0.0, derivs.get(12, 6), 0.00001);
		assertEquals(0.0, derivs.get(12, 7), 0.00001);
		assertEquals(0.0, derivs.get(12, 8), 0.00001);
		assertEquals(0.0, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 5 - develop residential on mixed-use.
		derivs = ((DevelopNewAlternative) alts.get(4).get(1))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(120393, derivs.get(5, 5), 1);
		assertEquals(1013.4, derivs.get(5, 6), 0.1);
		assertEquals(1209.4, derivs.get(5, 7), 0.1);
		assertEquals(1164.9, derivs.get(5, 8), 0.1);
		assertEquals(92788, derivs.get(5, 9), 1);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(0.0, derivs.get(8, 0), 0.00001);
		assertEquals(0.0, derivs.get(8, 1), 0.00001);
		assertEquals(0.0, derivs.get(8, 2), 0.00001);
		assertEquals(0.0, derivs.get(8, 3), 0.00001);
		assertEquals(0.0, derivs.get(8, 4), 0.00001);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(120393, derivs.get(9, 5), 1);
		assertEquals(1013.4, derivs.get(9, 6), 0.1);
		assertEquals(1209.4, derivs.get(9, 7), 0.1);
		assertEquals(1164.9, derivs.get(9, 8), 0.1);
		assertEquals(92788, derivs.get(9, 9), 1);
		assertEquals(0.0, derivs.get(10, 0), 0.00001);
		assertEquals(0.0, derivs.get(10, 1), 0.00001);
		assertEquals(0.0, derivs.get(10, 2), 0.00001);
		assertEquals(0.0, derivs.get(10, 3), 0.00001);
		assertEquals(0.0, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.698345, derivs.get(12, 5), 0.00001);
		assertEquals(0.005878, derivs.get(12, 6), 0.00001);
		assertEquals(0.007015, derivs.get(12, 7), 0.00001);
		assertEquals(0.006757, derivs.get(12, 8), 0.00001);
		assertEquals(0.538220, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 6 - historical - can't build.
		land.advanceToNext();
		// Parcel 7 - derelict.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(6).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(16861, derivs.get(6, 0), 1);
		assertEquals(517.82, derivs.get(6, 1), 0.01);
		assertEquals(1811.2, derivs.get(6, 2), 0.1);
		assertEquals(236.99, derivs.get(6, 3), 0.01);
		assertEquals(-2.12399E+6, derivs.get(6, 4), 10);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(0.0, derivs.get(7, 5), 0.00001);
		assertEquals(0.0, derivs.get(7, 6), 0.00001);
		assertEquals(0.0, derivs.get(7, 7), 0.00001);
		assertEquals(0.0, derivs.get(7, 8), 0.00001);
		assertEquals(0.0, derivs.get(7, 9), 0.00001);
		assertEquals(16861, derivs.get(8, 0), 1);
		assertEquals(517.82, derivs.get(8, 1), 0.01);
		assertEquals(1811.2, derivs.get(8, 2), 0.1);
		assertEquals(236.99, derivs.get(8, 3), 0.01);
		assertEquals(-2.12399E+6, derivs.get(8, 4), 10);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(0.0, derivs.get(9, 6), 0.00001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(0.0, derivs.get(9, 9), 0.00001);
		assertEquals(0.234733, derivs.get(10, 0), 0.00001);
		assertEquals(0.007209, derivs.get(10, 1), 0.00001);
		assertEquals(0.025214, derivs.get(10, 2), 0.00001);
		assertEquals(0.003299, derivs.get(10, 3), 0.00001);
		assertEquals(-29.5689, derivs.get(10, 4), 0.001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.0, derivs.get(12, 5), 0.00001);
		assertEquals(0.0, derivs.get(12, 6), 0.00001);
		assertEquals(0.0, derivs.get(12, 7), 0.00001);
		assertEquals(0.0, derivs.get(12, 8), 0.00001);
		assertEquals(0.0, derivs.get(12, 9), 0.00001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
		// Parcel 8 - vacant.
		land.advanceToNext();
		derivs = ((DevelopNewAlternative) alts.get(7).get(0))
				.getExpectedTargetDerivativesWRTParameters(targets, coeffs);
		assertEquals(0.0, derivs.get(0, 0), 0.00001);
		assertEquals(0.0, derivs.get(0, 1), 0.00001);
		assertEquals(0.0, derivs.get(0, 2), 0.00001);
		assertEquals(0.0, derivs.get(0, 3), 0.00001);
		assertEquals(0.0, derivs.get(0, 4), 0.00001);
		assertEquals(0.0, derivs.get(0, 5), 0.00001);
		assertEquals(0.0, derivs.get(0, 6), 0.00001);
		assertEquals(0.0, derivs.get(0, 7), 0.00001);
		assertEquals(0.0, derivs.get(0, 8), 0.00001);
		assertEquals(0.0, derivs.get(0, 9), 0.00001);
		assertEquals(0.0, derivs.get(1, 0), 0.00001);
		assertEquals(0.0, derivs.get(1, 1), 0.00001);
		assertEquals(0.0, derivs.get(1, 2), 0.00001);
		assertEquals(0.0, derivs.get(1, 3), 0.00001);
		assertEquals(0.0, derivs.get(1, 4), 0.00001);
		assertEquals(0.0, derivs.get(1, 5), 0.00001);
		assertEquals(0.0, derivs.get(1, 6), 0.00001);
		assertEquals(0.0, derivs.get(1, 7), 0.00001);
		assertEquals(0.0, derivs.get(1, 8), 0.00001);
		assertEquals(0.0, derivs.get(1, 9), 0.00001);
		assertEquals(0.0, derivs.get(2, 0), 0.00001);
		assertEquals(0.0, derivs.get(2, 1), 0.00001);
		assertEquals(0.0, derivs.get(2, 2), 0.00001);
		assertEquals(0.0, derivs.get(2, 3), 0.00001);
		assertEquals(0.0, derivs.get(2, 4), 0.00001);
		assertEquals(0.0, derivs.get(2, 5), 0.00001);
		assertEquals(0.0, derivs.get(2, 6), 0.00001);
		assertEquals(0.0, derivs.get(2, 7), 0.00001);
		assertEquals(0.0, derivs.get(2, 8), 0.00001);
		assertEquals(0.0, derivs.get(2, 9), 0.00001);
		assertEquals(0.0, derivs.get(3, 0), 0.00001);
		assertEquals(0.0, derivs.get(3, 1), 0.00001);
		assertEquals(0.0, derivs.get(3, 2), 0.00001);
		assertEquals(0.0, derivs.get(3, 3), 0.00001);
		assertEquals(0.0, derivs.get(3, 4), 0.00001);
		assertEquals(0.0, derivs.get(3, 5), 0.00001);
		assertEquals(0.0, derivs.get(3, 6), 0.00001);
		assertEquals(0.0, derivs.get(3, 7), 0.00001);
		assertEquals(0.0, derivs.get(3, 8), 0.00001);
		assertEquals(0.0, derivs.get(3, 9), 0.00001);
		assertEquals(0.0, derivs.get(4, 0), 0.00001);
		assertEquals(0.0, derivs.get(4, 1), 0.00001);
		assertEquals(0.0, derivs.get(4, 2), 0.00001);
		assertEquals(0.0, derivs.get(4, 3), 0.00001);
		assertEquals(0.0, derivs.get(4, 4), 0.00001);
		assertEquals(0.0, derivs.get(4, 5), 0.00001);
		assertEquals(0.0, derivs.get(4, 6), 0.00001);
		assertEquals(0.0, derivs.get(4, 7), 0.00001);
		assertEquals(0.0, derivs.get(4, 8), 0.00001);
		assertEquals(0.0, derivs.get(4, 9), 0.00001);
		assertEquals(0.0, derivs.get(5, 0), 0.00001);
		assertEquals(0.0, derivs.get(5, 1), 0.00001);
		assertEquals(0.0, derivs.get(5, 2), 0.00001);
		assertEquals(0.0, derivs.get(5, 3), 0.00001);
		assertEquals(0.0, derivs.get(5, 4), 0.00001);
		assertEquals(0.0, derivs.get(5, 5), 0.00001);
		assertEquals(0.0, derivs.get(5, 6), 0.00001);
		assertEquals(0.0, derivs.get(5, 7), 0.00001);
		assertEquals(0.0, derivs.get(5, 8), 0.00001);
		assertEquals(0.0, derivs.get(5, 9), 0.00001);
		assertEquals(0.0, derivs.get(6, 0), 0.00001);
		assertEquals(0.0, derivs.get(6, 1), 0.00001);
		assertEquals(0.0, derivs.get(6, 2), 0.00001);
		assertEquals(0.0, derivs.get(6, 3), 0.00001);
		assertEquals(0.0, derivs.get(6, 4), 0.00001);
		assertEquals(0.0, derivs.get(6, 5), 0.00001);
		assertEquals(0.0, derivs.get(6, 6), 0.00001);
		assertEquals(0.0, derivs.get(6, 7), 0.00001);
		assertEquals(0.0, derivs.get(6, 8), 0.00001);
		assertEquals(0.0, derivs.get(6, 9), 0.00001);
		assertEquals(0.0, derivs.get(7, 0), 0.00001);
		assertEquals(0.0, derivs.get(7, 1), 0.00001);
		assertEquals(0.0, derivs.get(7, 2), 0.00001);
		assertEquals(0.0, derivs.get(7, 3), 0.00001);
		assertEquals(0.0, derivs.get(7, 4), 0.00001);
		assertEquals(70437, derivs.get(7, 5), 1);
		assertEquals(446.14, derivs.get(7, 6), 0.01);
		assertEquals(2647.2, derivs.get(7, 7), 0.1);
		assertEquals(621.80, derivs.get(7, 8), 0.01);
		assertEquals(-78257, derivs.get(7, 9), 1);
		assertEquals(0.0, derivs.get(8, 0), 0.00001);
		assertEquals(0.0, derivs.get(8, 1), 0.00001);
		assertEquals(0.0, derivs.get(8, 2), 0.00001);
		assertEquals(0.0, derivs.get(8, 3), 0.00001);
		assertEquals(0.0, derivs.get(8, 4), 0.00001);
		assertEquals(0.0, derivs.get(8, 5), 0.00001);
		assertEquals(0.0, derivs.get(8, 6), 0.00001);
		assertEquals(0.0, derivs.get(8, 7), 0.00001);
		assertEquals(0.0, derivs.get(8, 8), 0.00001);
		assertEquals(0.0, derivs.get(8, 9), 0.00001);
		assertEquals(0.0, derivs.get(9, 0), 0.00001);
		assertEquals(0.0, derivs.get(9, 1), 0.00001);
		assertEquals(0.0, derivs.get(9, 2), 0.00001);
		assertEquals(0.0, derivs.get(9, 3), 0.00001);
		assertEquals(0.0, derivs.get(9, 4), 0.00001);
		assertEquals(0.0, derivs.get(9, 5), 0.00001);
		assertEquals(0.0, derivs.get(9, 6), 0.00001);
		assertEquals(0.0, derivs.get(9, 7), 0.00001);
		assertEquals(0.0, derivs.get(9, 8), 0.00001);
		assertEquals(0.0, derivs.get(9, 9), 0.00001);
		assertEquals(0.0, derivs.get(10, 0), 0.00001);
		assertEquals(0.0, derivs.get(10, 1), 0.00001);
		assertEquals(0.0, derivs.get(10, 2), 0.00001);
		assertEquals(0.0, derivs.get(10, 3), 0.00001);
		assertEquals(0.0, derivs.get(10, 4), 0.00001);
		assertEquals(0.0, derivs.get(10, 5), 0.00001);
		assertEquals(0.0, derivs.get(10, 6), 0.00001);
		assertEquals(0.0, derivs.get(10, 7), 0.00001);
		assertEquals(0.0, derivs.get(10, 8), 0.00001);
		assertEquals(0.0, derivs.get(10, 9), 0.00001);
		assertEquals(0.0, derivs.get(11, 0), 0.00001);
		assertEquals(0.0, derivs.get(11, 1), 0.00001);
		assertEquals(0.0, derivs.get(11, 2), 0.00001);
		assertEquals(0.0, derivs.get(11, 3), 0.00001);
		assertEquals(0.0, derivs.get(11, 4), 0.00001);
		assertEquals(0.0, derivs.get(11, 5), 0.00001);
		assertEquals(0.0, derivs.get(11, 6), 0.00001);
		assertEquals(0.0, derivs.get(11, 7), 0.00001);
		assertEquals(0.0, derivs.get(11, 8), 0.00001);
		assertEquals(0.0, derivs.get(11, 9), 0.00001);
		assertEquals(0.0, derivs.get(12, 0), 0.00001);
		assertEquals(0.0, derivs.get(12, 1), 0.00001);
		assertEquals(0.0, derivs.get(12, 2), 0.00001);
		assertEquals(0.0, derivs.get(12, 3), 0.00001);
		assertEquals(0.0, derivs.get(12, 4), 0.00001);
		assertEquals(0.980586, derivs.get(12, 5), 0.00001);
		assertEquals(0.006211, derivs.get(12, 6), 0.00001);
		assertEquals(0.036853, derivs.get(12, 7), 0.00001);
		assertEquals(0.008656, derivs.get(12, 8), 0.00001);
		assertEquals(-1.08945, derivs.get(12, 9), 0.0001);
		assertEquals(0.0, derivs.get(13, 0), 0.00001);
		assertEquals(0.0, derivs.get(13, 1), 0.00001);
		assertEquals(0.0, derivs.get(13, 2), 0.00001);
		assertEquals(0.0, derivs.get(13, 3), 0.00001);
		assertEquals(0.0, derivs.get(13, 4), 0.00001);
		assertEquals(0.0, derivs.get(13, 5), 0.00001);
		assertEquals(0.0, derivs.get(13, 6), 0.00001);
		assertEquals(0.0, derivs.get(13, 7), 0.00001);
		assertEquals(0.0, derivs.get(13, 8), 0.00001);
		assertEquals(0.0, derivs.get(13, 9), 0.00001);
	}

}
