package UVS_fuzzy;

import DataOnly.FLRS;
import DataOnly.FV;
import Enumerations.FZ;

public class FLRS_Library {

	/**
	 * Identity FLRS (Move operation for 1 input, 1 output):
	 * Input:  NL, NM, ZR, PM, PL
	 * Output: NL, NM, ZR, PM, PL
	 */
	public static FLRS flrsMove = new FLRS(
			new FV(FZ.NL),
			new FV(FZ.NM),
			new FV(FZ.ZR),
			new FV(FZ.PM),
			new FV(FZ.PL)
	);

	/**
	 * FLRS+ (Fuzzy Addition for 2 inputs, 1 output):
	 * Matrix as defined in Project 2 Sheet:
	 * NL: [NL, NL, NL, NM, ZR]
	 * NM: [NL, NL, NM, ZR, PM]
	 * ZR: [NL, NM, ZR, PM, PL]
	 * PM: [NM, ZR, PM, PL, PL]
	 * PL: [ZR, PM, PL, PL, PL]
	 */
	public static FLRS flrsAdd = new FLRS(
			new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
			new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM),
			new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
			new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL),
			new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL), new FV(FZ.PL)
	);
}
