package edu.codemonkey.zendroid.installpackage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.codemonkey.zendroid.Utilities.Utilities;
import edu.codemonkey.zendroid.Utilities.Utilities.NullProcessException;
import edu.codemonkey.zendroid.Utilities.Utilities.StandardErrorNotEmptyException;
import edu.codemonkey.zendroid.errors.ZenError;

public class NSEScriptsInstaller implements Runnable{
	String filepath = "";
	String binaryDirectory="";

	public NSEScriptsInstaller(String Absolutefilepath, String binaryDirectory) {
		this.filepath = Absolutefilepath;
		this.binaryDirectory = binaryDirectory;
	}

	public synchronized void run() {
		ZenError.log(Thread.currentThread().getName());

		StringBuilder commandCall = new StringBuilder();
		commandCall.append("cp").append(' ').append(filepath).append(' ').append(binaryDirectory);
		StringBuilder normalOutput, errorOutput = new StringBuilder("");
		try {
			Process process = Runtime.getRuntime().exec("su");
			if (process == null) {
				throw new Utilities.NullProcessException(); // this condition is very unlikely
			} else {
				ZenError.log("Started shell process (" + "su" + ").");
			}

			DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			BufferedReader errorStream = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));


			outputStream.writeBytes(commandCall.toString() + "\n");
			ZenError.log(commandCall.toString());
			outputStream.flush();
			outputStream.writeBytes("exit\n");
			outputStream.flush();



			normalOutput = new StringBuilder();
			String line ="";
			while ((line = inputStream.readLine()) != null) {
				normalOutput.append(line).append((char) '\n');
				ZenError.log(line);
			}

			// Catch errors last. Errors are reported to user as Dialog,
			// and they might need to know what's on both stdout and stderr,
			// show both.
			errorOutput = new StringBuilder();
			while ((line = errorStream.readLine()) != null) {
				errorOutput.append(line).append((char) '\n');
				ZenError.log("Couldnt install nse scripts");
			}
			if (errorOutput.length() > 2) { // newline above counts as 1
				throw new Utilities.StandardErrorNotEmptyException(
						errorOutput.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (StandardErrorNotEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (NullProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
