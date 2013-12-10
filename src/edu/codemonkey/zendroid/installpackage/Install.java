package edu.codemonkey.zendroid.installpackage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import edu.codemonkey.zendroid.MainActivity;
import edu.codemonkey.zendroid.R;
import edu.codemonkey.zendroid.Utilities.Utilities;
import edu.codemonkey.zendroid.Utilities.Utilities.NullProcessException;
import edu.codemonkey.zendroid.Utilities.Utilities.StandardErrorNotEmptyException;
import edu.codemonkey.zendroid.constants.Constants;
import edu.codemonkey.zendroid.errors.ZenError;

/**
 * Now that I know about this cool thing called a
 * DigestOutputStream, this class is pretty solid, having the
 * potential to catch errors and all.
 * @author John
 *
 */
public class Install implements Runnable {

	private static final int BUFFER_SIZE = 8192;
	private long threadID=0;
	
	private class InstallerBinary {
		public transient String filename;
		public transient int files[];
		public transient boolean executable;
		

		public InstallerBinary(final String filename, final int files[], 
				final boolean executable) {
			this.filename = filename;
			this.files = files.clone();
			this.executable = executable;
		}
	}

	private final transient InstallerBinary installerBinaries[] = {
			new InstallerBinary("nse_main.lua", new int[] { R.raw.nse_main }, false),
			new InstallerBinary("tee", new int[] { R.raw.tee }, true),
			new InstallerBinary("tar", new int[] { R.raw.tar }, true),
			new InstallerBinary("nmap", new int[] { R.raw.nmap }, true),
			new InstallerBinary("ncat", new int[] { R.raw.ncat }, true),
			new InstallerBinary("nping", new int[] { R.raw.nping }, true),
			new InstallerBinary("nmap-os-db", new int[] { R.raw.nmap_os_db }, false),
			new InstallerBinary("nmap-payloads",
					new int[] { R.raw.nmap_payloads }, false),
			new InstallerBinary("nmap-protocols",
					new int[] { R.raw.nmap_protocols }, false),
			new InstallerBinary("nmap-rpc", new int[] { R.raw.nmap_rpc }, false),
			new InstallerBinary("nmap-service-probes",
					new int[] { R.raw.nmap_service_probes }, false),
			new InstallerBinary("nmap-services",
					new int[] { R.raw.nmap_services }, false),
			new InstallerBinary("nmap-mac-prefixes",
					new int[] { R.raw.nmap_mac_prefixes }, false),			
			new InstallerBinary("nse.tar", new int[] { R.raw.nse }, false)
			};

	private final transient String binaryDirectory;
	private final transient Resources appResources;
	private final transient boolean hasRoot;

	/**
	 * 
	 * @param context Context of the activity launching this installer.
	 * @param binaryDirectory Location to save binaries.
	 * @param hasRoot Does user have root access or not.
	 */
	public Install(final Context context, final String binaryDirectory,
			final boolean hasRoot, long threadID) {
		super();
		this.appResources = context.getResources();
		this.binaryDirectory = binaryDirectory;
		this.hasRoot = hasRoot;
		this.threadID = threadID;
	}

	private void deleteExistingFile(final File myFile) {
		if (myFile.exists()) {
			ZenError.log(myFile.getAbsolutePath() + " exists. Deleting...");
			if (myFile.delete()) {
				ZenError.log("...deleted.");
			} else {
				ZenError.log("...unable to delete.");
			}
		}
	}

	private MessageDigest writeNewFile(final File myFile, final int fileResources[]) {
		byte[] buf = new byte[BUFFER_SIZE];
		int size=0;
		DigestOutputStream out;
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			out = new DigestOutputStream(new FileOutputStream(myFile), md5);
			for (int resource : fileResources) {
				final InputStream inputStream = appResources
						.openRawResource(resource);
				while ((size = inputStream.read(buf)) > 0) {
					out.write(buf,0,size);
					buf = new byte[BUFFER_SIZE];
				}
				inputStream.close();
			}
			out.close();
		} catch (FileNotFoundException e) {
			ZenError.log(e);
		} catch (IOException e) {
			ZenError.log(e);
		} catch (NoSuchAlgorithmException e) {
			ZenError.log(e);
		}
		
		return md5;
	}

	private void setExecutable(final File myFile) {
		final String shell = hasRoot ? "su" : "sh";
		try {
			final Process process = Runtime.getRuntime().exec(shell);
			final DataOutputStream outputStream = new DataOutputStream(
					process.getOutputStream());
			final BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(process.getInputStream()),
					BUFFER_SIZE);
			final BufferedReader errorStream = new BufferedReader(
					new InputStreamReader(process.getErrorStream()),
					BUFFER_SIZE);

			outputStream.writeBytes("cd " + this.binaryDirectory + "\n");

			if (hasRoot) {
				outputStream.writeBytes("chown root.root * \n");
				ZenError.log("chown root.root *");
			}

			outputStream.writeBytes("chmod 555 " + myFile.getAbsolutePath()
					+ " \n");
			ZenError.log("chmod 555 " + myFile.getAbsolutePath());

			outputStream
					.writeBytes("chmod 777 " + this.binaryDirectory + " \n");
			ZenError.log("chmod 777 " + this.binaryDirectory + " \n");

			outputStream.writeBytes("exit\n");

			final StringBuilder feedback = new StringBuilder();
			String input, error;
			while ((input = inputStream.readLine()) != null) {
				feedback.append(input);
			}
			while ((error = errorStream.readLine()) != null) {
				feedback.append(error);
			}

			final String chmodResult = feedback.toString();
			ZenError.log(chmodResult);

			outputStream.close();
			inputStream.close();
			errorStream.close();
			process.waitFor();
			process.destroy();

			if (chmodResult.length() > 0) {
				Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
						threadID+" "+chmodResult).sendToTarget();
			}
		} catch (IOException e) {
			ZenError.log(e);
			Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
					threadID+" "+e.toString()).sendToTarget();
		} catch (InterruptedException e) {
			ZenError.log(e);
			Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
					threadID+" "+e.toString()).sendToTarget();
		}
	}

	public synchronized void run() {
		ZenError.log(Thread.currentThread().getName());
		Message.obtain(MainActivity.handler, Constants.PROGRESS_DIALOG_START,
				(Object) threadID+" "+"Installing Nmap binaries...").sendToTarget();
		File myFile;

		for (InstallerBinary install : installerBinaries) {
			final String filename = binaryDirectory + install.filename;

			Message.obtain(MainActivity.handler,
					Constants.PROGRESS_DIALOG_CHANGE_TEXT,
					(Object) threadID+" "+"Installing " + install.filename).sendToTarget();
			myFile = new File(filename);

			deleteExistingFile(myFile);

			MessageDigest md5 = writeNewFile(myFile, install.files);
			
			if (install.executable) {
				setExecutable(myFile);
			}
			
			ZenError.log("Installed " + install.filename + " (MD5 hash: " + getHash(md5) + ").");
		}
		StringBuilder commandCall = new StringBuilder(binaryDirectory);
		commandCall.append("tar").append(' ').append("-xf").append(' ').append(binaryDirectory+"nse.tar -C ").append(binaryDirectory);
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

			if (normalOutput.length() <= 0) {
				ZenError.log("Couldnt install nmap scripts");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
					threadID+" "+errorOutput).sendToTarget();
		} catch (StandardErrorNotEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
					threadID+" "+errorOutput).sendToTarget();
		} catch (NullProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Message.obtain(MainActivity.handler, Constants.INSTALL_ERROR,
					threadID+" "+errorOutput).sendToTarget();
		}
		Message.obtain(MainActivity.handler, Constants.PROGRESS_DIALOG_DISMISS,
				threadID+"").sendToTarget();
		Message.obtain(MainActivity.handler,Constants.INSTALL_COMPLETE,
				threadID+"").sendToTarget();
	}
	
	private String getHash (MessageDigest digest) {
		StringBuffer hexString = new StringBuffer();
		byte[] hash = digest.digest();

		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		
		return hexString.toString();
	}
}
