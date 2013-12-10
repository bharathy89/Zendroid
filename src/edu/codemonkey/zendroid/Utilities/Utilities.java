package edu.codemonkey.zendroid.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Message;
import android.util.Log;
import edu.codemonkey.zendroid.MainActivity;
import edu.codemonkey.zendroid.constants.Constants;
import edu.codemonkey.zendroid.errors.ZenError;

public class Utilities {

	static File su;
	static File tar;

	private Utilities() {
	}

	public static String getApplicationFolder(final Context context,
			final String subfolder) {
		File appDir = new File(getDataDirectory(context) + "/" + subfolder);

		try {
			// if it doesn't exist, create it
			if (appDir.exists()) {
				ZenError.log(appDir.getAbsolutePath() + " exists.");
			} else {
				ZenError.log(appDir.getAbsolutePath() + " does not exist.");
				if (appDir.mkdirs()) {
					ZenError.log("Created " + appDir.getAbsolutePath());
				} else {
					throw new CannotCreateDirectoryException(
							"Failed to create " + appDir.getAbsolutePath(),
							appDir);
				}
			}
		} catch (CannotCreateDirectoryException e) {
			ZenError.log(e.toString());
			appDir = new File("/tmp/");
		}

		return appDir.getAbsolutePath() + "/";
	}

	private static String getDataDirectory(final Context context) {
		String dataDirectory = "";
		try {
			dataDirectory = context.getPackageManager().getApplicationInfo(
					"edu.codemonkey.zendroid", 0).dataDir;
		} catch (NameNotFoundException e) {
			// this wouldn't be good, but it is very unlikely to happen.
			ZenError.log(e.toString());
		}
		return dataDirectory;
	}

	/**
	 * Tests if the handset has a "su" command available in
	 * the various locations specified in $PATH. If API 9 or greater,
	 * also tests if that the su command is executable or not.
	 * 
	 * @return
	 */
	public static boolean canGetRoot() {
		String path = System.getenv("PATH");
		StringTokenizer st = new StringTokenizer(path, ":");
		boolean suFound = false;
		try {
			while (st.hasMoreTokens()) {

				String nextToken = st.nextToken();

				File suTest = new File(nextToken + "/su");

				// File.canExecute() is a feature only available to
				// SDK 9 and above. This code should fix that compatibility
				// problem.
				if (android.os.Build.VERSION.SDK_INT >= 9)
					suFound = suTest.exists() && suTest.canExecute();
				else
					suFound = suTest.exists();

				if (suFound) {
					Utilities.su = suTest;
					break;
				}

			}
		} catch (NoSuchMethodError e) {
			ZenError.log(e);
		}
		return suFound;
	}

	public static boolean isSDCardMounted() {
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

		Message.obtain(MainActivity.handler,
				Constants.POST_TOAST,"SDcard "+(isSDPresent?" detected":" not detected") ).sendToTarget();
		return isSDPresent;
	}

	public static String getScanStorageDirectory() {
		if(isSDCardMounted()) {
			File direct = new File(android.os.Environment.getExternalStorageDirectory() + "/ScanResultFolder/");
			try {
				if(!direct.exists())
				{
					direct.mkdir();

				}
				if(direct.isDirectory() && direct.exists()) {
					Message.obtain(MainActivity.handler,
							Constants.POST_TOAST,direct.getAbsolutePath()+ " detected" ).sendToTarget();
				}else {
					Message.obtain(MainActivity.handler,
							Constants.POST_TOAST,direct.getAbsolutePath()+ " not detected" ).sendToTarget();
				}
				return direct.getAbsolutePath();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public static String readResultsFile(String filename,String extention) {
		if(isSDCardMounted()) {
			File sdcard = android.os.Environment.getExternalStorageDirectory();

			//Get the text file
			File file = new File(getScanStorageDirectory(),filename+extention);
			if(file.exists()) {
				//Read text from file
				StringBuilder text = new StringBuilder();

				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;

					while ((line = br.readLine()) != null) {
						text.append(line);
						text.append('\n');
					}
				
					return text.toString();
				}catch (IOException e) {
					//You'll need to add proper error handling here
					return ">";
				}
			}
		}
		return ">";
	}
	
	public static boolean removeFile(String filename) {
		boolean finalboolean = false;
		if(isSDCardMounted()) {
			File sdcard = android.os.Environment.getExternalStorageDirectory();
			if(filename== null){
				return false;
			}
			//Get the text file
			File file1 = new File(getScanStorageDirectory(),filename+".nmap");
			if(file1.exists()) {
				finalboolean = file1.delete();
			}
			File file2 = new File(getScanStorageDirectory(),filename+".gnmap");
			if(file2.exists()) {
				finalboolean = finalboolean && file2.delete();
			}
			File file3 = new File(getScanStorageDirectory(),filename+".xml");
			if(file3.exists()) {
				
				finalboolean =finalboolean && file3.delete();
			}
			File file4 = new File(getScanStorageDirectory(),filename+".ncat");
			if(file4.exists()) {
				
				finalboolean =finalboolean && file4.delete();
			}
			File file5 = new File(getScanStorageDirectory(),filename+".nping");
			if(file5.exists()) {
				
				finalboolean =finalboolean && file5.delete();
			}
			
			return finalboolean;
		}
		return false;
	}

	/**
	 * Custom exception for handling errors where
	 * Runtime.getRuntime().exec(String) returned null.
	 * 
	 */
	public static class NullProcessException extends Exception {

		private static final long serialVersionUID = -6606740982523502676L;

		/**
		 * Use if Runtime.getRuntime().exec(String) returns null.
		 */
		public NullProcessException() {
			super();
		}
	}

	/**
	 * Custom exception extending IOException. This is useful because
	 * File.mkdirs() does not throw an IOException.
	 * 
	 */
	static class CannotCreateDirectoryException extends IOException {

		private static final long serialVersionUID = -7566027000405830050L;

		public File directory;

		/**
		 * If you find File.mkdirs() returns false, throw this exception.
		 * 
		 * @param detailMessage
		 *            Absolute path of the directory you attempted to create.
		 */
		public CannotCreateDirectoryException(final String detailMessage,
				final File directory) {
			super(detailMessage);
			this.directory = directory;
		}
	}

	/**
	 * Custom exception for handling case where Nmap executable returned
	 * standard error.
	 * 
	 */
	public static class StandardErrorNotEmptyException extends Exception {

		private static final long serialVersionUID = 866136689299038573L;

		/**
		 * Under normal circumstances there should be nothing on the standard
		 * error stream after running Nmap. If there is, you need to throw this
		 * exception.
		 * 
		 * @param detailMessage
		 *            Everything read from the standard error stream.
		 */
		public StandardErrorNotEmptyException(final String detailMessage) {
			super(detailMessage);
		}
	}

	private static class UtilitiesSingletonHolder {
		private static final Utilities INSTANCE = new Utilities();
	}

	/**
	 * Lazy initialization, how cool is that?
	 * 
	 * @return
	 */
	public static Utilities getInstance() {
		return UtilitiesSingletonHolder.INSTANCE;
	}
}
