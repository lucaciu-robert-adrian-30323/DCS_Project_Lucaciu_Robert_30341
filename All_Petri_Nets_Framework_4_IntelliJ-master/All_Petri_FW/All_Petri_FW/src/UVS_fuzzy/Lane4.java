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
 * Lane 4 - Urban Vehicle Flow Control System using FLETPNs
 *
 * Places:
 *   - P_i4: Input flow place
 *   - P_l4: Lane flow place
 *   - P_TL4: Traffic light signal received from Controller (Red=NL, Green=ZR, Yellow=PL)
 *   - P_out4: DataTransfer sending flow to Intersection (port 1080, place P_i4)
 */
public class Lane4 {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Lane 4";
		pn.NetworkPort = 1087;

		// ----------------------------------------------------------------
		// PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_i4 = new DataFuzzy();
		p_i4.SetName("P_i4");
		p_i4.SetValue(new Fuzzy(0.3F)); // Sample initial incoming vehicle flow
		pn.PlaceList.add(p_i4);

		DataFuzzy p_l4 = new DataFuzzy();
		p_l4.SetName("P_l4");
		pn.PlaceList.add(p_l4);

		// P_TL4: initially Red (NL = -1.0F) — same phase as Lane 2
		DataFuzzy p_tl4 = new DataFuzzy();
		p_tl4.SetName("P_TL4");
		p_tl4.SetValue(new Fuzzy(-1.0F)); // Red
		pn.PlaceList.add(p_tl4);

		DataTransfer p_out4 = new DataTransfer();
		p_out4.SetName("P_out4");
		p_out4.Value = new TransferOperation("localhost", "1080", "P_i4");
		pn.PlaceList.add(p_out4);

		// Constant Green: ZR <0, 0, 1, 0, 0> = 0.0F
		DataFuzzy green = new DataFuzzy();
		green.SetName("Green");
		green.SetValue(new Fuzzy(0.0F));
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		// ----------------------------------------------------------------
		// TRANSITIONS
		// ----------------------------------------------------------------

		// t_in4: move flow from P_i4 to P_l4 (FLRS Move)
		PetriTransition t_in4 = new PetriTransition(pn);
		t_in4.TransitionName = "t_in4";
		t_in4.InputPlaceName.add("P_i4");

		Condition cin4 = new Condition(t_in4, "P_i4", TransitionCondition.NotNull);
		GuardMapping grd_in4 = new GuardMapping();
		grd_in4.condition = cin4;

		ArrayList<PlaceNameWithWeight> in4_inputs = new ArrayList<>();
		in4_inputs.add(new PlaceNameWithWeight("P_i4", 1.0F));
		ArrayList<String> in4_outputs = new ArrayList<>();
		in4_outputs.add("P_l4");

		grd_in4.Activations.add(new Activation(t_in4, FLRS_Library.flrsMove, in4_inputs, TransitionOperation.FLRS, in4_outputs));
		t_in4.GuardMappingList.add(grd_in4);
		t_in4.Delay = 0;
		pn.Transitions.add(t_in4);

		// t_out4: release flow from P_l4 to P_out4 ONLY if P_TL4 == Green
		PetriTransition t_out4 = new PetriTransition(pn);
		t_out4.TransitionName = "t_out4";
		t_out4.InputPlaceName.add("P_l4");
		t_out4.InputPlaceName.add("P_TL4");

		Condition cout4_a = new Condition(t_out4, "P_l4",  TransitionCondition.NotNull);
		Condition cout4_b = new Condition(t_out4, "P_TL4", TransitionCondition.Equal, "Green");
		cout4_a.SetNextCondition(LogicConnector.AND, cout4_b);

		GuardMapping grd_out4 = new GuardMapping();
		grd_out4.condition = cout4_a;

		ArrayList<PlaceNameWithWeight> out4_inputs = new ArrayList<>();
		out4_inputs.add(new PlaceNameWithWeight("P_l4", 1.0F));
		ArrayList<String> out4_outputs = new ArrayList<>();
		out4_outputs.add("P_out4");

		grd_out4.Activations.add(new Activation(t_out4, FLRS_Library.flrsMove, out4_inputs, TransitionOperation.FLRS, out4_outputs));
		grd_out4.Activations.add(new Activation(t_out4, "P_TL4", TransitionOperation.Copy, "P_TL4"));

		t_out4.GuardMappingList.add(grd_out4);
		t_out4.Delay = 1;
		pn.Transitions.add(t_out4);

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Lane 4 started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
