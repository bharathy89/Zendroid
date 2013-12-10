package edu.codemonkey.zendroid.constants;

import java.util.HashMap;

public class Constants {
	public final static int INSTALL_COMPLETE = 1;
	public final static int INSTALL_ERROR = -1;

	public final static int PROGRESS_DIALOG_CHANGE_TEXT = 100;
	public final static int PROGRESS_DIALOG_START = 101;
	public final static int PROGRESS_DIALOG_DISMISS = 102;
	
	public final static int POST_TOAST = 300;

	public final static int SCAN_ERROR_NULL_PROCESS = -200;
	public final static int SCAN_ERROR_STANDARD_ERROR = -201;
	public final static int SCAN_ERROR_IOEXCEPTION = -202;
	public final static int SCAN_COMPLETE = 203;
	public static HashMap<String, String> ARGUMENTSMAP = new HashMap<String, String>();
	static{
		ARGUMENTSMAP.put("Intense scan", "-T4 -A -v");
		ARGUMENTSMAP.put("Intense scan plus UDP","-sS -sU -T4 -A -v");
		ARGUMENTSMAP.put("Intense scan, all TCP ports", "-p 1-65535 -T4 -A -v");
		ARGUMENTSMAP.put("Intense scan, no ping", "-T4 -A -v -Pn");
		ARGUMENTSMAP.put("Ping scan", "-sn");
		ARGUMENTSMAP.put("Quick scan", "-T4 -F");
		ARGUMENTSMAP.put("Quick scan plus", "-sV -T4 -O -F --version-light");
		ARGUMENTSMAP.put("Quick traceroute", "-sn --traceroute");
		ARGUMENTSMAP.put("Regular scan", "");
		ARGUMENTSMAP.put("Slow comprehensive scan", "-sS -sU -T4 -A -v -PE -PP -PS80,443 " +
				"-PA3389 -PY -g 53 --script \"default or (discovery and safe) \"");
	}
	public static long threadID = 0;
	
}
