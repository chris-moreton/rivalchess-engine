package com.netadapt.rivalchess.model;

public class AiLevelItem
{
	int m_depth;
	int m_thinkTimeMs;
	String m_description;
		
	public AiLevelItem(String description, int depth, int thinkTimeMs)
	{
		super();
		this.m_description = description;
		this.m_depth = depth;
		this.m_thinkTimeMs = thinkTimeMs;	
	}

	public boolean isDepthSearch( )
	{
		return ( this.m_depth != 0 );
	}
	
	public String getGeneratedDescription( )
	{
		if ( ! this.isDepthSearch( ))
		{
			return this.getDescription( ) +  " (" + this.getThinkTimeSecs( )   + " sec" + (this.getThinkTimeSecs( )!=1?"s":"") + " per move)";
		}
		return this.getDescription( );	
	}
	
	public int getDepth()
	{
		return this.m_depth;
	}
	public void setDepth(int depth)
	{
		this.m_depth = depth;
	}
	public int getThinkTimeMs()
	{
		return this.m_thinkTimeMs;
	}
	public void setThinkTimeMs(int thinkTime)
	{
		this.m_thinkTimeMs = thinkTime;
	}
	
	public int getThinkTimeSecs()
	{
		return (int)this.getThinkTimeMs( ) / 1000;
	}
	
	public String getDescription()
	{
		return this.m_description;
	}
	public void setDescription(String description)
	{
		this.m_description = description;
	}
	
	
}
