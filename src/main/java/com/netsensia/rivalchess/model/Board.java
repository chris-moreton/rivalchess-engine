package com.netsensia.rivalchess.model;

public class Board implements Cloneable {

	public final static int DEFAULT_BOARD_NUM_FILES = 8;
	public final static int DEFAULT_BOARD_NUM_RANKS = 8;

    public static final char VACANT_TILE = '_';

    protected char[] m_boardArray;
    protected int m_numXFiles;
    protected int m_numYRanks;
    protected Move m_lastMove;
    protected int m_enPassantFile = 0;

    protected static Square WHITE_QUEEN_SIDE_ROOK_START_BOARD_REF = new Square(0, 7);
    protected static Square WHITE_KING_SIDE_ROOK_START_BOARD_REF = new Square(7, 7);

    protected static Square BLACK_QUEEN_SIDE_ROOK_START_BOARD_REF = new Square(0, 0);
    protected static Square BLACK_KING_SIDE_ROOK_START_BOARD_REF = new Square(7, 0);

    protected static Move WHITE_QUEEN_SIDE_CASTLE_KING_MOVE_REF = new Move(4, 7, 2, 7);
    protected static Move WHITE_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF = new Move(0, 7, 3, 7);

    protected static Move WHITE_KING_SIDE_CASTLE_KING_MOVE_REF = new Move(4, 7, 6, 7);
    protected static Move WHITE_KING_SIDE_CASTLE_ROOK_MOVE_REF = new Move(7, 7, 5, 7);

    protected static Move BLACK_QUEEN_SIDE_CASTLE_KING_MOVE_REF = new Move(4, 0, 2, 0);
    protected static Move BLACK_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF = new Move(0, 0, 3, 0);

    protected static Move BLACK_KING_SIDE_CASTLE_KING_MOVE_REF = new Move(4, 0, 6, 0);
    protected static Move BLACK_KING_SIDE_CASTLE_ROOK_MOVE_REF = new Move(7, 0, 5, 0);

    protected static int WHITE_ENPASSANT_CAPTURE_RANK = 2;
    protected static int WHITE_ENPASSANT_INITIATE_SOURCE_RANK = 6;
    protected static int WHITE_ENPASSANT_INITIATE_TARGET_RANK = 4;

    protected static int BLACK_ENPASSANT_CAPTURE_RANK = 5;
    protected static int BLACK_ENPASSANT_INITIATE_SOURCE_RANK = 1;
    protected static int BLACK_ENPASSANT_INITIATE_TARGET_RANK = 3;

    protected static int NO_ENPASSANT_OPPORTUNITY_FILE = -1;

    protected boolean m_isWhiteKingSideCastleAvailable = true;
    protected boolean m_isWhiteQueenSideCastleAvailable = true;
    protected boolean m_isBlackKingSideCastleAvailable = true;
    protected boolean m_isBlackQueenSideCastleAvailable = true;

    protected int m_halfMoveCount = 0;
    protected boolean m_isWhiteToMove = true;

    protected int m_gameStateId = 0;

    protected MoveState m_lastMoveState = null;

    public Board() {
        this(DEFAULT_BOARD_NUM_FILES, DEFAULT_BOARD_NUM_RANKS);
    }

    public Board(int numXFiles, int numYRanks) {
        this.m_numXFiles = numXFiles;
        this.m_numYRanks = numYRanks;
        this.m_boardArray = new char[this.m_numYRanks * this.m_numXFiles];
    }

    private char pieceShift(Move move) {
        char srcPiece = this.getPieceCode(move.getSrcXFile(), move.getSrcYRank());
        this.setPieceCode(move.getTgtXFile(), move.getTgtYRank(), srcPiece);
        this.removePiece(move.getSrcXFile(), move.getSrcYRank());
        return srcPiece;
    }

    public boolean movePiece(Move move)
    {
        this.m_lastMoveState = new MoveState();

        if ((move.getSrcXFile() == move.getTgtXFile()) && (move.getSrcYRank() == move.getTgtYRank())) {
            return false;
        } else {
            this.m_lastMoveState.setPieceCode(this.getPieceCode(move.getSrcXFile(), move.getSrcYRank()));
            this.m_lastMoveState.setCapturedPieceCode(this.getPieceCode(move.getTgtXFile(), move.getTgtYRank()));


            boolean isEnPassantOpportunity = false;
            char srcPiece = this.pieceShift(move);

            switch (srcPiece) {
                case 'K':
                    if (this.m_isWhiteQueenSideCastleAvailable) {
                        if (move.equals(WHITE_QUEEN_SIDE_CASTLE_KING_MOVE_REF)) {
                            this.pieceShift(WHITE_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF);
                            this.m_lastMoveState.setCastleQueenSide(true);
                        }
                    }
                    if (this.m_isWhiteKingSideCastleAvailable) {
                        if (move.equals(WHITE_KING_SIDE_CASTLE_KING_MOVE_REF)) {
                            this.pieceShift(WHITE_KING_SIDE_CASTLE_ROOK_MOVE_REF);
                            this.m_lastMoveState.setCastleKingSide(true);
                        }
                    }
                    this.m_isWhiteKingSideCastleAvailable = false;
                    this.m_isWhiteQueenSideCastleAvailable = false;
                    break;
                case 'k':
                    if (this.m_isBlackQueenSideCastleAvailable) {
                        if (move.equals(BLACK_QUEEN_SIDE_CASTLE_KING_MOVE_REF)) {
                            this.pieceShift(BLACK_QUEEN_SIDE_CASTLE_ROOK_MOVE_REF);
                            this.m_lastMoveState.setCastleQueenSide(true);
                        }
                    }
                    if (this.m_isBlackKingSideCastleAvailable) {
                        if (move.equals(BLACK_KING_SIDE_CASTLE_KING_MOVE_REF)) {
                            this.pieceShift(BLACK_KING_SIDE_CASTLE_ROOK_MOVE_REF);
                            this.m_lastMoveState.setCastleKingSide(true);
                        }
                    }
                    this.m_isBlackKingSideCastleAvailable = false;
                    this.m_isBlackQueenSideCastleAvailable = false;
                    break;
                case 'R':
                    if (move.getSrcXFile() == 0) {
                        this.m_isWhiteQueenSideCastleAvailable = false;
                    }
                    if (move.getSrcXFile() == this.getNumXFiles() - 1) {
                        this.m_isWhiteKingSideCastleAvailable = false;
                    }
                    break;
                case 'r':
                    if (move.getSrcXFile() == 0) {
                        this.m_isBlackQueenSideCastleAvailable = false;
                    }
                    if (move.getSrcXFile() == this.getNumXFiles() - 1) {
                        this.m_isBlackKingSideCastleAvailable = false;
                    }
                    break;
                case 'P':
                    if (move.getTgtXFile() == this.m_enPassantFile && move.getTgtYRank() == WHITE_ENPASSANT_CAPTURE_RANK) {
                        this.removePiece(this.m_enPassantFile, BLACK_ENPASSANT_INITIATE_TARGET_RANK);  //blank source
                        this.m_lastMoveState.setCapturedPieceCode('p');
                    }
                    isEnPassantOpportunity = (move.getSrcYRank() == WHITE_ENPASSANT_INITIATE_SOURCE_RANK && move.getTgtYRank() == WHITE_ENPASSANT_INITIATE_TARGET_RANK);
                    break;
                case 'p':
                    if (move.getTgtXFile() == this.m_enPassantFile && move.getTgtYRank() == BLACK_ENPASSANT_CAPTURE_RANK) {
                        this.removePiece(this.m_enPassantFile, WHITE_ENPASSANT_INITIATE_TARGET_RANK);  //blank source
                        this.m_lastMoveState.setCapturedPieceCode('P');
                    }
                    isEnPassantOpportunity = (move.getSrcYRank() == BLACK_ENPASSANT_INITIATE_SOURCE_RANK && move.getTgtYRank() == BLACK_ENPASSANT_INITIATE_TARGET_RANK);
                    break;
            }

            Square tgtBoardRef = move.getTgtBoardRef();
            if (tgtBoardRef.equals(Board.WHITE_QUEEN_SIDE_ROOK_START_BOARD_REF)) {
                this.m_isWhiteQueenSideCastleAvailable = false;
            } else if (tgtBoardRef.equals(Board.WHITE_KING_SIDE_ROOK_START_BOARD_REF)) {
                this.m_isWhiteKingSideCastleAvailable = false;
            } else if (tgtBoardRef.equals(Board.BLACK_QUEEN_SIDE_ROOK_START_BOARD_REF)) {
                this.m_isBlackQueenSideCastleAvailable = false;
            } else if (tgtBoardRef.equals(Board.BLACK_KING_SIDE_ROOK_START_BOARD_REF)) {
                this.m_isBlackKingSideCastleAvailable = false;
            }

            this.m_enPassantFile = (isEnPassantOpportunity ? move.getSrcXFile() : NO_ENPASSANT_OPPORTUNITY_FILE);

            if (move.isPromotedPieceCode()) {
                this.setPieceCodeAtBoardRef(move.getTgtBoardRef(), move.getPromotedPieceCode());
                this.m_lastMoveState.setPromotionPieceCode(move.getPromotedPieceCode());
            }

            this.setLastMove(move);
            this.m_lastMoveState.setLastMove(move);


            return true;
        }
    }

    public char getPieceCode(Square boardRef) {
        return this.getPieceCode(boardRef.getXFile(), boardRef.getYRank());
    }

    public char getPieceCode(int xFile, int yRank) {
        return this.m_boardArray[this.getBoardArrayIndex(xFile, yRank)];
    }

    public void removePiece(int xFile, int yRank) {
        this.setPieceCode(xFile, yRank, VACANT_TILE);
    }

    public void setPieceCode(int xFile, int yRank, char pieceCode) {
        this.m_boardArray[this.getBoardArrayIndex(xFile, yRank)] = pieceCode;
    }

    protected int getBoardArrayIndex(int xFile, int yRank) {
        return (this.m_numYRanks * yRank) + xFile;
    }

    public void setPieceCodeAtBoardRef(Square boardRef, char pieceCode) {
        this.setPieceCode(boardRef.getXFile(), boardRef.getYRank(), pieceCode);
    }

    public int getNumXFiles() {
        return this.m_numXFiles;
    }

    public int getNumYRanks() {
        return this.m_numYRanks;
    }

    public int countPieceCodeInstances(char pieceCode) {
        int pieceCodeCount = 0;
        for (char pc : this.m_boardArray) {
            if (pc == pieceCode) {
                pieceCodeCount++;
            }
        }
        return pieceCodeCount;
    }

    public int countPieces() {
        int pieceCodeCount = 0;
        for (char pc : this.m_boardArray) {
            if (pc != VACANT_TILE) {
                pieceCodeCount++;
            }
        }
        return pieceCodeCount;
    }


    public int countPieceCodeColor(boolean isWhite) {
        int whitePieceCount = 0;
        int blackPieceCount = 0;

        for (char pc : this.m_boardArray) {
            if (pc != VACANT_TILE) {
                if (this.isWhitePieceCode(pc)) {
                    whitePieceCount++;
                } else {
                    blackPieceCount++;
                }
            }
        }
        return (isWhite ? whitePieceCount : blackPieceCount);
    }

    public boolean isWhitePieceCode(char pcCode) {
        return (Character.toUpperCase(pcCode) == pcCode);
    }

    public Board clone() {

        Board newBoard;
        try {
            newBoard = (Board) super.clone();
            newBoard.setFrom(this);
            return newBoard;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFrom(Board board) {
        this.m_boardArray = board.m_boardArray.clone();
        this.m_numXFiles = board.m_numXFiles;
        this.m_numYRanks = board.m_numYRanks;

        this.m_gameStateId = board.m_gameStateId;

        this.m_isWhiteKingSideCastleAvailable = board.m_isWhiteKingSideCastleAvailable;
        this.m_isWhiteQueenSideCastleAvailable = board.m_isWhiteQueenSideCastleAvailable;
        this.m_isBlackKingSideCastleAvailable = board.m_isBlackKingSideCastleAvailable;
        this.m_isBlackQueenSideCastleAvailable = board.m_isBlackQueenSideCastleAvailable;

        this.m_halfMoveCount = board.m_halfMoveCount;
        this.m_isWhiteToMove = board.m_isWhiteToMove;

        if (board.m_lastMove != null) {
            this.m_lastMove = new Move(board.m_lastMove);
        }

        if (board.m_lastMoveState != null) {
            this.m_lastMoveState = new MoveState(board.m_lastMoveState);
        }
    }

    public void setLastMove(Move lastMove) {
        this.m_lastMove = lastMove;
    }

    public boolean isWhiteKingSideCastleAvailable() {
        return this.m_isWhiteKingSideCastleAvailable;
    }

    public void setWhiteKingSideCastleAvailable(boolean isWhiteKingSideCastleAvailable) {
        this.m_isWhiteKingSideCastleAvailable = isWhiteKingSideCastleAvailable;
    }

    public boolean isWhiteQueenSideCastleAvailable() {
        return this.m_isWhiteQueenSideCastleAvailable;
    }

    public void setWhiteQueenSideCastleAvailable(boolean isWhiteQueenSideCastleAvailable) {
        this.m_isWhiteQueenSideCastleAvailable = isWhiteQueenSideCastleAvailable;
    }

    public boolean isBlackKingSideCastleAvailable() {
        return this.m_isBlackKingSideCastleAvailable;
    }

    public void setBlackKingSideCastleAvailable(boolean isBlackKingSideCastleAvailable) {
        this.m_isBlackKingSideCastleAvailable = isBlackKingSideCastleAvailable;
    }

    public boolean isBlackQueenSideCastleAvailable() {
        return this.m_isBlackQueenSideCastleAvailable;
    }

    public void setBlackQueenSideCastleAvailable(boolean isBlackQueenSideCastleAvailable) {
        this.m_isBlackQueenSideCastleAvailable = isBlackQueenSideCastleAvailable;
    }

    public int getHalfMoveCount() {
        return this.m_halfMoveCount;
    }

    public boolean isWhiteToMove() {
        return this.m_isWhiteToMove;
    }

    public boolean isBlackToMove() {
        return !this.m_isWhiteToMove;
    }

    public void setWhiteToMove(boolean isWhiteToMove) {
        this.m_isWhiteToMove = isWhiteToMove;
    }

    public void ToggleColorToMove() {
        this.setWhiteToMove(!this.isWhiteToMove());
    }

    public int getEnPassantFile() {
        return this.m_enPassantFile;
    }

    public void setEnPassantFile(int enPassantFile) {
        this.m_enPassantFile = enPassantFile;
    }
}