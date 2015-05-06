package com.netadapt.rivalchess.uci;

import java.io.PrintWriter;
import java.util.TimerTask;

import com.netadapt.rivalchess.engine.core.RivalConstants;
import com.netadapt.rivalchess.engine.core.RivalSearch;
import com.netadapt.rivalchess.util.ChessBoardConversion;
import com.netadapt.rivalchess.util.Logger;

public class EngineMonitor extends TimerTask
{
	private RivalSearch m_engine;
	private static PrintWriter m_out;
	
	public EngineMonitor(RivalSearch engine)
	{
		m_engine = engine;
	}
	
	public static void setWriter(PrintWriter out)
	{
		m_out = out;
	}
	
	public static void sendUCI(String s)
	{
		System.out.println(s);
    	if (RivalConstants.UCI_DEBUG)
    	{
			Logger.log(m_out, s, "<");
    	}
	}
	
	public void printInfo()
	{
		int depth = m_engine.getIterativeDeepeningDepth();
		sendUCI(
					"info" +
					" currmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(m_engine.m_currentDepthZeroMove) +
					" currmovenumber " + m_engine.m_currentDepthZeroMoveNumber +
					" depth " + depth + 
					" score " + m_engine.getCurrentScoreHuman() +
					" pv " + m_engine.getCurrentPathString().trim() + 
					" time " + m_engine.getSearchDuration() + 
					" nodes " + m_engine.getNodes() +
					" nps " + m_engine.getNodesPerSecond());
	}
	
	public void run()
	{
		m_engine.m_currentTimeMillis = System.currentTimeMillis();
		
		if (m_engine.isUCIMode())
		{
			if (m_engine.m_isOkToSendInfo)
			{
				int state = m_engine.getEngineState(); 
				if (state == RivalConstants.SEARCHSTATE_SEARCHING && !m_engine.isAbortingSearch())
				{
					printInfo();
				}
			}
		}
	}
}
