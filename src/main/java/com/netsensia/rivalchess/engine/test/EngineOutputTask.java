package com.netsensia.rivalchess.engine.test;

import java.util.TimerTask;

import com.netsensia.rivalchess.engine.core.RivalSearch;

public class EngineOutputTask extends TimerTask
{
	RivalSearch m_engine;
	public EngineOutputTask(RivalSearch engine)
	{
		this.m_engine = engine;
	}
	
	public void run()
	{
		if (m_engine.getIterativeDeepeningDepth() > 0)
		{
			System.out.println(m_engine.isSearching());
		}
	}
}
