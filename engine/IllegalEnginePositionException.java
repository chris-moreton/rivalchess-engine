package com.netadapt.rivalchess.engine;

public class IllegalEnginePositionException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String m_error = "";
	
	public IllegalEnginePositionException()
	{
		super();
		m_error = "Unknown Illegal Position Exception";
	}

	public IllegalEnginePositionException(String error)
	{
		super();
		this.m_error = error;
	}
	
	public String getError()
	{
		return this.m_error;
	}
	
	public String toString()
	{
		return getError();
	}
}
