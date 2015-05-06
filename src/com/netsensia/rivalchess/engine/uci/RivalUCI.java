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
