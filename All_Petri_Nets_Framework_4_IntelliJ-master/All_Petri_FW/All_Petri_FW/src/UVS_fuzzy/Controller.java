package UVS_fuzzy;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriNetWindow;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataString;
import DataObjects.DataTransfer;
import DataOnly.Fuzzy;
import DataOnly.TransferOperation;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

/**
 * Controller - Traffic Light Controller using FLETPNs
 *
 * Adapted from Controller2F in DCS_UVS:
 *   - Same state sequence: r1r2 -> g1r2 -> y1r2 -> r1g2 -> r1y2 -> loop
 *   - 5-second delays per phase
 *   - Instead of DataString colors, sends FUZZY signals:
 *       Red    = NL = -1.0F  <1,0,0,0,0>
 *       Green  = ZR =  0.0F  <0,0,1,0,0>
 *       Yellow = PL = +1.0F  <0,0,0,0,1>
 *   - Transmits to OP1..OP4 corresponding to Lane 1 (1084), Lane 2 (1085),
 *     Lane 3 (1086), Lane 4 (1087) on place P_TL.
 */
public class Controller {

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		pn.PetriNetName = "Controller";
		pn.NetworkPort = 1083;

		// ----------------------------------------------------------------
		// FUZZY SIGNAL CONSTANTS
		// ----------------------------------------------------------------
		// Red: NL = -1.0F
		DataFuzzy red = new DataFuzzy();
		red.SetName("red");
		red.SetValue(new Fuzzy(-1.0F));
		red.Printable = false;
		pn.ConstantPlaceList.add(red);

		// Green: ZR = 0.0F
		DataFuzzy green = new DataFuzzy();
		green.SetName("green");
		green.SetValue(new Fuzzy(0.0F));
		green.Printable = false;
		pn.ConstantPlaceList.add(green);

		// Yellow: PL = +1.0F
		DataFuzzy yellow = new DataFuzzy();
		yellow.SetName("yellow");
		yellow.SetValue(new Fuzzy(1.0F));
		yellow.Printable = false;
		pn.ConstantPlaceList.add(yellow);

		// ----------------------------------------------------------------
		// STATE PLACES (same state machine as Controller2F from assignment 1)
		// ----------------------------------------------------------------
		DataString r1r2 = new DataString();
		r1r2.SetName("r1r2");
		r1r2.SetValue("signal"); // Initial token: all red
		pn.PlaceList.add(r1r2);

		DataString g1r2 = new DataString();
		g1r2.SetName("g1r2"); // Lane 1&3 Green, Lane 2&4 Red
		pn.PlaceList.add(g1r2);

		DataString y1r2 = new DataString();
		y1r2.SetName("y1r2"); // Lane 1&3 Yellow, Lane 2&4 Red
		pn.PlaceList.add(y1r2);

		DataString r1g2 = new DataString();
		r1g2.SetName("r1g2"); // Lane 1&3 Red, Lane 2&4 Green
		pn.PlaceList.add(r1g2);

		DataString r1y2 = new DataString();
		r1y2.SetName("r1y2"); // Lane 1&3 Red, Lane 2&4 Yellow
		pn.PlaceList.add(r1y2);

		// ----------------------------------------------------------------
		// NETWORK TRANSFER OUTPUTS (sending to each Lane's P_TL place)
		// ----------------------------------------------------------------
		DataTransfer op1 = new DataTransfer();
		op1.SetName("OP1");
		op1.Value = new TransferOperation("localhost", "1084", "P_TL1");
		pn.PlaceList.add(op1);

		DataTransfer op2 = new DataTransfer();
		op2.SetName("OP2");
		op2.Value = new TransferOperation("localhost", "1085", "P_TL2");
		pn.PlaceList.add(op2);

		DataTransfer op3 = new DataTransfer();
		op3.SetName("OP3");
		op3.Value = new TransferOperation("localhost", "1086", "P_TL3");
		pn.PlaceList.add(op3);

		DataTransfer op4 = new DataTransfer();
		op4.SetName("OP4");
		op4.Value = new TransferOperation("localhost", "1087", "P_TL4");
		pn.PlaceList.add(op4);

		// ----------------------------------------------------------------
		// TRANSITIONS (5-Second Delays, adapted from Controller2F)
		// ----------------------------------------------------------------

		// T1: r1r2 -> g1r2 (Lane 1&3 Green, Lane 2&4 Red)
		PetriTransition t1 = new PetriTransition(pn);
		t1.TransitionName = "T1";
		t1.InputPlaceName.add("r1r2");

		Condition t1_c = new Condition(t1, "r1r2", TransitionCondition.NotNull);
		GuardMapping grd_t1 = new GuardMapping();
		grd_t1.condition = t1_c;
		grd_t1.Activations.add(new Activation(t1, "r1r2", TransitionOperation.Move, "g1r2"));
		grd_t1.Activations.add(new Activation(t1, "green", TransitionOperation.SendOverNetwork, "OP1"));
		grd_t1.Activations.add(new Activation(t1, "green", TransitionOperation.SendOverNetwork, "OP3"));
		grd_t1.Activations.add(new Activation(t1, "red",   TransitionOperation.SendOverNetwork, "OP2"));
		grd_t1.Activations.add(new Activation(t1, "red",   TransitionOperation.SendOverNetwork, "OP4"));
		t1.GuardMappingList.add(grd_t1);
		t1.Delay = 5;
		pn.Transitions.add(t1);

		// T2: g1r2 -> y1r2 (Lane 1&3 Yellow, Lane 2&4 Red)
		PetriTransition t2 = new PetriTransition(pn);
		t2.TransitionName = "T2";
		t2.InputPlaceName.add("g1r2");

		Condition t2_c = new Condition(t2, "g1r2", TransitionCondition.NotNull);
		GuardMapping grd_t2 = new GuardMapping();
		grd_t2.condition = t2_c;
		grd_t2.Activations.add(new Activation(t2, "g1r2", TransitionOperation.Move, "y1r2"));
		grd_t2.Activations.add(new Activation(t2, "yellow", TransitionOperation.SendOverNetwork, "OP1"));
		grd_t2.Activations.add(new Activation(t2, "yellow", TransitionOperation.SendOverNetwork, "OP3"));
		t2.GuardMappingList.add(grd_t2);
		t2.Delay = 5;
		pn.Transitions.add(t2);

		// T3: y1r2 -> r1g2 (Lane 1&3 Red, Lane 2&4 Green)
		PetriTransition t3 = new PetriTransition(pn);
		t3.TransitionName = "T3";
		t3.InputPlaceName.add("y1r2");

		Condition t3_c = new Condition(t3, "y1r2", TransitionCondition.NotNull);
		GuardMapping grd_t3 = new GuardMapping();
		grd_t3.condition = t3_c;
		grd_t3.Activations.add(new Activation(t3, "y1r2", TransitionOperation.Move, "r1g2"));
		grd_t3.Activations.add(new Activation(t3, "red",   TransitionOperation.SendOverNetwork, "OP1"));
		grd_t3.Activations.add(new Activation(t3, "red",   TransitionOperation.SendOverNetwork, "OP3"));
		grd_t3.Activations.add(new Activation(t3, "green", TransitionOperation.SendOverNetwork, "OP2"));
		grd_t3.Activations.add(new Activation(t3, "green", TransitionOperation.SendOverNetwork, "OP4"));
		t3.GuardMappingList.add(grd_t3);
		t3.Delay = 5;
		pn.Transitions.add(t3);

		// T4: r1g2 -> r1y2 (Lane 1&3 Red, Lane 2&4 Yellow)
		PetriTransition t4 = new PetriTransition(pn);
		t4.TransitionName = "T4";
		t4.InputPlaceName.add("r1g2");

		Condition t4_c = new Condition(t4, "r1g2", TransitionCondition.NotNull);
		GuardMapping grd_t4 = new GuardMapping();
		grd_t4.condition = t4_c;
		grd_t4.Activations.add(new Activation(t4, "r1g2", TransitionOperation.Move, "r1y2"));
		grd_t4.Activations.add(new Activation(t4, "yellow", TransitionOperation.SendOverNetwork, "OP2"));
		grd_t4.Activations.add(new Activation(t4, "yellow", TransitionOperation.SendOverNetwork, "OP4"));
		t4.GuardMappingList.add(grd_t4);
		t4.Delay = 5;
		pn.Transitions.add(t4);

		// T5: r1y2 -> g1r2 (Loop back: Lane 1&3 Green, Lane 2&4 Red)
		PetriTransition t5 = new PetriTransition(pn);
		t5.TransitionName = "T5";
		t5.InputPlaceName.add("r1y2");

		Condition t5_c = new Condition(t5, "r1y2", TransitionCondition.NotNull);
		GuardMapping grd_t5 = new GuardMapping();
		grd_t5.condition = t5_c;
		grd_t5.Activations.add(new Activation(t5, "r1y2", TransitionOperation.Move, "g1r2"));
		grd_t5.Activations.add(new Activation(t5, "green", TransitionOperation.SendOverNetwork, "OP1"));
		grd_t5.Activations.add(new Activation(t5, "green", TransitionOperation.SendOverNetwork, "OP3"));
		grd_t5.Activations.add(new Activation(t5, "red",   TransitionOperation.SendOverNetwork, "OP2"));
		grd_t5.Activations.add(new Activation(t5, "red",   TransitionOperation.SendOverNetwork, "OP4"));
		t5.GuardMappingList.add(grd_t5);
		t5.Delay = 5;
		pn.Transitions.add(t5);

		// ----------------------------------------------------------------
		// START
		// ----------------------------------------------------------------
		System.out.println("Controller started");
		pn.Delay = 1000;
		PetriNetWindow frame = new PetriNetWindow(false);
		frame.petriNet = pn;
		frame.setVisible(true);
	}
}
