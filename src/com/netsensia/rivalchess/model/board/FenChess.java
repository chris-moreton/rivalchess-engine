package com.netsensia.rivalchess.model.board;



//!!! EXCEPTIONS !!!!
//X-FEN : // http://en.wikipedia.org/wiki/X-FEN

public class FenChess extends Fen
{
	protected int m_fullMoveCount 	= 0;
	protected int m_halfMoveCount	= 0;
	protected String m_enPassantValue = "";
	protected char m_toMoveValue;
	
	public FenChess(BoardModel board)
	{
		super(board);
	}

	@Override
	public boolean setFromStr( String fenStr )
	{
		this.m_fenStr = fenStr;

		boolean isError = false;

		int fenIndex 			= 0;
		int boardArrayIndex 	= 0;

		if ( fenStr.trim()  == "" )
		{
			isError = true;
		}

		char fenToken;

		if ( ! isError )  // Process main board
		{
			char zeroChar = '0';
			char nineChar = '9';
			
			for(int i = 0; i<fenStr.length(); i++) 			
			{
				fenToken = fenStr.charAt( fenIndex++ );
				
			
				// If this is an integer, pad board with spaces
				if ( fenToken >= zeroChar &&  fenToken <= nineChar )
				{
					//Trap double digit numbers - hacky.
					/*
					 * TODO - this is for another day - large boards over 10*10					 * 					* 
					 * fenNextToken = fenStr[ fenIndex ];
					if ( ord(fenNextToken) >=  ord('0') &&  ord(fenNextToken) <= ord('9') )
					{
						fenToken = fenToken . fenNextToken;
						fenIndex++;

						//echo(':' . fenToken .':');
					}*/

										
					int numPadded =Character.digit(fenToken, 10);
					
					for (int n=1; n <= numPadded; n++ )
					{
						//this.m_boardArray[ boardArrayIndex % 8 ][ boardArrayIndex / 8 ] = '_';
						int targetXFile = (int)Math.floor(boardArrayIndex % this.m_board.getNumXFiles( ));
						int targetYRank = (int)Math.floor(boardArrayIndex / this.m_board.getNumXFiles( ));

						this.m_board.setPieceCode( targetXFile, targetYRank, BoardModel.VACANT_TILE );
						boardArrayIndex++;
					}
				}
				else // set the right piece unless divider
				{
					if ( fenToken == '/')
					{
						// ignore
						//echo('<br>');
					}
					else
					{
						int targetXFile = (int)Math.floor(boardArrayIndex % this.m_board.getNumXFiles( ));
						int targetYRank = (int)Math.floor(boardArrayIndex / this.m_board.getNumXFiles( ));

						//echo( '(' . targetXFile . ' : ' . targetYRank . ')[' . boardArrayIndex . '/' .  this.m_board.GetNumYRanks( ) . ']  <b>#</b>      ');

						//this.m_boardArray[ boardArrayIndex % 8 ][ boardArrayIndex / 8 ] = fenToken;
						this.m_board.setPieceCode( targetXFile,targetYRank, fenToken  );
						boardArrayIndex++;
					}
				}

				// No space should exist until finished.
				if (fenToken == ' ')
				{
					isError = true;
				}

				if ( boardArrayIndex == (this.m_board.getNumXFiles( )*this.m_board.getNumYRanks( )) )
				{
					// We have the whole board
					break;
				}

				if ( boardArrayIndex > (this.m_board.getNumXFiles( )*this.m_board.getNumYRanks( )) )
				{
					isError = true;
				}
			}
		}

		//
		// TODO - other fen parts.
		//
		
		if ( ! isError )   //Parse additional FEN info only if no error so far.
		{
			fenIndex++; // Scoot past space

			fenToken = fenStr.charAt( fenIndex++ );
			if ( fenToken == 'w' || fenToken == 'b' )
			{
				this.m_board.setWhiteToMove( fenToken == 'w' );
				this.m_toMoveValue = fenToken;
			}
			else
			{
				isError = true;
			}

			if ( ! isError )
			{
				fenIndex ++;

				this.m_board.setWhiteQueenSideCastleAvailable(false);  
				this.m_board.setWhiteKingSideCastleAvailable(false);  
				this.m_board.setBlackQueenSideCastleAvailable(false);  
				this.m_board.setBlackKingSideCastleAvailable(false); 
			
				while (fenStr.charAt(fenIndex) != ' ')
				{
					switch (fenStr.charAt(fenIndex))
					{
						case 'Q' : this.m_board.setWhiteQueenSideCastleAvailable(true); break;  
						case 'K' : this.m_board.setWhiteKingSideCastleAvailable(true); break;  
						case 'q' : this.m_board.setBlackQueenSideCastleAvailable(true); break;  
						case 'k' : this.m_board.setBlackKingSideCastleAvailable(true); break;  
					}
					// castle privileges
					fenIndex ++;
				}

				fenIndex ++;
				
				if (fenStr.charAt(fenIndex) != '-')
				{
					// en passant;
					this.m_board.setEnPassantFile(fenStr.charAt(fenIndex) - 97);
					fenIndex ++;
				}
				else
				{
					this.m_board.setEnPassantFile(-1);
				}
				
			}
		}

		if  ( isError )
		{
			this.m_isValidFen = false;
		}
		else
		{
			this.m_isValidFen = true;
		}
		return isError;
	}
	
	public String getEnPassantValue( )
	{
		return this.m_enPassantValue;
	}

	public int getHalfMoveCount( )
	{
		return this.m_halfMoveCount;
	}

	public int getFullMoveCount( )
	{
		return this.m_fullMoveCount;
	}

	public boolean isWhiteToMove( )
	{
		return ( this.m_toMoveValue == 'w' );
	}


	@Override
	public String getFen()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	public BoardRef GetEnPassantBoardRef( )
    {
        //echo('<h1>[' . this.m_enPassantValue . ']</h1>');
        //echo('<h4>' . this.m_fenStr . '</h4>');
        
		if ( this.m_enPassantValue != '-' && strlen( this.m_enPassantValue ) == 2 )
        {
           //echo('<b>There is a enpassant value in FEN</b>');
           alphaValue = strtolower( this.m_enPassantValue[0] );
           numericValue = this.m_enPassantValue[1];
           return new BoardRef( (ord( alphaValue ) - 97), (this.m_board.GetNumYRanks( )-numericValue)  );
        }
        return null;
    }
    */

    /*
     * function GetArrayPosFromAlgebraic( algebraicValue, &xVal, &yVal )
    {
        alphaValue     = strtolower( algebraicValue[0] );
        numericValue   = algebraicValue[1];

        yVal = 8-numericValue;
        xVal = ord( alphaValue) - 97;
    }
    */

	//
	// protected method
	//
	/*function GetFenPart(  &fenStr, &index )
	{
		fenPart = '';
		while( index < strlen(fenStr) && fenStr[ index ] != NULL && fenStr[ index ] != ' ')
		{
			fenPart .= fenStr[ index ];
			index++;
		}
        echo('~' . fenPart . '~');

		return trim( fenPart );
	}
	*/

	/*function GetBoardValue( xRank, yFile )
	{
	return this.m_boardArray[xRank][yFile];
	}
	*/

	/*function GetFen( )
	{
		completFenValueString = this.GetFenStrFromBoard(  this.m_board );
		if ( this.m_board.IsBoard2( ))
		{
			completFenValueString .= ',' . this.GetFenStrFromBoard( this.m_board.GetBoard2( ));
		}
		return completFenValueString;
	}*/

	/*function GetFenStrFromBoard( CBoardChess board  )
	{
		spaceCounter = 0;

		fenValueString = '';

		for (y = 0; y < board.GetNumYRanks( ); y++)
		{
			spaceCounter = 0;

			for (x = 0; x < board.GetNumXFiles( ); x++)
			{
				//currentTile = this.playBoardArray[x][y];
				currentTile = board.GetPieceCode(x,y);
				if (currentTile != '_')
				{
					if (spaceCounter != 0)
					{
						fenValueString .= spaceCounter;
					}
					spaceCounter = 0;
					fenValueString .= currentTile;
				}
				else
				{
					spaceCounter++;
				}
			}

			if (spaceCounter != 0)
			{
				fenValueString .= spaceCounter;
			}
			if (y != 7)
			{
				fenValueString .= '/';
			}
		}

		fenValueString .= ' ';
		// to move
		//fenValueString .= this.toMoveCol;
		fenValueString .='w';

		fenValueString .= ' ';


		// Get castling state for persistance
		//fenValueString .= this.fenCastleValue;
		fenValueString .= 'KQkq';
		fenValueString .= ' ';


		//Get 'en passant' target square
        enPassantBoardRef = board.GetEnPassantBoardRef( );
		if( enPassantBoardRef )
        {
            fenValueString .= enPassantBoardRef.GetAlgebraicXFile( board.GetNumXFiles( ) );
            fenValueString .= enPassantBoardRef.GetAlgebraicYRank( board.GetNumYRanks( ) );
        }
        else
        {
            fenValueString .= '-';   //eg, e4
        }

		fenValueString .= ' ';

		// Halfmove clock (since pawn move or capture)
		//fenValueString .= this.halfMoveCount;
		fenValueString .= 1;

		fenValueString .= ' ';
		// fullMoveCount clock : value '1' for the first move of a game for both White and Black.
		//fenValueString .= this.fullMoveCount;
		fenValueString .= 22;

		//echo('<br><b>From array to string for persisting  (getfenValueString)<Br>' . fenValueString . '</b></br>');

		return fenValueString;
	}*/
}
