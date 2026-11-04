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
 * Lane 3 - Urban Vehicle Flow Control System using FLETPNs
 *
 * Places:
 *   - P_i3: Input flow place
 *   - P_l3: Lane flow place
 *   - P_TL3: Traffic light signal received from Controller (Red=NL, Green=ZR, Yellow=PL)
 *   - P_out3: DataTransfer sending flow to Intersection (port 1080, place P_i3)
 */
public class Lane3 {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Lane 3";
		pn.NetworkPort = 1086;

		// ----------------------------------------------------------------
		// PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_i3 = new DataFuzzy();
		p_i3.SetName("P_i3");
		p_i3.SetValue(new Fuzzy(0.5F)); // Sample initial incoming vehicle flow
		pn.PlaceList.add(p_i3);

		DataFuzzy p_l3 = new DataFuzzy();
		p_l3.SetName("P_l3");
		pn.PlaceList.add(p_l3);

		// P_TL3: initially Green (ZR = 0.0F) — same phase as Lane 1
		DataFuzzy p_tl3 = new DataFuzzy();
		p_tl3.SetName("P_TL3");
		p_tl3.SetValue(new Fuzzy(0.0F)); // Green
		pn.PlaceList.add(p_tl3);

		DataTransfer p_out3 = new DataTransfer();
		p_out3.SetName("P_out3");
		p_out3.Value = new TransferOperation("localhost", "1080", "P_i3");
		pn.PlaceList.add(p_out3);

		// Constant Green: ZR <0, 0, 1, 0, 0> = 0.0F
		DataFuzzy green = new DataFuzzy();
		green.SetName("Green");
		green.SetValue(new Fuzzy(0.0F));
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		// ----------------------------------------------------------------
		// TRANSITIONS
		// ----------------------------------------------------------------

		// t_in3: move flow from P_i3 to P_l3 (FLRS Move)
		PetriTransition t_in3 = new PetriTransition(pn);
		t_in3.TransitionName = "t_in3";
		t_in3.InputPlaceName.add("P_i3");

		Condition cin3 = new Condition(t_in3, "P_i3", TransitionCondition.NotNull);
		GuardMapping grd_in3 = new GuardMapping();
		grd_in3.condition = cin3;

		ArrayList<PlaceNameWithWeight> in3_inputs = new ArrayList<>();
		in3_inputs.add(new PlaceNameWithWeight("P_i3", 1.0F));
		ArrayList<String> in3_outputs = new ArrayList<>();
		in3_outputs.add("P_l3");

		grd_in3.Activations.add(new Activation(t_in3, FLRS_Library.flrsMove, in3_inputs, TransitionOperation.FLRS, in3_outputs));
		t_in3.GuardMappingList.add(grd_in3);
		t_in3.Delay = 0;
		pn.Transitions.add(t_in3);

		// t_out3: release flow from P_l3 to P_out3 ONLY if P_TL3 == Green
		PetriTransition t_out3 = new PetriTransition(pn);
		t_out3.TransitionName = "t_out3";
		t_out3.InputPlaceName.add("P_l3");
		t_out3.InputPlaceName.add("P_TL3");

		Condition cout3_a = new Condition(t_out3, "P_l3",  TransitionCondition.NotNull);
		Condition cout3_b = new Condition(t_out3, "P_TL3", TransitionCondition.Equal, "Green");
		cout3_a.SetNextCondition(LogicConnector.AND, cout3_b);

		GuardMapping grd_out3 = new GuardMapping();
		grd_out3.condition = cout3_a;

		ArrayList<PlaceNameWithWeight> out3_inputs = new ArrayList<>();
		out3_inputs.add(new PlaceNameWithWeight("P_l3", 1.0F));
		ArrayList<String> out3_outputs = new ArrayList<>();
		out3_outputs.add("P_out3");

		grd_out3.Activations.add(new Activation(t_out3, FLRS_Library.flrsMove, out3_inputs, TransitionOperation.FLRS, out3_outputs));
		grd_out3.Activations.add(new Activation(t_out3, "P_TL3", TransitionOperation.Copy, "P_TL3"));

		t_out3.GuardMappingList.add(grd_out3);
		t_out3.Delay = 1;
		pn.Transitions.add(t_out3);

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Lane 3 started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
