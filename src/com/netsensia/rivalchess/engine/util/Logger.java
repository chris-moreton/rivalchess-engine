package com.netadapt.rivalchess.util;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.netadapt.rivalchess.engine.core.RivalConstants;

public class Logger {
	
	public static void log(PrintWriter out, String s, String type)
	{
		Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM-dd HH:mm:ss:S");
		String formattedDate = formatter.format(todaysDate);
		
		if (RivalConstants.UCI_LOG)
		{
			out.println(formattedDate + " [" + type + "] " + s);
			out.flush();
		}
	}
}
