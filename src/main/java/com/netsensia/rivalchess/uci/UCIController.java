package com.netsensia.rivalchess.uci;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netsensia.rivalchess.engine.core.Bitboards;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.engine.test.epd.EPDRunner;
import com.netsensia.rivalchess.model.board.BoardModel;
import com.netsensia.rivalchess.model.board.FenChess;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.Logger;

public class UCIController implements Runnable {

	static boolean m_isDebug = false;
	static int m_whiteTime;
	static int m_blackTime;
	static int m_whiteInc;
	static int m_blackInc;
	static int m_movesToGo;
	static int m_maxDepth;
	static int m_maxNodes;
	static int m_mateInX;
	static int m_moveTime;
	static int m_hashSizeMB = 128;
	static Bitboards m_bitboards;
	static boolean m_isInfinite;
	
	private static FileWriter fstream;
	private static PrintWriter out;
 	
	static RivalSearch m_engine;
	private static int m_timeMultiple = 1;
	static EngineMonitor m_monitor;
	
	public UCIController(RivalSearch engine, int timeMultiple)
	{
		m_engine = engine;
		m_timeMultiple = timeMultiple;
	}
	
	@Override
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String s;

		BoardModel m_boardModel = new BoardModel();
		FenChess m_fenChess = new FenChess( m_boardModel );
		m_bitboards = new Bitboards();
		EngineChessBoard m_engineBoard = new EngineChessBoard(m_bitboards);
		Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MMMdd-HH-mm-ss-S");
		String formattedDate = formatter.format(todaysDate);
		m_engine.setUseOpeningBook(false);
		
	    try {
	    	if (RivalConstants.UCI_DEBUG)
	    	{
				fstream = new FileWriter(RivalConstants.UCI_DEBUG_FILEPATH + "ucidebug-" + formattedDate + ".log",true);
				out = new PrintWriter(fstream);
				EngineMonitor.setWriter(out);
				m_engine.setLogWriter(out);
	    	}
			
			while ((s = in.readLine()) != null)
			{
				uciDebug("Received input: " + s);
				if (s.trim().length() != 0)
				{
					Logger.log(out, s, ">");
					String[] parts = s.split(" ");
					int l = parts.length;
					if (l > 0)
					{
						if (parts[0].equals("uci"))
						{
							EngineMonitor.sendUCI("id name Rival (build " + RivalConstants.VERSION + ")");
							EngineMonitor.sendUCI("id author Chris Moreton");
							EngineMonitor.sendUCI("option name Hash type spin default 1 min 1 max 256");
							EngineMonitor.sendUCI("uciok");
						}
						if (parts[0].equals("var"))
						{
							showEngineVar(parts[1]);
						}
						if (parts[0].equals("perft"))
						{
							perftTest();
						}
						if (parts[0].equals("epd"))
						{
							Pattern p = Pattern.compile("epd \"(.*)\" (.*) (.*)");
							Matcher m = p.matcher(s);
							if (m.find())
							{
								EPDRunner epdRunner = new EPDRunner();
								epdRunner.go(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
							}
						}
						if (parts[0].equals("debug"))
						{
							waitForSearchToComplete();
							m_isDebug = parts[1].equals("On"); 
						}
						if (parts[0].equals("isready"))
						{
							EngineMonitor.sendUCI("readyok");
						}
						if (parts[0].equals("ucinewgame"))
						{
							waitForSearchToComplete();
							m_engine.newGame();
						}
						if (parts[0].equals("position"))
						{
							waitForSearchToComplete();
							if (parts[1].equals("startpos"))
							{
								m_fenChess.setFromStr( EngineChessBoard.START_POS );
							}
							else
							{
								m_fenChess.setFromStr( s.substring(12).trim() );
							}
		
							m_engineBoard.setBoard(m_boardModel);
							m_engine.setBoard(m_engineBoard);
							
							if (l > 2)
							{
								
								for (int pos=2; pos<l; pos++)
								{
									if (parts[pos].equals("moves"))
									{
										for (int i=pos+1; i<l; i++)
										{
											m_engine.m_board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(parts[i]));
										}
										break;
									}
								}
							}
							if (RivalConstants.UCI_DEBUG)
							{
								m_engine.m_board.printLegalMoves();
								m_engine.m_board.printBoard();
							}
						}
						if (parts[0].equals("go"))
						{
							m_whiteTime = -1;
							m_blackTime = -1;
							m_whiteInc = -1;
							m_blackInc = -1;
							m_movesToGo = 0;
							m_moveTime = -1;
							m_isInfinite = false;
							m_maxDepth = -1;
							for (int i=1; i<l; i++)
							{
								if (parts[i].equals("searchmoves"))
								{
								}
								if (parts[i].equals("ponder"))
								{
								}
								if (parts[i].equals("wtime"))
								{
									m_whiteTime = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("btime"))
								{
									m_blackTime = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("winc"))
								{
									m_whiteInc = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("binc"))
								{
									m_blackInc = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("movestogo"))
								{
									m_movesToGo = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("depth"))
								{
									m_maxDepth = Integer.parseInt(parts[i+1]);
									m_engine.setSearchDepth(m_maxDepth);
								}
								if (parts[i].equals("nodes"))
								{
									m_maxNodes = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("mate"))
								{
									m_mateInX = Integer.parseInt(parts[i+1]);
								}
								if (parts[i].equals("movetime"))
								{
									m_moveTime = Integer.parseInt(parts[i+1]) * m_timeMultiple;
								}
								if (parts[i].equals("infinite"))
								{
									m_isInfinite = true;
								}
							}
							
							if (m_isInfinite)
							{
								m_engine.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
								m_engine.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH-2);
								m_engine.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
							}
							else
							if (m_moveTime != -1)
							{
								m_engine.setMillisToThink(m_moveTime);
								m_engine.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH-2);
								m_engine.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
							}
							else
							if (m_maxDepth != -1)
							{
								m_engine.setSearchDepth(m_maxDepth);
								m_engine.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
								m_engine.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
							}
							else
							if (m_maxNodes != -1)
							{
								m_engine.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH-2);
								m_engine.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
								m_engine.setNodesToSearch(m_maxNodes);
							}
							else
							if (m_whiteTime != -1)
							{
					    		int calcTime = (m_engineBoard.m_isWhiteToMove ? m_whiteTime : m_blackTime) / (m_movesToGo == 0 ? 120 : m_movesToGo);
					    		int guaranteedTime = (m_engineBoard.m_isWhiteToMove ? m_whiteInc : m_blackInc);
					    		int timeToThink = calcTime + guaranteedTime - RivalConstants.UCI_TIMER_SAFTEY_MARGIN_MILLIS;
					    		uciDebug("I am going to think for " + timeToThink + " millis");
								m_engine.setMillisToThink(timeToThink);
								m_engine.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH-2);
							}
							
							m_engine.startSearch();
						}
						if (parts[0].equals("setoption"))
						{
							if (parts[1].equals("name"))
							{
								if (parts[2].equals("Clear") && parts[3].equals("Hash"))
								{
									m_engine.clearHash();
								}
								if (parts[2].equals("Hash"))
								{
									if (parts[3].equals("value"))
									{
										m_hashSizeMB = Integer.parseInt(parts[4]);
										m_engine.setHashSizeMB(m_hashSizeMB);
									}					
								}					
								if (parts[2].equals("OwnBook"))
								{
									if (parts[3].equals("value"))
									{
										if (parts[4].equals("true"))
										{
											m_engine.setUseOpeningBook(true);
										}
										else
										{
											m_engine.setUseOpeningBook(false);
										}
									}					
								}					
							}
						}
						if (parts[0].equals("stop"))
						{
							uciDebug("UCI stop command received");
							m_engine.stopSearch();
							waitForSearchToComplete();
						}
						if (parts[0].equals("quit"))
						{
							System.exit(0);
						}
					}
				}
			}
			uciDebug("That's it - I'm outta here");
			out.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void waitForSearchToComplete()
	{
		int state;
		m_engine.stopSearch();
		do
		{
			state = m_engine.getEngineState();
			uciDebug("Waiting for it all to end...");
		}
		while (state != RivalConstants.SEARCHSTATE_READY && state != RivalConstants.SEARCHSTATE_SEARCHCOMPLETE);
	}
	
	public static void uciDebug(String s)
	{
		if (RivalConstants.UCI_DEBUG)
		{
			System.out.println(s);
			Logger.log(out, s, "D");
		}
	}

	public static int[][] m_legalMoves;
	
	public static long verifyPerftScore(String fen, int depth, long correctNodeCount)
	{
		EngineChessBoard engineBoard = new EngineChessBoard(new Bitboards());
		engineBoard.setBoard(getBoardModel(fen));
		
		//engineBoard.printBoard();
		//engineBoard.printLegalMoves();
		System.out.print("Calculating Perft Value for " + fen + " at depth " + depth + "...");
		long nodes = getPerft(engineBoard, depth);
		System.out.println(" = " + nodes);
		if (nodes != correctNodeCount)
		{
			System.out.println("*********************");
			System.out.println("ERROR IN Perft Test!!");
			System.out.println("*********************");
			System.exit(0);
		}
		return nodes;
	}
	
	public static BoardModel getBoardModel( String fen )
	{
		BoardModel boardModel = new BoardModel();
		FenChess fenChess = new FenChess( boardModel );
		String invertedFEN = invertFEN(fen);
		
		fenChess.setFromStr( invertedFEN );
	
		RivalSearch testSearcher = new RivalSearch();
		EngineChessBoard testBoard = new EngineChessBoard(new Bitboards());
		testBoard.setBoard(boardModel);
		int eval1 = testSearcher.evaluate(testBoard);

		fenChess.setFromStr( fen );
		testBoard.setBoard(boardModel);
		int eval2 = testSearcher.evaluate(testBoard);
		
		if (eval1 != eval2)
		{
			System.out.println("Eval flip error");
			System.out.println(fen);
			System.out.println(invertedFEN);
			System.out.println(eval1 + " v " + eval2);
			System.exit(0);
		}
		
		return boardModel;
	}	
	
	public static String invertFEN(String fen)
	{
		fen = fen.replace(" b ", " . ");
		fen = fen.replace(" w ", " ; ");

		fen = fen.replace('Q', 'z');
		fen = fen.replace('K', 'x');
		fen = fen.replace('N', 'c');
		fen = fen.replace('B', 'v');
		fen = fen.replace('R', 'm');
		fen = fen.replace('P', ',');

		fen = fen.replace('q', 'Q');
		fen = fen.replace('k', 'K');
		fen = fen.replace('n', 'N');
		fen = fen.replace('b', 'B');
		fen = fen.replace('r', 'R');
		fen = fen.replace('p', 'P');

		fen = fen.replace('z', 'q');
		fen = fen.replace('x', 'k');
		fen = fen.replace('c', 'n');
		fen = fen.replace('v', 'b');
		fen = fen.replace('m', 'r');
		fen = fen.replace(',', 'p');

		fen = fen.replace(" . ", " w ");
		fen = fen.replace(" ; ", " b ");
		
		String[] fenParts = fen.split(" ");
		String[] boardParts = fenParts[0].split("/");

		String newFen = 
			boardParts[7] + "/" + 
			boardParts[6] + "/" + 
			boardParts[5] + "/" + 
			boardParts[4] + "/" + 
			boardParts[3] + "/" + 
			boardParts[2] + "/" + 
			boardParts[1] + "/" + 
			boardParts[0];
		
		for (int i=1; i<fenParts.length; i++)
		{
			newFen += " " + fenParts[i];
		}
		
		return newFen;
	}
	
	public static void perftTest()
	{
		long totalNodes = 0;
		m_legalMoves = new int[10][RivalConstants.MAX_LEGAL_MOVES];
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(0);
		
		long t1 = System.currentTimeMillis();
		 
		totalNodes += verifyPerftScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", 4, 20541);
		totalNodes += verifyPerftScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 6, 38633283);
		totalNodes += verifyPerftScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609);
		totalNodes += verifyPerftScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - 1 67", 2, 279);
		totalNodes += verifyPerftScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 5, 11139762);
		totalNodes += verifyPerftScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 7, 178633661);
		totalNodes += verifyPerftScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 5, 193690690);

		long t2 = System.currentTimeMillis() - t1;
		long nps = (long)(((double)totalNodes / t2) * 1000);
		System.out.println("(" + totalNodes + "/" + t2 + ")*1000 = " + nf.format(nps) + " nps");

		System.out.println("Move generation test passed");

		System.exit(0);
	}
	
	public static long getPerft(EngineChessBoard board, int depth)
	{
		if (depth == 0) return 1;
		long nodes = 0;
		int moveNum = 0;
		
		int[] legalMoves = m_legalMoves[depth];
		
		board.setLegalMoves(legalMoves);
		while (legalMoves[moveNum] != 0)
		{
			if (board.makeMove(legalMoves[moveNum])) {
				//board.printBoard();
				nodes += getPerft(board, depth-1);
				board.unMakeMove();
				//board.printBoard();
			}
			moveNum ++;
		}
		
		return nodes;
	}
	
	public static void showEngineVar(String varName)
	{
		try
		{
		    Class<?> c = Class.forName("com.netsensia.rivalchess.engine.core.RivalConstants");
		    Field field = c.getDeclaredField(varName);
		    
		    System.out.println("Class: " + c.getName());
		    
		    System.out.println(varName + " = " + field.get(new RivalConstants()));
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
	}

}
