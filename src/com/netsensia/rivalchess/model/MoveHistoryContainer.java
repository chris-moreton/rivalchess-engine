package com.netsensia.rivalchess.model;

import java.util.Vector;

public class MoveHistoryContainer extends Vector implements Cloneable 
{
	synchronized public MoveHistoryItem[] GetArray( )
	{		
		MoveHistoryItem[] moveHistoryArray = new MoveHistoryItem[ this.size( )];						
		for (int n = 0; n < this.size( );n++)
		{
			moveHistoryArray[n] = (MoveHistoryItem)this.get( n );
		}				
		return moveHistoryArray;
	}
	
	synchronized public void addItem( MoveHistoryItem moveHistoryItem )
	{
		this.add( moveHistoryItem );
	}
	
	synchronized public boolean popMoves( int numMoves )
	{	
		if ( this.size( ) < 1 )
		{
			return false;
		}
		
		int popIndexStart = this.size( )- numMoves;
		int popIndexEnd = this.size( );
				
		if ( popIndexStart < 0 )
		{
			popIndexStart = 0;
		}
				
		if ( numMoves == 1)
		{				
			this.remove( popIndexStart );		
		}
		else
		{		
			this.removeRange( popIndexStart, popIndexEnd);
		}		 				
		return true;
	}
	
	synchronized public int getMoveCounter( )
	{
		return this.size( );	
	}
	
	synchronized public void reset( )
	{
		this.clear( );
	}
	
	synchronized public MoveHistoryContainer clone(  )
    {       	
    	MoveHistoryContainer newMoveHistoryContainer;
	
		newMoveHistoryContainer = (MoveHistoryContainer) super.clone( );		
						
		return newMoveHistoryContainer;    		
    }	
    
	synchronized public MoveHistoryItem GetTopMoveHistoryItem( )
    {
    	if ( this.size( ) >= 1 )
    	{
    		return (MoveHistoryItem)this.get( this.size( )-1);
    	}
    	return null;
    }   
    
	synchronized public MoveHistoryItem GetMoveHistoryItem( int index )
    {
    	return (MoveHistoryItem)this.get( index );    
    } 
}