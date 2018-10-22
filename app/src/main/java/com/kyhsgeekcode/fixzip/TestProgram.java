package com.kyhsgeekcode.fixzip;

import android.util.*;

public class TestProgram
{
	public static void Run(MainActivity a)
	{
		String s = "";
		a.print("hello");
		try
		{
			 s=a.readLine();
		}
		catch (InterruptedException e)
		{
			a.print(Log.getStackTraceString(e));
		}
		a.print("you said");
		a.print(s);
	}
}
