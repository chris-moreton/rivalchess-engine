package com.netadapt.rivalchess.model.board;

import java.util.Hashtable;


public class BoardModel implements Cloneable
{	
	public static final char VACANT_TILE = '_';
	public static int s_modelCount = 0;
	
	protected char[] m_boardArray;
	protected int m_numXFiles;
	protected int m_numYRanks;
	protected MoveRef m_lastMove;
	protected int m_enPassantFile = 0;
		
	protected static BoardRef WHITE_QUEEN_SIDE_ROOK_START_BOARD_REF = new BoardRef( 0, 7 );
	protected static BoardRef WHITE_KING_SIDE_ROOK_START_BOARD_REF = new BoardRef( 7, 7 );
	
	protected static BoardRef BLACK_QUEEN_SIDE_ROOK_START_BOARD_REF = new BoardRef( 0, 0 );
	protected static BoardRef BLACK_KING_SIDE_ROOK_START_BOARD_REF = new BoardRef( 7, 0 );
	
	
	protected static MoveRef WHITE_QUEEN_SIDE_CASTLE_KING_MOVE_REF  = new MoveRef( 4,7, 2,7 );
	protected static MoveRef WHITE_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF 	= new MoveRef( 0,7, 3,7 );

	protected static MoveRef WHITE_KING_SIDE_CASTLE_KING_MOVE_REF 	= new MoveRef( 4,7, 6,7 );
	protected static MoveRef WHITE_KING_SIDE_CASTLE_ROOK_MOVE_REF	= new MoveRef( 7,7, 5,7 );
	
	
	protected static MoveRef BLACK_QUEEN_SIDE_CASTLE_KING_MOVE_REF  = new MoveRef( 4,0, 2,0 );
	protected static MoveRef BLACK_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF 	= new MoveRef( 0,0, 3,0 );
	
	protected static MoveRef BLACK_KING_SIDE_CASTLE_KING_MOVE_REF 	= new MoveRef( 4,0, 6,0 );
	protected static MoveRef BLACK_KING_SIDE_CASTLE_ROOK_MOVE_REF 	= new MoveRef( 7,0, 5,0 );
		
	protected static int WHITE_ENPASSANT_CAPTURE_RANK 			= 2;
	protected static int WHITE_ENPASSANT_INITIATE_SOURCE_RANK 	= 6;
	protected static int WHITE_ENPASSANT_INITIATE_TARGET_RANK 	= 4;
	
	protected static int BLACK_ENPASSANT_CAPTURE_RANK 			= 5;
	protected static int BLACK_ENPASSANT_INITIATE_SOURCE_RANK 	= 1;
	protected static int BLACK_ENPASSANT_INITIATE_TARGET_RANK 	= 3;	
	
	protected static int NO_ENPASSANT_OPPORTUNITY_FILE = -1;
			
	protected boolean m_isWhiteKingSideCastleAvailable = true;
	protected boolean m_isWhiteQueenSideCastleAvailable = true;
	protected boolean m_isBlackKingSideCastleAvailable = true;
	protected boolean m_isBlackQueenSideCastleAvailable = true;
	
	protected int m_halfMoveCount = 0;
	protected boolean m_isWhiteToMove = true;
		
	protected int m_gameStateId = 0;
	
	protected String m_instanceStamp;
	
	protected MoveState m_lastMoveState = null;
	
	public BoardModel( )
	{
		this( BoardModelConstants.DEFAULT_BOARD_NUM_FILES, BoardModelConstants.DEFAULT_BOARD_NUM_RANKS );		
	}
	
	public BoardModel( int numXFiles, int numYRanks )
	{		
		this.m_numXFiles = numXFiles;
		this.m_numYRanks = numYRanks;					
		this.m_boardArray = new char[this.m_numYRanks * this.m_numXFiles];		
	}

	//Move piece cleanly on model
	private char pieceShift(  MoveRef moveRef ) 
	{
		char srcPiece = this.getPieceCode( moveRef.getSrcXFile( ), moveRef.getSrcYRank( ) );					
		this.setPieceCode(  moveRef.getTgtXFile( ), moveRef.getTgtYRank( ), srcPiece);  //move piece
		this.removePiece( moveRef.getSrcXFile( ), moveRef.getSrcYRank( ) );  //blank source
		return srcPiece;
	}
	
	public boolean movePiece( MoveRef moveRef  ) //sourceXFile, sourceYRank, targetXFile, targetYRank
	{
		this.m_lastMoveState = new MoveState( );
		//valid move - move piece.
		if (( moveRef.getSrcXFile( ) == moveRef.getTgtXFile( )) && ( moveRef.getSrcYRank( ) ==  moveRef.getTgtYRank( )))
		{
			return false;
		}
		else
		{													
			this.m_lastMoveState.setPieceCode( this.getPieceCode( moveRef.getSrcXFile( ), moveRef.getSrcYRank( )));
			this.m_lastMoveState.setCapturedPieceCode( this.getPieceCode(  moveRef.getTgtXFile( ), moveRef.getTgtYRank( ) ));
			
		
			boolean isEnPassantOpportunity = false;
			char srcPiece = this.pieceShift( moveRef );
			
			switch ( srcPiece )
			{
				case 'K':
					if ( this.m_isWhiteQueenSideCastleAvailable )
					{
						if ( moveRef.sameAs( WHITE_QUEEN_SIDE_CASTLE_KING_MOVE_REF ) )
						{
							this.pieceShift( WHITE_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF );
							this.m_lastMoveState.setCastleQueenSide( true );
						}
					}					
					if ( this.m_isWhiteKingSideCastleAvailable )
					{
						if ( moveRef.sameAs( WHITE_KING_SIDE_CASTLE_KING_MOVE_REF ) )
						{
							this.pieceShift( WHITE_KING_SIDE_CASTLE_ROOK_MOVE_REF );
							this.m_lastMoveState.setCastleKingSide( true );
						}
					}									
					this.m_isWhiteKingSideCastleAvailable=false;
					this.m_isWhiteQueenSideCastleAvailable=false;
				break;
				case 'k':
					if ( this.m_isBlackQueenSideCastleAvailable )
					{
						if ( moveRef.sameAs( BLACK_QUEEN_SIDE_CASTLE_KING_MOVE_REF ) )
						{
							this.pieceShift( BLACK_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF );
							this.m_lastMoveState.setCastleQueenSide( true );
						}
					}					
					if ( this.m_isBlackKingSideCastleAvailable )
					{
						if ( moveRef.sameAs( BLACK_KING_SIDE_CASTLE_KING_MOVE_REF ) )
						{
							this.pieceShift( BLACK_KING_SIDE_CASTLE_ROOK_MOVE_REF );
							this.m_lastMoveState.setCastleKingSide( true );
						}
					}
					this.m_isBlackKingSideCastleAvailable=false;
					this.m_isBlackQueenSideCastleAvailable=false;
				break;
				case 'R':
					if ( moveRef.getSrcXFile( )== 0)
					{
						this.m_isWhiteQueenSideCastleAvailable=false;
					}
					if ( moveRef.getSrcXFile( )== this.getNumXFiles()-1)
					{
						this.m_isWhiteKingSideCastleAvailable=false;					
					}
				break;
				case 'r':
					if ( moveRef.getSrcXFile( )== 0)
					{
						this.m_isBlackQueenSideCastleAvailable=false;
					}
					if ( moveRef.getSrcXFile( )== this.getNumXFiles()-1)
					{
						this.m_isBlackKingSideCastleAvailable=false;						
					}
				break;
				case 'P':				
					if ( moveRef.getTgtXFile( ) == this.m_enPassantFile && moveRef.getTgtYRank( ) == WHITE_ENPASSANT_CAPTURE_RANK )
					{
						this.removePiece( this.m_enPassantFile, BLACK_ENPASSANT_INITIATE_TARGET_RANK );  //blank source	
						this.m_lastMoveState.setCapturedPieceCode('p');
					}					
					isEnPassantOpportunity = ( moveRef.getSrcYRank( ) == WHITE_ENPASSANT_INITIATE_SOURCE_RANK && moveRef.getTgtYRank( ) ==  WHITE_ENPASSANT_INITIATE_TARGET_RANK );					
				break;
				case 'p':					
					if ( moveRef.getTgtXFile( ) == this.m_enPassantFile && moveRef.getTgtYRank( ) == BLACK_ENPASSANT_CAPTURE_RANK )
					{
						this.removePiece( this.m_enPassantFile, WHITE_ENPASSANT_INITIATE_TARGET_RANK );  //blank source
						this.m_lastMoveState.setCapturedPieceCode('P');
					}					
					isEnPassantOpportunity = ( moveRef.getSrcYRank( ) == BLACK_ENPASSANT_INITIATE_SOURCE_RANK && moveRef.getTgtYRank( ) ==  BLACK_ENPASSANT_INITIATE_TARGET_RANK );
				break;						
			}			
			
			BoardRef tgtBoardRef = moveRef.getTgtBoardRef( );
			if (tgtBoardRef.sameAs( BoardModel.WHITE_QUEEN_SIDE_ROOK_START_BOARD_REF ))
			{
				this.m_isWhiteQueenSideCastleAvailable=false;				
			}
			else if (tgtBoardRef.sameAs( BoardModel.WHITE_KING_SIDE_ROOK_START_BOARD_REF ))
			{
				this.m_isWhiteKingSideCastleAvailable=false;				
			}
			else if (tgtBoardRef.sameAs( BoardModel.BLACK_QUEEN_SIDE_ROOK_START_BOARD_REF ))
			{
				this.m_isBlackQueenSideCastleAvailable=false;				
			}
			else if (tgtBoardRef.sameAs( BoardModel.BLACK_KING_SIDE_ROOK_START_BOARD_REF ))
			{
				this.m_isBlackKingSideCastleAvailable=false;				
			}
		
			
			this.m_enPassantFile = (isEnPassantOpportunity?moveRef.getSrcXFile( ):NO_ENPASSANT_OPPORTUNITY_FILE);
			
		    if ( moveRef.isPromotedPieceCode( ))
		    {
		    	this.setPieceCodeAtBoardRef( moveRef.getTgtBoardRef( ), moveRef.getPromotedPieceCode( ));
		    	this.m_lastMoveState.setPromotionPieceCode( moveRef.getPromotedPieceCode( ) );
		    }
			
			this.setLastMove( moveRef );
			this.m_lastMoveState.setLastMove( moveRef );
						
			
			return true;
		}
	}
	
	public boolean isTileVacant( BoardRef boardRef  )
	{
		return ( this.isTileVacant( boardRef.getXFile( ), boardRef.getYRank( )));
	}
	
	public boolean isTileVacant( int xFile, int yRank  )
	{
		return ( this.getPieceCode( xFile, yRank ) == VACANT_TILE);
	}

	public char getPieceCode( BoardRef boardRef )
	{
		return this.getPieceCode(boardRef.getXFile( ), boardRef.getYRank( )); 	
	}	
	
	public char getPieceCode( int xFile, int yRank )
	{
        return this.m_boardArray[this.getBoardArrayIndex( xFile, yRank )];		
	}

	public void removePiece( int xFile, int yRank )
	{
		this.setPieceCode( xFile, yRank, VACANT_TILE );  //blank source
	}

	public void setPieceCode( int xFile, int yRank, char pieceCode )
	{
		this.m_boardArray[ this.getBoardArrayIndex( xFile, yRank )] = pieceCode;
	}

	protected int getBoardArrayIndex( int xFile, int yRank )
	{
		return (this.m_numYRanks * yRank) + xFile; 
	}
	
	protected BoardRef getBoardRefFromIndex( int index )
	{		
		int yRank = ((int)index/this.m_numYRanks+1)-1;
		int xFile = ((int)index%this.m_numYRanks+1)-1;
		return new BoardRef( xFile, yRank );  
	}
	
	public void setPieceCodeAtBoardRef( BoardRef boardRef, char pieceCode )
	{
		this.setPieceCode( boardRef.getXFile( ), boardRef.getYRank( ), pieceCode );
	}

	public boolean isBoardRefOnBoard( int xFile, int yRank )
	{
		return (xFile >=0 && xFile <  this.getNumXFiles( ) &&  yRank >=0 && yRank <  this.getNumYRanks( ));
	}

	public int getNumXFiles( )
	{
		return this.m_numXFiles;
	}

	public int getNumYRanks( )
	{
		return this.m_numYRanks;
	}

	public int countPieceCodeInstances( char pieceCode )
	{
		int pieceCodeCount = 0;
		for (char pc : this.m_boardArray) 
		{ 
		    if ( pc == pieceCode )
		    {
		    	pieceCodeCount++;	
		    }
		}
		return pieceCodeCount;					
	}
	
	public int countPieces( )
	{
		int pieceCodeCount = 0;
		for (char pc : this.m_boardArray) 
		{ 
		    if ( pc != VACANT_TILE )
		    {
		    	pieceCodeCount++;	
		    }
		}
		return pieceCodeCount;	
	}
	

	public int countPieceCodeColor( boolean isWhite )
	{
		int whitePieceCount = 0;
		int blackPieceCount = 0;
		
		for (char pc : this.m_boardArray) 
		{ 
			if ( pc != VACANT_TILE)
			{
				if ( this.isWhitePieceCode( pc ))
				{
					whitePieceCount++;
				}
				else
				{
					blackPieceCount++;
				}						
			}
		}
		return (isWhite?whitePieceCount:blackPieceCount);		
	}
	
	public Hashtable getPieceCodeStatusReport( )
	{
		Hashtable map = new Hashtable();
		for (char pc : this.m_boardArray) 
		{ 
			if ( pc != VACANT_TILE)
			{
				if ( map.containsKey( pc ))
				{
					int numPieces = (Integer) (map.get(pc)) + 1;
					map.remove(pc);
					map.put(pc, numPieces);
				
				}
				else
				{					
					map.put(pc, 1);
				}
			}
		}
		return map;
	}
	
	public boolean isPawnPromotionMove( MoveRef moveRef )
	{
		char srcPiece = this.getPieceCode( moveRef.getSrcXFile( ), moveRef.getSrcYRank( ) );
		if (srcPiece == 'P' && moveRef.getTgtYRank( ) == 0 )
		{
			return true;
		}
		if (srcPiece == 'p' && moveRef.getTgtYRank( ) == 7 )
		{
			return true;
		}
		return false;
	}
	
	public boolean isWhitePieceCode( char pcCode )
	{
		return (Character.toUpperCase(pcCode) == pcCode);
	}
	
	public boolean isOnBoard( BoardRef boardRef )
	{
		if ( boardRef.getXFile() < 0 ||  boardRef.getYRank( ) < 0 || boardRef.getXFile( ) >= this.getNumXFiles( ) ||  boardRef.getYRank( ) >= this.getNumYRanks( ))
		{
			return false;		
		}			
		return true;
	}

	public boolean isValidPieceSelect( BoardRef boardRef )
	{
		char pc = this.getPieceCode( boardRef );
		
		if ( this.isWhitePieceCode( pc ) && this.isWhiteToMove())
		{
			return true;
		}
		
		if ( ! this.isWhitePieceCode( pc ) && this.isBlackToMove( ))
		{
			return true;
		}		
		return false;	
	}
	
    public BoardModel clone(  )
    {   
    	
    	BoardModel newBoardModel;
		try
		{
			newBoardModel = (BoardModel) super.clone( );
			newBoardModel.setFrom( this );										
			return newBoardModel;
	    	
		} 
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}	
		return null;
    }

    
    public void setFrom( BoardModel boardModel)
    {
    	this.m_boardArray = boardModel.m_boardArray.clone( );
    	this.m_numXFiles = boardModel.m_numXFiles;
    	this.m_numYRanks = boardModel.m_numYRanks;
    		    		    	
    	this.m_gameStateId = boardModel.m_gameStateId;    	
    		    	
    	this.m_isWhiteKingSideCastleAvailable = boardModel.m_isWhiteKingSideCastleAvailable;
    	this.m_isWhiteQueenSideCastleAvailable = boardModel.m_isWhiteQueenSideCastleAvailable;
    	this.m_isBlackKingSideCastleAvailable = boardModel.m_isBlackKingSideCastleAvailable;
    	this.m_isBlackQueenSideCastleAvailable = boardModel.m_isBlackQueenSideCastleAvailable;
    	
    	this.m_halfMoveCount = boardModel.m_halfMoveCount;
    	this.m_isWhiteToMove = boardModel.m_isWhiteToMove;
    	      	   
    	if ( boardModel.m_lastMove != null )
    	{
    		this.m_lastMove = boardModel.m_lastMove.clone( );
    	}

    	if ( boardModel.m_lastMoveState != null )
    	{
    		this.m_lastMoveState =  boardModel.m_lastMoveState.clone( );
    	}
    	
    }
    
    public MoveRef getLastMove()
	{
		return this.m_lastMove;
	}

	public void setLastMove(MoveRef lastMove)
	{
		this.m_lastMove = lastMove;
	}

	public boolean isWhiteKingSideCastleAvailable()
	{
		return this.m_isWhiteKingSideCastleAvailable;
	}

	public void setWhiteKingSideCastleAvailable( boolean isWhiteKingSideCastleAvailable)
	{
		this.m_isWhiteKingSideCastleAvailable = isWhiteKingSideCastleAvailable;
	}

	public boolean isWhiteQueenSideCastleAvailable()
	{
		return this.m_isWhiteQueenSideCastleAvailable;
	}

	public void setWhiteQueenSideCastleAvailable(boolean isWhiteQueenSideCastleAvailable)
	{
		this.m_isWhiteQueenSideCastleAvailable = isWhiteQueenSideCastleAvailable;
	}

	public boolean isBlackKingSideCastleAvailable()
	{
		return this.m_isBlackKingSideCastleAvailable;
	}

	public void setBlackKingSideCastleAvailable( boolean isBlackKingSideCastleAvailable)
	{
		this.m_isBlackKingSideCastleAvailable = isBlackKingSideCastleAvailable;
	}

	public boolean isBlackQueenSideCastleAvailable()
	{
		return this.m_isBlackQueenSideCastleAvailable;
	}

	public void setBlackQueenSideCastleAvailable( boolean isBlackQueenSideCastleAvailable)
	{
		this.m_isBlackQueenSideCastleAvailable = isBlackQueenSideCastleAvailable;
	}

	public int getHalfMoveCount()
	{
		return this.m_halfMoveCount;
	}

	public void setHalfMoveCount(int halfMoveCount)
	{
		this.m_halfMoveCount = halfMoveCount;
	}

	public boolean isWhiteToMove()
	{
		return this.m_isWhiteToMove;
	}
	
	public boolean isBlackToMove()
	{
		return ! this.m_isWhiteToMove;
	}

	public void setWhiteToMove(boolean isWhiteToMove)
	{
		this.m_isWhiteToMove = isWhiteToMove;
	}
	
	public void ToggleColorToMove( )
	{
		this.setWhiteToMove( ! this.isWhiteToMove( ));
	}
		
	public int getEnPassantFile()
	{
		return this.m_enPassantFile;
	}

	public void setEnPassantFile(int enPassantFile)
	{
		this.m_enPassantFile = enPassantFile;
	}
	
	public boolean isEnPassantFile( )
	{
		return (this.m_enPassantFile != NO_ENPASSANT_OPPORTUNITY_FILE );
	}

	public BoardRef getKingBoardRef(boolean isWhiteToMove)
	{
		char kingPieceCode = (isWhiteToMove?'K':'k');
		
		for (int n=0;n<this.m_boardArray.length;n++) 
		{ 
			if ( kingPieceCode == this.m_boardArray[ n ])
			{
				return this.getBoardRefFromIndex( n );
			}
		}
		return null;
	}	

	public MoveState getLastMoveState()
	{
		return this.m_lastMoveState;
	}	
}