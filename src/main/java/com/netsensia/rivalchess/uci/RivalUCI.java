package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;

import java.io.PrintStream;

public final class RivalUCI 
{
	final static RivalSearch m_engine = new RivalSearch(System.out);

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
		
		m_engine.startEngineTimer(true);
		m_engine.setHashSizeMB(32);
		
		new Thread(m_engine).start();
		
		UCIController uciController = new UCIController(
				m_engine,
				timeMultiple,
				new PrintStream(System.out));

		new Thread(uciController).start();
	}
}
