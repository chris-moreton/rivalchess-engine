package com.netadapt.rivalchess.util;

import com.netadapt.rivalchess.engine.core.RivalConstants;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppVersion
{
	private static int s_versionNumber = 0;
	private static String s_versionName = null;
	
	static public String getVersionString( Context context )
	{
		
		PackageInfo pinfo;		
		try
		{
			pinfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
			AppVersion.s_versionNumber = pinfo.versionCode;
			AppVersion.s_versionName = pinfo.versionName;
		} catch (NameNotFoundException e)
		{
			AppVersion.s_versionNumber = 0;
			AppVersion.s_versionName = "unknown";
		}
		
		return "Version " +  AppVersion.s_versionName + " Engine Build " + RivalConstants.VERSION + " Release " + AppVersion.s_versionNumber;
	}
	
	public void setVersionDetails( Context context  )
	{
		PackageInfo pinfo;		
		try
		{
			pinfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
			AppVersion.s_versionNumber = pinfo.versionCode;
			AppVersion.s_versionName = pinfo.versionName;
		} catch (NameNotFoundException e)
		{
			AppVersion.s_versionNumber = 0;
			AppVersion.s_versionName = "unknown";
		}
	}
}
