package com.netadapt.rivalchess.uci;

import com.netadapt.rivalchess.engine.core.RivalSearch;

public final class RivalUCI 
{
	static RivalSearch m_engine;

	public static void main(String[] args)
	{
		int timeMultiple;
		if (args.length > 1 && args[0].equals("tm"))
		{
			timeMultiple = Integer.parseInt(args[1]);
		}
		else
		{
			timeMultiple = 1;
		}
		
		System.out.println("Hi");
		
		m_engine = new RivalSearch();
		m_engine.startEngineTimer(true);
		m_engine.setHashSizeMB(128);
		
		new Thread(m_engine).start();
		
		UCIController uciController = new UCIController(m_engine, timeMultiple);
		new Thread(uciController).start();
	}
}

/*
 * Draw Test
 
position startpos moves b1c3 c7c5 e2e4 d7d6 g1f3 g8f6 d2d4 c5d4 f3d4 a7a6 c1g5 e7e6 f2f4 b8d7 d1e2 d8c7 e1c1 f8e7 d4f3 h7h6 g5h4 d7b6 e4e5 d6e5 e2e5 b6d5 c3d5 c7e5 f4e5 f6d5 h4e7 d5e7 f1c4 e8g8 h1e1 e7c6 f3h4 f7f6 h4g6 f8f7 d1d6 g8h7 c4d3 h7g8 d3c4 g8h7 c4d3 h7g8 e1d1 f7d7 d3e2 d7c7 d6d8 c6d8 d1d8 g8f7 e2h5 c8d7 d8a8 d7c8 g6h4 f7e7
go depth 6

position startpos moves b1c3 c7c5 e2e4 d7d6 g1f3 g8f6 d2d4 c5d4 f3d4 a7a6 c1g5 e7e6 f2f4 b8d7 d1e2 d8c7 e1c1 f8e7 d4f3 h7h6 g5h4 d7b6 e4e5 d6e5 e2e5 b6d5 c3d5 c7e5 f4e5 f6d5 h4e7 d5e7 f1c4 e8g8 h1e1 e7c6 f3h4 f7f6 h4g6 f8f7 d1d6 g8h7 c4d3 h7g8 d3c4 g8h7 c4d3 h7g8 e1d1 f7d7 d3e2 d7c7 d6d8 c6d8 d1d8 g8f7 e2h5 c8d7 d8a8 d7c8 g6h4 f7e7 h4g6 e7f7 g6f4 f7e7
go depth 6

position startpos moves b1c3 c7c5 e2e4 d7d6 g1f3 g8f6 d2d4 c5d4 f3d4 a7a6 c1g5 e7e6 f2f4 b8d7 d1e2 d8c7 e1c1 f8e7 d4f3 h7h6 g5h4 d7b6 e4e5 d6e5 e2e5 b6d5 c3d5 c7e5 f4e5 f6d5 h4e7 d5e7 f1c4 e8g8 h1e1 e7c6 f3h4 f7f6 h4g6 f8f7 d1d6 g8h7 c4d3 h7g8 d3c4 g8h7 c4d3 h7g8 e1d1 f7d7 d3e2 d7c7 d6d8 c6d8 d1d8 g8f7 e2h5 c8d7 d8a8 d7c8 g6h4 f7e7 h4g6 e7f7 g6f4 f7e7 f4g6
go depth 6

*/