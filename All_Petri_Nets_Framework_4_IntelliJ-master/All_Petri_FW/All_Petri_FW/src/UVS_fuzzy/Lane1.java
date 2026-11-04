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
 * Lane 1 - Urban Vehicle Flow Control System using FLETPNs
 *
 * Places:
 *   - P_i1: Input flow place
 *   - P_l1: Lane flow place
 *   - P_TL1: Traffic light signal received from Controller (Red=NL, Green=ZR, Yellow=PL)
 *   - P_out1: DataTransfer sending flow to Intersection (port 1080, place P_i1)
 *
 * Transitions (adapted from first assignment DCS_UVS Lane T_u1, T_e1 to FLETPN):
 *   - t_in1: Moves flow from P_i1 to P_l1 using FLRS Move
 *   - t_out1: Moves flow from P_l1 to P_out1 using FLRS Move ONLY when P_TL1 == Green (ZR)
 */
public class Lane1 {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Lane 1";
		pn.NetworkPort = 1084;

		// ----------------------------------------------------------------
		// PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_i1 = new DataFuzzy();
		p_i1.SetName("P_i1");
		p_i1.SetValue(new Fuzzy(0.6F)); // Sample initial incoming vehicle flow
		pn.PlaceList.add(p_i1);

		DataFuzzy p_l1 = new DataFuzzy();
		p_l1.SetName("P_l1");
		pn.PlaceList.add(p_l1);

		// P_TL1: Traffic light signal (Green=ZR=0.0F, Red=NL=-1.0F, Yellow=PL=1.0F)
		DataFuzzy p_tl1 = new DataFuzzy();
		p_tl1.SetName("P_TL1");
		p_tl1.SetValue(new Fuzzy(0.0F)); // Initially Green
		pn.PlaceList.add(p_tl1);

		// P_out1: sends flow over network to Intersection input P_i1
		DataTransfer p_out1 = new DataTransfer();
		p_out1.SetName("P_out1");
		p_out1.Value = new TransferOperation("localhost", "1080", "P_i1");
		pn.PlaceList.add(p_out1);

		// Constant Green: ZR <0, 0, 1, 0, 0> = 0.0F
		DataFuzzy green = new DataFuzzy();
		green.SetName("Green");
		green.SetValue(new Fuzzy(0.0F));
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		// ----------------------------------------------------------------
		// TRANSITIONS
		// ----------------------------------------------------------------

		// t_in1: move flow from P_i1 to P_l1 (FLRS Move)
		PetriTransition t_in1 = new PetriTransition(pn);
		t_in1.TransitionName = "t_in1";
		t_in1.InputPlaceName.add("P_i1");

		Condition cin1 = new Condition(t_in1, "P_i1", TransitionCondition.NotNull);
		GuardMapping grd_in1 = new GuardMapping();
		grd_in1.condition = cin1;

		ArrayList<PlaceNameWithWeight> in1_inputs = new ArrayList<>();
		in1_inputs.add(new PlaceNameWithWeight("P_i1", 1.0F));
		ArrayList<String> in1_outputs = new ArrayList<>();
		in1_outputs.add("P_l1");

		grd_in1.Activations.add(new Activation(t_in1, FLRS_Library.flrsMove, in1_inputs, TransitionOperation.FLRS, in1_outputs));
		t_in1.GuardMappingList.add(grd_in1);
		t_in1.Delay = 0;
		pn.Transitions.add(t_in1);

		// t_out1: release flow from P_l1 to P_out1 ONLY if P_TL1 == Green
		PetriTransition t_out1 = new PetriTransition(pn);
		t_out1.TransitionName = "t_out1";
		t_out1.InputPlaceName.add("P_l1");
		t_out1.InputPlaceName.add("P_TL1");

		Condition cout1_a = new Condition(t_out1, "P_l1",  TransitionCondition.NotNull);
		Condition cout1_b = new Condition(t_out1, "P_TL1", TransitionCondition.Equal, "Green");
		cout1_a.SetNextCondition(LogicConnector.AND, cout1_b);

		GuardMapping grd_out1 = new GuardMapping();
		grd_out1.condition = cout1_a;

		ArrayList<PlaceNameWithWeight> out1_inputs = new ArrayList<>();
		out1_inputs.add(new PlaceNameWithWeight("P_l1", 1.0F));
		ArrayList<String> out1_outputs = new ArrayList<>();
		out1_outputs.add("P_out1");

		grd_out1.Activations.add(new Activation(t_out1, FLRS_Library.flrsMove, out1_inputs, TransitionOperation.FLRS, out1_outputs));
		grd_out1.Activations.add(new Activation(t_out1, "P_TL1", TransitionOperation.Copy, "P_TL1"));

		t_out1.GuardMappingList.add(grd_out1);
		t_out1.Delay = 1;
		pn.Transitions.add(t_out1);

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Lane 1 started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
