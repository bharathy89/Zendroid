package edu.codemonkey.zendroid;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Message;
import edu.codemonkey.zendroid.Utilities.Utilities;
import edu.codemonkey.zendroid.Utilities.Utilities.StandardErrorNotEmptyException;
import edu.codemonkey.zendroid.constants.Constants;
import edu.codemonkey.zendroid.errors.ZenError;

/**
 * The general workflow for this program is:<ol>
 * <li>User initiates scan from PIPSActivity, which creates a thread of type Scan.</li>
 * <li>The Scan thread sends messages to PIPSActivity.handler with errors or success, and includes output in Message.obj</li>
 * <li>TODO</li></ol>
 * <br /><b>Now implements Runnable</b> instead of extends Thread.
 */
class Scan implements Runnable {

	private final String binaryDirectory;
	private final boolean hasRoot;
	private final int help;
	private final String shell;
	private final String totalCommand;
	private final long threadID;

	/**
	 * Thread that handles the actual scanning. Yes, it's a little more
	 * intimidating than I had wanted when I first started this rewrite, but
	 * it's not nearly as bad as what I had before.
	 * 
	 * @param binaryDirectory
	 *            Directory the binaries will be stored. See Utilities class.
	 * @param hasRoot
	 *            Whether the user has access to root or not. See Utilities
	 *            class.
	 * @param help
	 *            Whether or not you want -h command-line help. 1 for help, 0 for normal output.
	 * @param context
	 *            Context the activity is running from (required to save results to database).
	 *            @param saveHistory
	 *            If true, history will be saved in the database.
	 */
	Scan(final String binaryDirectory, final boolean hasRoot,
			final int help, final Context context, final boolean saveHistory, String command, long threadID) {
		super();
		this.binaryDirectory = binaryDirectory;
		this.hasRoot = hasRoot;
		this.help = help;
		this.totalCommand = command;
		this.shell = hasRoot ? "su" : "sh";
		this.threadID = threadID;
	}

	public void run() {
		String line;
		Process process = null;
		DataOutputStream outputStream = null;
		BufferedReader inputStream, errorStream;
		inputStream = errorStream = null;
		StringBuilder errorOutput, normalOutput, commandCall;

		Message.obtain(MainActivity.handler, Constants.PROGRESS_DIALOG_START,
				threadID+" "+(help==0?"Scanning...":"Processing...")).sendToTarget();
		
		commandCall = new StringBuilder(binaryDirectory);
		commandCall.append(totalCommand);

		if (totalCommand.startsWith("nmap") && !totalCommand.contains("privileged")) {
			if (hasRoot)
			{
				commandCall.append(" --privileged ");
			}
			else
			{
				commandCall.append(" --unprivileged ");
			}
			String scanStorage = Utilities.getScanStorageDirectory();
			if( scanStorage != null) {
				commandCall.append(" "+"-oA "+scanStorage+"/"+threadID);
			}
		}else if(totalCommand.startsWith("ncat")) {
			String scanStorage = Utilities.getScanStorageDirectory();
			if( scanStorage != null) {
				commandCall.append(" "+"-o "+scanStorage+"/"+threadID+".ncat");
			}
		}else if(totalCommand.startsWith("nping") && !totalCommand.contains("privileged")) {
			String scanStorage = Utilities.getScanStorageDirectory();
			if (hasRoot)
			{
				commandCall.append(" --privileged ");
			}
			else
			{
				commandCall.append(" --unprivileged ");
			}
			if( scanStorage != null) {
				commandCall.append(" "+"-v | "+binaryDirectory+"tee "+scanStorage+"/"+threadID+".nping");
			}
		}

		if (help == 1 && !totalCommand.contains("-h")) {
			commandCall.append(" -h ");
		}

		try {
			process = Runtime.getRuntime().exec(shell);
			if (process == null) {
				throw new Utilities.NullProcessException(); // this condition is very unlikely
			} else {
				ZenError.log("Started shell process (" + shell + ").");
			}

			outputStream = new DataOutputStream(process.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			errorStream = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));

			outputStream.writeBytes(commandCall.toString() + "\n");
			ZenError.log(commandCall.toString());
			outputStream.flush();
			outputStream.writeBytes("exit\n");

			normalOutput = new StringBuilder();
			while ((line = inputStream.readLine()) != null) {
				normalOutput.append(line).append((char) '\n');
				ZenError.log(line);
			}
			
			if (normalOutput.length() > 0) {
				Message.obtain(MainActivity.handler, Constants.SCAN_COMPLETE,
						help, 0, threadID+" "+normalOutput.toString()).sendToTarget();
			} else if (normalOutput.length() <= 0) {
				ZenError.log("Ran nmap but received no input from stdout.");
			}
	
			// Catch errors last. Errors are reported to user as Dialog,
			// and they might need to know what's on both stdout and stderr,
			// show both.
			errorOutput = new StringBuilder();
			while ((line = errorStream.readLine()) != null) {
				errorOutput.append(line).append((char) '\n');
				ZenError.log(line);
			}
			if (errorOutput.length() > 2) { // newline above counts as 1
				throw new Utilities.StandardErrorNotEmptyException(
						errorOutput.toString());
			}
			

		} catch (IOException e) {
			Message.obtain(MainActivity.handler,
					Constants.SCAN_ERROR_IOEXCEPTION, threadID+" "+(String) e.toString())
					.sendToTarget();
		} catch (Utilities.NullProcessException e) {
			Message.obtain(MainActivity.handler,
					Constants.SCAN_ERROR_NULL_PROCESS, threadID+" ").sendToTarget();
		} catch (StandardErrorNotEmptyException e) {
			Message.obtain(MainActivity.handler,
					Constants.SCAN_ERROR_STANDARD_ERROR, threadID+" "+e.getMessage())
					.sendToTarget();
		}finally {
			Message.obtain(MainActivity.handler,
					Constants.PROGRESS_DIALOG_DISMISS, threadID+" ").sendToTarget();
			if (process != null) {
				process.destroy();
			}

			// I don't like nested try/catch statements, but it really needs to
			// be this way to make sure
			// everything gets closed correctly.
			try {
				if (outputStream != null) {
					outputStream.close();
				} else {
					ZenError.log("Cannot close null outputStream.");
				}
			} catch (IOException e) {
				ZenError.log("Unable to close outputStream: ");
				ZenError.log(e);
			}
			try {
				if (errorStream != null) {
					errorStream.close();
				} else {
					ZenError.log("Cannot close null errorStream.");
				}
			} catch (IOException e) {
				ZenError.log("Unable to close errorStream: ");
				ZenError.log(e);
			}
		}
	}
}
