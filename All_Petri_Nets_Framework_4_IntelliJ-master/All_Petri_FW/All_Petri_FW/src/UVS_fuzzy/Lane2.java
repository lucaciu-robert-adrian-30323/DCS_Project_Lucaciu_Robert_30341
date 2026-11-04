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
 * Lane 2 - Urban Vehicle Flow Control System using FLETPNs
 *
 * Places:
 *   - P_i2: Input flow place
 *   - P_l2: Lane flow place
 *   - P_TL2: Traffic light signal received from Controller (Red=NL, Green=ZR, Yellow=PL)
 *   - P_out2: DataTransfer sending flow to Intersection (port 1080, place P_i2)
 */
public class Lane2 {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Lane 2";
		pn.NetworkPort = 1085;

		// ----------------------------------------------------------------
		// PLACES
		// ----------------------------------------------------------------
		DataFuzzy p_i2 = new DataFuzzy();
		p_i2.SetName("P_i2");
		p_i2.SetValue(new Fuzzy(0.4F)); // Sample initial incoming vehicle flow
		pn.PlaceList.add(p_i2);

		DataFuzzy p_l2 = new DataFuzzy();
		p_l2.SetName("P_l2");
		pn.PlaceList.add(p_l2);

		// P_TL2: initially Red (NL = -1.0F)
		DataFuzzy p_tl2 = new DataFuzzy();
		p_tl2.SetName("P_TL2");
		p_tl2.SetValue(new Fuzzy(-1.0F)); // Red
		pn.PlaceList.add(p_tl2);

		DataTransfer p_out2 = new DataTransfer();
		p_out2.SetName("P_out2");
		p_out2.Value = new TransferOperation("localhost", "1080", "P_i2");
		pn.PlaceList.add(p_out2);

		// Constant Green: ZR <0, 0, 1, 0, 0> = 0.0F
		DataFuzzy green = new DataFuzzy();
		green.SetName("Green");
		green.SetValue(new Fuzzy(0.0F));
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		// ----------------------------------------------------------------
		// TRANSITIONS
		// ----------------------------------------------------------------

		// t_in2: move flow from P_i2 to P_l2 (FLRS Move)
		PetriTransition t_in2 = new PetriTransition(pn);
		t_in2.TransitionName = "t_in2";
		t_in2.InputPlaceName.add("P_i2");

		Condition cin2 = new Condition(t_in2, "P_i2", TransitionCondition.NotNull);
		GuardMapping grd_in2 = new GuardMapping();
		grd_in2.condition = cin2;

		ArrayList<PlaceNameWithWeight> in2_inputs = new ArrayList<>();
		in2_inputs.add(new PlaceNameWithWeight("P_i2", 1.0F));
		ArrayList<String> in2_outputs = new ArrayList<>();
		in2_outputs.add("P_l2");

		grd_in2.Activations.add(new Activation(t_in2, FLRS_Library.flrsMove, in2_inputs, TransitionOperation.FLRS, in2_outputs));
		t_in2.GuardMappingList.add(grd_in2);
		t_in2.Delay = 0;
		pn.Transitions.add(t_in2);

		// t_out2: release flow from P_l2 to P_out2 ONLY if P_TL2 == Green
		PetriTransition t_out2 = new PetriTransition(pn);
		t_out2.TransitionName = "t_out2";
		t_out2.InputPlaceName.add("P_l2");
		t_out2.InputPlaceName.add("P_TL2");

		Condition cout2_a = new Condition(t_out2, "P_l2",  TransitionCondition.NotNull);
		Condition cout2_b = new Condition(t_out2, "P_TL2", TransitionCondition.Equal, "Green");
		cout2_a.SetNextCondition(LogicConnector.AND, cout2_b);

		GuardMapping grd_out2 = new GuardMapping();
		grd_out2.condition = cout2_a;

		ArrayList<PlaceNameWithWeight> out2_inputs = new ArrayList<>();
		out2_inputs.add(new PlaceNameWithWeight("P_l2", 1.0F));
		ArrayList<String> out2_outputs = new ArrayList<>();
		out2_outputs.add("P_out2");

		grd_out2.Activations.add(new Activation(t_out2, FLRS_Library.flrsMove, out2_inputs, TransitionOperation.FLRS, out2_outputs));
		grd_out2.Activations.add(new Activation(t_out2, "P_TL2", TransitionOperation.Copy, "P_TL2"));

		t_out2.GuardMappingList.add(grd_out2);
		t_out2.Delay = 1;
		pn.Transitions.add(t_out2);

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Lane 2 started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
