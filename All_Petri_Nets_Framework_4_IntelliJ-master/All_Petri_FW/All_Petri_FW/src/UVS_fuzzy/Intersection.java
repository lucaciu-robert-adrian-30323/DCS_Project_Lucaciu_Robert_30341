package UVS_fuzzy;

import java.util.ArrayList;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.Fuzzy;
import DataOnly.PlaceNameWithWeight;
import DataOnly.TransferOperation;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

/**
 * Intersection - Urban Vehicle Flow Control System using FLETPNs
 *
 * Requirements from Sheet 2:
 *   - Inputs: P_i1, P_i2, P_i3, P_i4 (received from 4 incoming lanes on port 1080)
 *   - Summing transitions using FLRS+:
 *       t1: P_i1 + P_i2 -> P_int1
 *       t2: P_i3 + P_i4 -> P_int2
 *       t3: P_int1 + P_int2 -> P_i (total intersection flow)
 *   - Flow splitting transition t_s (using Prod_Fuzzy mathematical operations):
 *       P_i_out1 = P_i * 0.25 (to Exit Lane 1)
 *       P_i_out2 = P_i * 0.50 (to Exit Lane 2)
 *       P_i_out3 = P_i * 0.13 (to Exit Lane 3)
 *       P_i_out4 = P_i * 0.12 (to Exit Lane 4)
 *   - Transitions t_out1..4: send divided flows over network to Exit Lanes (port 1082)
 */
public class Intersection {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Intersection";
		pn.NetworkPort = 1080;

		// ----------------------------------------------------------------
		// INPUT PLACES (from Lane 1, 2, 3, 4)
		// ----------------------------------------------------------------
		DataFuzzy p_i1 = new DataFuzzy();
		p_i1.SetName("P_i1");
		p_i1.SetValue(new Fuzzy(0.3F)); // initial sample flow
		pn.PlaceList.add(p_i1);

		DataFuzzy p_i2 = new DataFuzzy();
		p_i2.SetName("P_i2");
		p_i2.SetValue(new Fuzzy(0.2F)); // initial sample flow
		pn.PlaceList.add(p_i2);

		DataFuzzy p_i3 = new DataFuzzy();
		p_i3.SetName("P_i3");
		p_i3.SetValue(new Fuzzy(0.1F)); // initial sample flow
		pn.PlaceList.add(p_i3);

		DataFuzzy p_i4 = new DataFuzzy();
		p_i4.SetName("P_i4");
		p_i4.SetValue(new Fuzzy(0.2F)); // initial sample flow
		pn.PlaceList.add(p_i4);

		// ----------------------------------------------------------------
		// INTERMEDIATE & TOTAL SUM PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_int1 = new DataFuzzy();
		p_int1.SetName("P_int1");
		pn.PlaceList.add(p_int1);

		DataFuzzy p_int2 = new DataFuzzy();
		p_int2.SetName("P_int2");
		pn.PlaceList.add(p_int2);

		DataFuzzy p_i = new DataFuzzy();
		p_i.SetName("P_i");
		pn.PlaceList.add(p_i);

		// ----------------------------------------------------------------
		// SPLIT OUTPUT PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_i_out1 = new DataFuzzy();
		p_i_out1.SetName("P_i_out1");
		pn.PlaceList.add(p_i_out1);

		DataFuzzy p_i_out2 = new DataFuzzy();
		p_i_out2.SetName("P_i_out2");
		pn.PlaceList.add(p_i_out2);

		DataFuzzy p_i_out3 = new DataFuzzy();
		p_i_out3.SetName("P_i_out3");
		pn.PlaceList.add(p_i_out3);

		DataFuzzy p_i_out4 = new DataFuzzy();
		p_i_out4.SetName("P_i_out4");
		pn.PlaceList.add(p_i_out4);

		// ----------------------------------------------------------------
		// NETWORK TRANSFER PLACES (to Exit Lanes on port 1082)
		// ----------------------------------------------------------------
		DataTransfer p_exit1 = new DataTransfer();
		p_exit1.SetName("P_exit1");
		p_exit1.Value = new TransferOperation("localhost", "1082", "P_o1");
		pn.PlaceList.add(p_exit1);

		DataTransfer p_exit2 = new DataTransfer();
		p_exit2.SetName("P_exit2");
		p_exit2.Value = new TransferOperation("localhost", "1082", "P_o2");
		pn.PlaceList.add(p_exit2);

		DataTransfer p_exit3 = new DataTransfer();
		p_exit3.SetName("P_exit3");
		p_exit3.Value = new TransferOperation("localhost", "1082", "P_o3");
		pn.PlaceList.add(p_exit3);

		DataTransfer p_exit4 = new DataTransfer();
		p_exit4.SetName("P_exit4");
		p_exit4.Value = new TransferOperation("localhost", "1082", "P_o4");
		pn.PlaceList.add(p_exit4);

		// ----------------------------------------------------------------
		// CONSTANTS: Split multipliers in ConstantPlaceList
		// ----------------------------------------------------------------
		DataFuzzy c025 = new DataFuzzy();
		c025.SetName("c025");
		c025.SetValue(new Fuzzy(0.25F));
		c025.Printable = false;
		pn.ConstantPlaceList.add(c025);

		DataFuzzy c050 = new DataFuzzy();
		c050.SetName("c050");
		c050.SetValue(new Fuzzy(0.50F));
		c050.Printable = false;
		pn.ConstantPlaceList.add(c050);

		DataFuzzy c013 = new DataFuzzy();
		c013.SetName("c013");
		c013.SetValue(new Fuzzy(0.13F));
		c013.Printable = false;
		pn.ConstantPlaceList.add(c013);

		DataFuzzy c012 = new DataFuzzy();
		c012.SetName("c012");
		c012.SetValue(new Fuzzy(0.12F));
		c012.Printable = false;
		pn.ConstantPlaceList.add(c012);

		// ----------------------------------------------------------------
		// TRANSITION t1: P_i1 + P_i2 -> P_int1 (using FLRS+)
		// ----------------------------------------------------------------
		PetriTransition t1 = new PetriTransition(pn);
		t1.TransitionName = "t1";
		t1.InputPlaceName.add("P_i1");
		t1.InputPlaceName.add("P_i2");

		Condition t1_c1 = new Condition(t1, "P_i1", TransitionCondition.NotNull);
		Condition t1_c2 = new Condition(t1, "P_i2", TransitionCondition.NotNull);
		t1_c1.SetNextCondition(LogicConnector.AND, t1_c2);

		GuardMapping grd_t1 = new GuardMapping();
		grd_t1.condition = t1_c1;

		ArrayList<PlaceNameWithWeight> in_t1 = new ArrayList<>();
		in_t1.add(new PlaceNameWithWeight("P_i1", 1.0F));
		in_t1.add(new PlaceNameWithWeight("P_i2", 1.0F));
		ArrayList<String> out_t1 = new ArrayList<>();
		out_t1.add("P_int1");

		grd_t1.Activations.add(new Activation(t1, FLRS_Library.flrsAdd, in_t1, TransitionOperation.FLRS, out_t1));
		t1.GuardMappingList.add(grd_t1);
		t1.Delay = 0;
		pn.Transitions.add(t1);

		// ----------------------------------------------------------------
		// TRANSITION t2: P_i3 + P_i4 -> P_int2 (using FLRS+)
		// ----------------------------------------------------------------
		PetriTransition t2 = new PetriTransition(pn);
		t2.TransitionName = "t2";
		t2.InputPlaceName.add("P_i3");
		t2.InputPlaceName.add("P_i4");

		Condition t2_c1 = new Condition(t2, "P_i3", TransitionCondition.NotNull);
		Condition t2_c2 = new Condition(t2, "P_i4", TransitionCondition.NotNull);
		t2_c1.SetNextCondition(LogicConnector.AND, t2_c2);

		GuardMapping grd_t2 = new GuardMapping();
		grd_t2.condition = t2_c1;

		ArrayList<PlaceNameWithWeight> in_t2 = new ArrayList<>();
		in_t2.add(new PlaceNameWithWeight("P_i3", 1.0F));
		in_t2.add(new PlaceNameWithWeight("P_i4", 1.0F));
		ArrayList<String> out_t2 = new ArrayList<>();
		out_t2.add("P_int2");

		grd_t2.Activations.add(new Activation(t2, FLRS_Library.flrsAdd, in_t2, TransitionOperation.FLRS, out_t2));
		t2.GuardMappingList.add(grd_t2);
		t2.Delay = 0;
		pn.Transitions.add(t2);

		// ----------------------------------------------------------------
		// TRANSITION t3: P_int1 + P_int2 -> P_i (using FLRS+)
		// ----------------------------------------------------------------
		PetriTransition t3 = new PetriTransition(pn);
		t3.TransitionName = "t3";
		t3.InputPlaceName.add("P_int1");
		t3.InputPlaceName.add("P_int2");

		Condition t3_c1 = new Condition(t3, "P_int1", TransitionCondition.NotNull);
		Condition t3_c2 = new Condition(t3, "P_int2", TransitionCondition.NotNull);
		t3_c1.SetNextCondition(LogicConnector.AND, t3_c2);

		GuardMapping grd_t3 = new GuardMapping();
		grd_t3.condition = t3_c1;

		ArrayList<PlaceNameWithWeight> in_t3 = new ArrayList<>();
		in_t3.add(new PlaceNameWithWeight("P_int1", 1.0F));
		in_t3.add(new PlaceNameWithWeight("P_int2", 1.0F));
		ArrayList<String> out_t3 = new ArrayList<>();
		out_t3.add("P_i");

		grd_t3.Activations.add(new Activation(t3, FLRS_Library.flrsAdd, in_t3, TransitionOperation.FLRS, out_t3));
		t3.GuardMappingList.add(grd_t3);
		t3.Delay = 0;
		pn.Transitions.add(t3);

		// ----------------------------------------------------------------
		// TRANSITION t_s: Split Total Flow P_i with Prod_Fuzzy
		// ----------------------------------------------------------------
		PetriTransition t_s = new PetriTransition(pn);
		t_s.TransitionName = "t_s";
		t_s.InputPlaceName.add("P_i");

		Condition ts_c1 = new Condition(t_s, "P_i", TransitionCondition.NotNull);
		GuardMapping grd_ts = new GuardMapping();
		grd_ts.condition = ts_c1;

		// P_i_out1 = P_i * 0.25
		ArrayList<PlaceNameWithWeight> in_s1 = new ArrayList<>();
		in_s1.add(new PlaceNameWithWeight("P_i", 1.0F));
		in_s1.add(new PlaceNameWithWeight("c025", 1.0F));
		grd_ts.Activations.add(new Activation(in_s1, TransitionOperation.Prod_Fuzzy, "P_i_out1", t_s));

		// P_i_out2 = P_i * 0.50
		ArrayList<PlaceNameWithWeight> in_s2 = new ArrayList<>();
		in_s2.add(new PlaceNameWithWeight("P_i", 1.0F));
		in_s2.add(new PlaceNameWithWeight("c050", 1.0F));
		grd_ts.Activations.add(new Activation(in_s2, TransitionOperation.Prod_Fuzzy, "P_i_out2", t_s));

		// P_i_out3 = P_i * 0.13
		ArrayList<PlaceNameWithWeight> in_s3 = new ArrayList<>();
		in_s3.add(new PlaceNameWithWeight("P_i", 1.0F));
		in_s3.add(new PlaceNameWithWeight("c013", 1.0F));
		grd_ts.Activations.add(new Activation(in_s3, TransitionOperation.Prod_Fuzzy, "P_i_out3", t_s));

		// P_i_out4 = P_i * 0.12
		ArrayList<PlaceNameWithWeight> in_s4 = new ArrayList<>();
		in_s4.add(new PlaceNameWithWeight("P_i", 1.0F));
		in_s4.add(new PlaceNameWithWeight("c012", 1.0F));
		grd_ts.Activations.add(new Activation(in_s4, TransitionOperation.Prod_Fuzzy, "P_i_out4", t_s));

		t_s.GuardMappingList.add(grd_ts);
		t_s.Delay = 0;
		pn.Transitions.add(t_s);

		// ----------------------------------------------------------------
		// TRANSITIONS t_out1..4: Send Split Flows to Exit Lanes
		// ----------------------------------------------------------------
		addExitSender(pn, "t_out1", "P_i_out1", "P_exit1");
		addExitSender(pn, "t_out2", "P_i_out2", "P_exit2");
		addExitSender(pn, "t_out3", "P_i_out3", "P_exit3");
		addExitSender(pn, "t_out4", "P_i_out4", "P_exit4");

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Intersection started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}

	private static void addExitSender(PetriNet pn, String transName, String inputPlace, String transferPlace) {
		PetriTransition t = new PetriTransition(pn);
		t.TransitionName = transName;
		t.InputPlaceName.add(inputPlace);

		Condition cond = new Condition(t, inputPlace, TransitionCondition.NotNull);
		GuardMapping grd = new GuardMapping();
		grd.condition = cond;
		grd.Activations.add(new Activation(t, inputPlace, TransitionOperation.SendOverNetwork, transferPlace));
		t.GuardMappingList.add(grd);
		t.Delay = 0;
		pn.Transitions.add(t);
	}
}
