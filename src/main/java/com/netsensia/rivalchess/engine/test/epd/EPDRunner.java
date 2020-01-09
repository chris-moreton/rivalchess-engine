package com.netsensia.rivalchess.engine.test.epd;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netsensia.rivalchess.engine.core.Bitboards;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.model.board.BoardModel;
import com.netsensia.rivalchess.model.board.FenChess;

public class EPDRunner 
{
	public static final String EPD_FILEPATH = "S:\\Java\\Chess Supp\\EPD\\";
	public ArrayList<EPDPosition> positions = new ArrayList<EPDPosition>();
	int correctSoFar = 0;
	int doneSoFar = 0;
	long totalMillisToSolution = 0;
	long nodesSoFar = 0;
	long totalTimeTaken = 0;
	public long sessionStartTime;
	NumberFormat nf = NumberFormat.getInstance();
	NumberFormat nfi = NumberFormat.getInstance();
	EngineChessBoard engineChessBoard;
	RivalSearch rivalSearch;
	BoardModel boardModel;
	FenChess fenChess;
	public int maxMillis;
	
	public EPDRunner()
	{
		engineChessBoard = new EngineChessBoard(new Bitboards());
		rivalSearch = new RivalSearch(System.out);
		boardModel = new BoardModel();
		fenChess = new FenChess(boardModel);
		nf.setMaximumFractionDigits(2);
		nfi.setMaximumFractionDigits(0);
	}
	
	public void doPosition(EPDPosition epdPosition)
	{
		positions.add(epdPosition);
		fenChess.setFromStr(epdPosition.fen);
		engineChessBoard.setBoard(boardModel);
		rivalSearch.newGame();
		rivalSearch.setBoard(engineChessBoard);
		rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH);
		rivalSearch.setMillisToThink(epdPosition.maxMillis);
		rivalSearch.setUseOpeningBook(false);
		rivalSearch.startEngineTimer(false);
		epdPosition.setStartTime(System.currentTimeMillis());
		rivalSearch.setEPDPosition(epdPosition);
		rivalSearch.setHashSizeMB(128);
		rivalSearch.go();

		epdPosition.setEndTime(System.currentTimeMillis());

		int nodes = rivalSearch.getNodes();
		long timeTaken = epdPosition.totalTimeUsed();
		int millisToSolution = epdPosition.millisWhenFirstCorrect();

		totalTimeTaken += timeTaken; 
		doneSoFar ++;
		nodesSoFar += nodes;
		
		double nps = ((double)nodes / timeTaken) * 1000.0;
		double totalNPS = ((double)nodesSoFar / totalTimeTaken) * 1000.0;
		boolean correct = millisToSolution != -1;
		
		if (correct)
		{
			correctSoFar ++;
		}
		else
		{
			millisToSolution = epdPosition.maxMillis;
		}

		totalMillisToSolution += millisToSolution;
		
		System.out.println(
				epdPosition.id + "," + 
				epdPosition.getPlyMovesString() + "," + 
				epdPosition.bestMoves() + "," + 
				(correct ? "Correct" : "[*****F*A*I*L*E*D*****]") + "," + 
				correctSoFar + "/" + 
				doneSoFar + "," + 
				millisToSolution + "," +
				(int)nps + "," +
				totalMillisToSolution + "," + 
				totalTimeTaken + "," + 
				(int)totalNPS);
		
	}
	
	public void go(String filepath, int extraPlies, int maxMillis)
	{
		this.maxMillis = maxMillis;
		sessionStartTime = System.currentTimeMillis();
		FileInputStream fis;
		try 
		{
			fis = new FileInputStream(filepath);
		} 
		catch (FileNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	    DataInputStream in = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        
		System.out.println("ID,Rival,Best,Result,Correct,Solved In,NPS,Solved In (Total),Time Taken (Total),NPS (Total)");
        
	    try 
	    {
			while ((line = br.readLine()) != null)
			{
				if (!line.trim().equals(""))
				{
					String parts[] = line.split("bm");
					String fen = parts[0].trim();
					String rest = parts[1];
					parts = rest.split(";");
					String bestMoves[] = parts[0].trim().split(" ");
					
					int count = 1;
					String id = "";
					rest = parts[count];
					Pattern p = Pattern.compile("id (.*)");
					Matcher m = p.matcher(rest);
					if (m.find())
					{
						id = m.group(1);
					}
					if (id.equals("")) System.exit(0);
					
					EPDPosition epdPosition = new EPDPosition(line.trim(), fen, bestMoves, id, maxMillis, extraPlies);
	                doPosition(epdPosition);
				}
			}
			br.close();
	    } catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		displayResults();
		System.exit(0);
	}
	
	public void displayResults()
	{
		long totalTimeTaken = (System.currentTimeMillis() - sessionStartTime);
		System.out.println("Total Session Time = " + nf.format(totalTimeTaken / 1000.0));
		System.out.println("Nodes Per Second = " + nf.format((nodesSoFar / totalTimeTaken) * 1000.0));
		int totalPositions = 0;
		int totalCorrect = 0;
		int timeSegments = (maxMillis / 1000) + 1;
		int solvedIn[] = new int[timeSegments];
		for (int i=0; i<timeSegments; i++) solvedIn[i] = 0;
		
		System.out.println("POSITIONS FAILED");
		System.out.println("================");
		for (EPDPosition position : positions)
		{
			totalPositions ++;
			int timeTaken = position.millisWhenFirstCorrect();
			if (timeTaken != -1)
			{
				totalCorrect ++;
				for (int i=1; i<=timeSegments; i++)
				{
					if (timeTaken < i*1000)
					{
						solvedIn[i-1] ++;
						break;
					}
				}
			}
			else
			{
				System.out.println(position.epd);
			}
		}
		System.out.println("Correct = " + totalCorrect + " / " + totalPositions);
		int total = 0;
		for (int i=1; i<=timeSegments; i++)
		{
			total += solvedIn[i-1];
			System.out.println("Within " + i + "s = " + total);
		}
	}
}
