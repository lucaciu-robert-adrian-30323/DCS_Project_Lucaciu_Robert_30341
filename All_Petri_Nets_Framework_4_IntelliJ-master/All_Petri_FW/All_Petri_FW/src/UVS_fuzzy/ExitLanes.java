package UVS_fuzzy;

import java.util.ArrayList;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataOnly.PlaceNameWithWeight;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

/**
 * Exit Lanes - Urban Vehicle Flow Control System using FLETPNs
 *
 * Receives flows from Intersection into P_o1, P_o2, P_o3, P_o4 on port 1082
 * and moves them to P_End1..4 via FLRS Move.
 */
public class ExitLanes {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Exit Lanes";
		pn.NetworkPort = 1082; // Receives from Intersection

		// 4 Exit Lanes
		for (int i = 1; i <= 4; i++) {
			String placeIn = "P_o" + i;
			String placeEnd = "P_End" + i;
			String transName = "t_" + i;

			DataFuzzy p_in = new DataFuzzy();
			p_in.SetName(placeIn);
			pn.PlaceList.add(p_in);

			DataFuzzy p_end = new DataFuzzy();
			p_end.SetName(placeEnd);
			pn.PlaceList.add(p_end);

			PetriTransition t = new PetriTransition(pn);
			t.TransitionName = transName;
			t.InputPlaceName.add(placeIn);

			Condition cond = new Condition(t, placeIn, TransitionCondition.NotNull);
			GuardMapping grd = new GuardMapping();
			grd.condition = cond;

			ArrayList<PlaceNameWithWeight> in_weights = new ArrayList<>();
			in_weights.add(new PlaceNameWithWeight(placeIn, 1.0F));
			ArrayList<String> out_names = new ArrayList<>();
			out_names.add(placeEnd);

			grd.Activations.add(new Activation(t, FLRS_Library.flrsMove, in_weights, TransitionOperation.FLRS, out_names));
			t.GuardMappingList.add(grd);
			t.Delay = 0;
			pn.Transitions.add(t);
		}

		System.out.println("Exit Lanes started");
		pn.Delay = 2000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
