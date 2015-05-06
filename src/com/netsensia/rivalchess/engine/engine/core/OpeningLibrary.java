package com.netadapt.rivalchess.engine.core;

import java.util.ArrayList;
import java.util.Random;

import com.netadapt.rivalchess.util.ChessBoardConversion;

public class OpeningLibrary 
{
	private ArrayList<OpeningPosition> openings;
	
	public OpeningLibrary()
	{
		openings = new ArrayList<OpeningPosition>();

		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "e2e4", 5872));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "d2d4", 3603));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "c7c5", 2584));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq -", "g8f6", 2083));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "g1f3", 2014));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "e7e5", 1412));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -", "c2c4", 1295));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "g1f3", 1191));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -", "d7d6", 1164));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq -", "d7d5", 1044));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/3p4/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "d2d4", 1004));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/3p4/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq -", "c5d4", 994));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -", "b8c6", 965));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/3p4/8/3pP3/5N2/PPP2PPP/RNBQKB1R w KQkq -", "f3d4", 945));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "c2c4", 944));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "e7e6", 943));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/3p4/8/3NP3/8/PPP2PPP/RNBQKB1R b KQkq -", "g8f6", 901));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/3p1n2/8/3NP3/8/PPP2PPP/RNBQKB1R w KQkq -", "b1c3", 886));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/4p3/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq -", "d7d5", 853));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "d2d4", 823));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -", "c2c4", 630));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "e7e6", 610));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/3p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R b KQkq -", "a7a6", 604));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "f1b5", 599));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -", "g1f3", 568));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -", "b8c6", 490));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "b1c3", 487));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "g7g6", 457));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq -", "a7a6", 439));
		openings.add(new OpeningPosition("rnbqkb1r/pppppp1p/5np1/8/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "b1c3", 394));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "g1f3", 378));
		openings.add(new OpeningPosition("r1bqkbnr/pp1ppppp/2n5/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "d2d4", 371));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "b1c3", 370));
		openings.add(new OpeningPosition("r1bqkbnr/pp1ppppp/2n5/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq -", "c5d4", 368));
		openings.add(new OpeningPosition("r1bqkbnr/pp1ppppp/2n5/8/3pP3/5N2/PPP2PPP/RNBQKB1R w KQkq -", "f3d4", 366));
		openings.add(new OpeningPosition("r1bqkbnr/1ppp1ppp/p1n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq -", "b5a4", 361));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/2PP4/2N5/PP2PPPP/R1BQKBNR b KQkq -", "f8b4", 320));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "c7c6", 320));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq -", "g8f6", 315));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -", "g1f3", 311));
		openings.add(new OpeningPosition("r1bqkbnr/1ppp1ppp/p1n5/4p3/B3P3/5N2/PPPP1PPP/RNBQK2R b KQkq -", "g8f6", 306));
		openings.add(new OpeningPosition("rnbqkb1r/pppppp1p/5np1/8/2PP4/2N5/PP2PPPP/R1BQKBNR b KQkq -", "f8g7", 291));
		openings.add(new OpeningPosition("r1bqkb1r/1ppp1ppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQK2R w KQkq -", "e1g1", 285));
		openings.add(new OpeningPosition("rnbqk2r/ppppppbp/5np1/8/2PP4/2N5/PP2PPPP/R1BQKBNR w KQkq -", "e2e4", 284));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -", "e7e6", 284));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/2p5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "d2d4", 272));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq -", "e7e5", 271));
		openings.add(new OpeningPosition("rnbqk2r/ppppppbp/5np1/8/2PPP3/2N5/PP3PPP/R1BQKBNR b KQkq -", "d7d6", 270));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/2p5/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq -", "d7d5", 270));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "c7c6", 261));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R b KQkq -", "g8f6", 244));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/2N5/PPP2PPP/R1BQKBNR b KQkq -", "f8b4", 242));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/2N5/PPP2PPP/R1BQKBNR b KQkq -", "g8f6", 232));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "g1f3", 232));
		openings.add(new OpeningPosition("r1bqkbnr/pp1ppppp/2n5/8/3NP3/8/PPP2PPP/RNBQKB1R b KQkq -", "g8f6", 229));
		openings.add(new OpeningPosition("r1bqkb1r/pp1ppppp/2n2n2/8/3NP3/8/PPP2PPP/RNBQKB1R w KQkq -", "b1c3", 228));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/5N2/PPP1PPPP/RNBQKB1R b KQkq -", "e7e6", 224));
		openings.add(new OpeningPosition("rnbqkbnr/pp1p1ppp/4p3/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "d2d4", 214));
		openings.add(new OpeningPosition("rnbqkbnr/pp1p1ppp/4p3/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq -", "c5d4", 213));
		openings.add(new OpeningPosition("rnbqkbnr/pp1p1ppp/4p3/8/3pP3/5N2/PPP2PPP/RNBQKB1R w KQkq -", "f3d4", 212));
		openings.add(new OpeningPosition("rnbqk1nr/ppp2ppp/4p3/3p4/1b1PP3/2N5/PPP2PPP/R1BQKBNR w KQkq -", "e4e5", 211));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/5N2/PPP1PPPP/RNBQKB1R b KQkq -", "g7g6", 203));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq -", "b7b6", 198));
		openings.add(new OpeningPosition("rnbqkb1r/1p2pppp/p2p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "c1e3", 188));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "c7c5", 182));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "e7e6", 179));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "d7d6", 177));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq -", "g8f6", 173));
		openings.add(new OpeningPosition("rnbqkb1r/ppp1pppp/5n2/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R w KQkq -", "c2c4", 171));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "f1c4", 170));
		openings.add(new OpeningPosition("rnbq1rk1/ppp1ppbp/3p1np1/8/2PPP3/2N2N2/PP3PPP/R1BQKB1R w KQ -", "f1e2", 166));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq -", "f7f5", 166));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "b1d2", 165));
		openings.add(new OpeningPosition("r1bqkb1r/1ppp1ppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQ1RK1 b kq -", "f8e7", 163));
		openings.add(new OpeningPosition("r1bqkb1r/pp1ppppp/2n2n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R b KQkq -", "e7e5", 161));
		openings.add(new OpeningPosition("rnbqk2r/pppp1ppp/4pn2/8/1bPP4/2N5/PP2PPPP/R1BQKBNR w KQkq -", "d1c2", 161));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "c2c3", 161));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -", "b1c3", 160));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/2p2n2/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq -", "b1c3", 159));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -", "g8f6", 159));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "b1c3", 158));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq -", "b1c3", 156));
		openings.add(new OpeningPosition("r1bqkb1r/pp1p1ppp/2n2n2/4p3/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "d4b5", 155));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/3p4/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq -", "g8f6", 153));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "b1c3", 153));
		openings.add(new OpeningPosition("r1bqkb1r/pp1p1ppp/2n2n2/1N2p3/4P3/2N5/PPP2PPP/R1BQKB1R b KQkq -", "d7d6", 152));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/3p4/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "d2d4", 152));
		openings.add(new OpeningPosition("rnbqk1nr/ppp2ppp/4p3/3pP3/1b1P4/2N5/PPP2PPP/R1BQKBNR b KQkq -", "c7c5", 151));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "e4e5", 151));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p4/3PP3/2N5/PPP2PPP/R1BQKBNR w KQkq -", "c1g5", 147));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -", "b1c3", 147));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq -", "e7e6", 145));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3pP3/3P4/8/PPP2PPP/RNBQKBNR b KQkq -", "c7c5", 144));
		openings.add(new OpeningPosition("r1bqk2r/1pppbppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQ1RK1 w kq -", "f1e1", 143));
		openings.add(new OpeningPosition("r1bqk2r/2ppbppp/p1n2n2/1p2p3/B3P3/5N2/PPPP1PPP/RNBQR1K1 w kq -", "a4b3", 142));
		openings.add(new OpeningPosition("r1bqk2r/1pppbppp/p1n2n2/4p3/B3P3/5N2/PPPP1PPP/RNBQR1K1 b kq -", "b7b5", 142));
		openings.add(new OpeningPosition("rnbq1rk1/ppp1ppbp/3p1np1/8/2PPP3/2N2N2/PP2BPPP/R1BQK2R b KQ -", "e7e5", 142));
		openings.add(new OpeningPosition("rnbqkb1r/1p2pppp/p2p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "c1g5", 142));
		openings.add(new OpeningPosition("rnbqkb1r/pp1ppppp/5n2/2p5/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "d4d5", 141));
		openings.add(new OpeningPosition("rnbqkb1r/ppp1pppp/3p1n2/8/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "b1c3", 140));
		openings.add(new OpeningPosition("rnbqkb1r/p1pp1ppp/1p2pn2/8/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq -", "g2g3", 140));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "d7d5", 140));
		openings.add(new OpeningPosition("rnbqk2r/ppp1ppbp/3p1np1/8/2PPP3/2N2N2/PP3PPP/R1BQKB1R b KQkq -", "e8g8", 137));
		openings.add(new OpeningPosition("r1bqkb1r/pp3ppp/2np1n2/1N2p1B1/4P3/2N5/PPP2PPP/R2QKB1R b KQkq -", "a7a6", 135));
		openings.add(new OpeningPosition("rnbqk1nr/pp3ppp/4p3/2ppP3/1b1P4/2N5/PPP2PPP/R1BQKBNR w KQkq -", "a2a3", 133));
		openings.add(new OpeningPosition("rnbqkb1r/1p2pppp/p2p1n2/6B1/3NP3/2N5/PPP2PPP/R2QKB1R b KQkq -", "e7e6", 133));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/4p3/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq -", "e5d4", 133));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "e4d5", 130));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "d2d4", 130));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "g1f3", 129));
		openings.add(new OpeningPosition("rnbqkb1r/pppppp1p/5np1/8/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq -", "f8g7", 128));
		openings.add(new OpeningPosition("rnbqk1nr/pp3ppp/4p3/2ppP3/3P4/P1b5/1PP2PPP/R1BQKBNR w KQkq -", "b2c3", 125));
		openings.add(new OpeningPosition("rnbqk1nr/pp3ppp/4p3/2ppP3/1b1P4/P1N5/1PP2PPP/R1BQKBNR b KQkq -", "b4c3", 125));
		openings.add(new OpeningPosition("r1bqkb1r/1p3ppp/p1np1n2/1N2p1B1/4P3/2N5/PPP2PPP/R2QKB1R w KQkq -", "b5a3", 124));
		openings.add(new OpeningPosition("rnbqk2r/ppp1ppbp/3p1np1/8/2PPP3/2N5/PP3PPP/R1BQKBNR w KQkq -", "f2f3", 124));
		openings.add(new OpeningPosition("rnbqkb1r/ppp1pppp/3p1n2/8/3PP3/2N5/PPP2PPP/R1BQKBNR b KQkq -", "g7g6", 124));
		openings.add(new OpeningPosition("r1bqkb1r/pp3ppp/2np1n2/1N2p3/4P3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "c1g5", 123));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/3p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R b KQkq -", "b8c6", 122));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/5N2/PPP1PPPP/RNBQKB1R b KQkq -", "d7d5", 121));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/3p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R b KQkq -", "g7g6", 119));
		openings.add(new OpeningPosition("rnbqk2r/ppp1ppbp/3p1np1/8/2PPP3/2N2P2/PP4PP/R1BQKBNR b KQkq -", "e8g8", 117));
		openings.add(new OpeningPosition("rnbqkb1r/1p3ppp/p2ppn2/6B1/3NP3/2N5/PPP2PPP/R2QKB1R w KQkq -", "f2f4", 116));
		openings.add(new OpeningPosition("rnbqkbnr/pp1p1ppp/4p3/8/3NP3/8/PPP2PPP/RNBQKB1R b KQkq -", "a7a6", 116));
		openings.add(new OpeningPosition("rnbqkb1r/pp3ppp/2p1pn2/3p4/2PP4/2N1PN2/PP3PPP/R1BQKB1R b KQkq -", "b8d7", 115));
		openings.add(new OpeningPosition("rnbqk2r/ppppppbp/5np1/8/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq -", "b1c3", 114));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/8/3pP3/5N2/PPP2PPP/RNBQKB1R w KQkq -", "f3d4", 114));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq -", "g8f6", 114));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq -", "c7c5", 114));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq -", "d7d5", 112));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/8/3Pp3/2N5/PPP2PPP/R1BQKBNR w KQkq -", "c3e4", 110));
		openings.add(new OpeningPosition("rnbqkb1r/pppppp1p/5np1/8/2PP4/2N5/PP2PPPP/R1BQKBNR b KQkq -", "d7d5", 110));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/3PP3/2N5/PPP2PPP/R1BQKBNR b KQkq -", "d5e4", 110));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3p4/2PP4/8/PP2PPPP/RNBQKBNR b KQkq -", "d5c4", 110));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pppp/2p2n2/3p4/2PP4/2N2N2/PP2PPPP/R1BQKB1R b KQkq -", "e7e6", 109));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "b1c3", 109));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/3P4/5N2/PPP1PPPP/RNBQKB1R w KQkq -", "c2c4", 109));
		openings.add(new OpeningPosition("rnbqkb1r/pppppp1p/5np1/8/3P4/5N2/PPP1PPPP/RNBQKB1R w KQkq -", "c2c4", 108));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq -", "g8f6", 108));
		openings.add(new OpeningPosition("rnbqkb1r/pp3ppp/2p1pn2/3p4/2PP4/2N2N2/PP2PPPP/R1BQKB1R w KQkq -", "c1g5", 107));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "f2f4", 107));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "g8f6", 107));
		openings.add(new OpeningPosition("rnbq1rk1/ppp1ppbp/3p1np1/8/2PPP3/2N2P2/PP4PP/R1BQKBNR w KQ -", "c1e3", 106));
		openings.add(new OpeningPosition("rnbqk1nr/pp3ppp/4p3/2ppP3/3P4/P1P5/2P2PPP/R1BQKBNR b KQkq -", "g8e7", 106));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/2P5/2N5/PP1PPPPP/R1BQKBNR b KQkq -", "g8f6", 106));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq -", "b8c6", 105));
		openings.add(new OpeningPosition("r1bqkb1r/pp2pppp/2np1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "c1g5", 104));
		openings.add(new OpeningPosition("rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq -", "d8d5", 102));
		openings.add(new OpeningPosition("rnbqkbnr/pp3ppp/4p3/2ppP3/3P4/8/PPP2PPP/RNBQKBNR w KQkq -", "c2c3", 100));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "f2f4", 98));
		openings.add(new OpeningPosition("rnbqkb1r/1p2pppp/p2p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "f1e2", 97));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -", "g2g3", 97));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/2PP4/2N5/PP2PPPP/R1BQKBNR b KQkq -", "g8f6", 96));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "b8c6", 96));
		openings.add(new OpeningPosition("r1bqk2r/2ppbppp/p1n2n2/1p2p3/4P3/1B3N2/PPPP1PPP/RNBQR1K1 b kq -", "e8g8", 95));
		openings.add(new OpeningPosition("rnbqkb1r/ppp1pppp/5n2/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R w KQkq -", "c1f4", 94));
		openings.add(new OpeningPosition("r1bqkb1r/1p3ppp/p1np1n2/4p1B1/4P3/N1N5/PPP2PPP/R2QKB1R b KQkq -", "b7b5", 93));
		openings.add(new OpeningPosition("rn1qkb1r/pbpp1ppp/1p2pn2/8/2PP4/5NP1/PP2PP1P/RNBQKB1R w KQkq -", "f1g2", 93));
		openings.add(new OpeningPosition("rnbqkb1r/p1pp1ppp/1p2pn2/8/2PP4/5NP1/PP2PP1P/RNBQKB1R b KQkq -", "c8b7", 93));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -", "c1g5", 93));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/5n2/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq -", "f3e5", 93));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pp1p/3p1np1/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -", "c1e3", 92));
		openings.add(new OpeningPosition("rnbqkb1r/1p2pppp/p2p1n2/8/3NP3/2N1B3/PPP2PPP/R2QKB1R b KQkq -", "e7e5", 91));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/2PP4/8/PP2PPPP/RNBQKBNR w KQkq -", "b1c3", 91));
		openings.add(new OpeningPosition("r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq -", "g8f6", 90));
		openings.add(new OpeningPosition("rnbq1rk1/ppp2pbp/3p1np1/4p3/2PPP3/2N2N2/PP2BPPP/R1BQK2R w KQ -", "e1g1", 89));
		openings.add(new OpeningPosition("rnbqkb1r/pp2pp1p/3p1np1/8/3NP3/2N1B3/PPP2PPP/R2QKB1R b KQkq -", "f8g7", 89));
		openings.add(new OpeningPosition("rnbqk2r/ppp1ppbp/3p1np1/8/2PPP3/2N5/PP3PPP/R1BQKBNR w KQkq -", "g1f3", 88));
		openings.add(new OpeningPosition("rnbqk2r/pppp1ppp/4pn2/8/1bPP4/2N5/PPQ1PPPP/R1B1KBNR b KQkq -", "e8g8", 88));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "e4e5", 88));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/5n2/4p3/2P5/2N5/PP1PPPPP/R1BQKBNR w KQkq -", "g1f3", 87));
		openings.add(new OpeningPosition("rnb1kbnr/ppp1pppp/8/3q4/8/8/PPPP1PPP/RNBQKBNR w KQkq -", "b1c3", 86));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/4P3/8/8/PPPP1PPP/RNBQKBNR b KQkq -", "f6d5", 86));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p4/3PP3/2N5/PPP2PPP/R1BQKBNR w KQkq -", "e4e5", 85));
		openings.add(new OpeningPosition("r1bqkb1r/pp2pppp/2np1n2/6B1/3NP3/2N5/PPP2PPP/R2QKB1R b KQkq -", "e7e6", 85));
		openings.add(new OpeningPosition("rnbqkbnr/ppp2ppp/4p3/3p4/3PP3/8/PPPN1PPP/R1BQKBNR b KQkq -", "g8f6", 84));
		openings.add(new OpeningPosition("rnbqkbnr/pppp1ppp/8/4p3/4PP2/8/PPPP2PP/RNBQKBNR b KQkq -", "e5f4", 84));
		openings.add(new OpeningPosition("rnbqkb1r/pppppppp/5n2/8/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -", "g2g3", 83));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -", "d2d4", 83));
		openings.add(new OpeningPosition("rnbqk2r/pp2ppbp/3p1np1/8/3NP3/2N1B3/PPP2PPP/R2QKB1R w KQkq -", "f2f3", 82));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq -", "e7e6", 82));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3pP3/3P4/2N5/PPP2PPP/R1BQKBNR b KQkq -", "f6d7", 81));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/5n2/4p3/2P5/2N2N2/PP1PPPPP/R1BQKB1R b KQkq -", "b8c6", 81));
		openings.add(new OpeningPosition("rnbqkbnr/pp1ppppp/8/2p5/3PP3/8/PPP2PPP/RNBQKBNR b KQkq -", "c5d4", 81));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", "b2b3", 81));
		openings.add(new OpeningPosition("rnbqkbnr/pppppp1p/6p1/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq -", "f8g7", 80));
		openings.add(new OpeningPosition("rnbqk2r/pppp1ppp/4pn2/8/1bPP4/2N5/PP2PPPP/R1BQKBNR w KQkq -", "e2e3", 80));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/2N2N2/PP2PPPP/R1BQKB1R b KQkq -", "f8e7", 79));
		openings.add(new OpeningPosition("rnbqk2r/pp2nppp/4p3/2ppP3/3P4/P1P5/2P2PPP/R1BQKBNR w KQkq -", "d1g4", 78));
		openings.add(new OpeningPosition("r1bqkb1r/pp3ppp/2nppn2/6B1/3NP3/2N5/PPP2PPP/R2QKB1R w KQkq -", "d1d2", 78));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p2B1/3PP3/2N5/PPP2PPP/R2QKBNR b KQkq -", "f8e7", 77));
		openings.add(new OpeningPosition("rnbqkb1r/ppp2ppp/4pn2/3p4/3PP3/8/PPPN1PPP/R1BQKBNR w KQkq -", "e4e5", 77));
		openings.add(new OpeningPosition("rnbqkb1r/pp3ppp/2p1pn2/3p4/2PP4/2N2N2/PP2PPPP/R1BQKB1R w KQkq -", "e2e3", 76));
		openings.add(new OpeningPosition("rnbqkb1r/ppp1pppp/5n2/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq -", "e7e6", 76));
		openings.add(new OpeningPosition("rnbqkb1r/pppp1ppp/4pn2/8/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq -", "d7d5", 76));
		openings.add(new OpeningPosition("r1bqkbnr/1ppp1ppp/p1n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq -", "b5c6", 76));
		openings.add(new OpeningPosition("rnbqkbnr/pp2pppp/2p5/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq -", "e4e5", 76));
		openings.add(new OpeningPosition("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -", "g7g6", 76));
	}
	
	public int getMove(String fen)
	{
		OpeningPosition position = null;
		ArrayList<OpeningPosition> matched = new ArrayList<OpeningPosition>();
		
		int size = openings.size();
		//Log.i("Book", "Size " + size);
		for (int i=0; i<size; i++)
		{
			position = openings.get(i);
			String parts[] = position.fen.split(" ");
			String fen1 = parts[0] + parts[1];
			parts = fen.split(" ");
			String fen2 = parts[0] + parts[1];
			//Log.i("Book", "Fen1 " + fen1);
			//Log.i("Book", "Fen2 " + fen2);
			
			if (fen1.equals(fen2))
			{
				//Log.i("Book", "Matched " + position.fen);
				matched.add(position);
			}
		}
		
		size = matched.size();
		if (size > 0)
		{
			int range = 0;
			for (int i=0; i<size; i++)
			{
				position = matched.get(i);
				range += position.frequency;
			}
			Random r = new Random();
			int movePositionInRange = r.nextInt(range);
			range = 0;
			for (int i=0; i<size; i++)
			{
				position = matched.get(i);
				range += position.frequency;
				if (movePositionInRange < range)
				{
					return ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(position.move);		
				}
			}
		}
		
		return 0;
	}
}
