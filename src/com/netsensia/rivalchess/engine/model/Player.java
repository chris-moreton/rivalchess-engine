package com.netadapt.rivalchess.model;

public class Player
{
	protected String m_name;
	protected boolean m_isCpu = false;

	public boolean isCpu()
	{
		return this.m_isCpu;
	}

	public void setIsCpu(boolean isCpu)
	{
		this.m_isCpu = isCpu;
	}

	public String getName()
	{
		return this.m_name;
	}

	public void setName(String name)
	{
		this.m_name = name;
	}	
}