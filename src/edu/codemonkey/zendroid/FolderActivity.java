package edu.codemonkey.zendroid;

import java.io.File;

import edu.codemonkey.zendroid.Utilities.Utilities;
import edu.codemonkey.zendroid.installpackage.NSEScriptsInstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class FolderActivity extends Activity  implements IFolderItemListener  {

	FolderLayout localFolders;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        localFolders = (FolderLayout)findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
            localFolders.setDir("/sdcard");//change directory if u want,default is root   

    }

    //Your stuff here for Cannot open Folder
    public void OnCannotFileRead(File file) {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.ic_launcher)
        .setTitle(
                "[" + file.getName()
                        + "] folder can't be read!")
        .setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {


                    }
                }).show();

    }


    //Your stuff here for file Click
    public void OnFileClicked(final File file) {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.ic_launcher)
        .setTitle("Installing " + file.getName() + " to nmap")
        .setPositiveButton("INSTALL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int which) {
                    	NSEScriptsInstaller nseScriptsInstaller = new NSEScriptsInstaller(file.getAbsolutePath(), 
                    			Utilities.getApplicationFolder(FolderActivity.this, "/bin/scripts"));
                    	Thread thread = new Thread(nseScriptsInstaller);
                    	thread.start();
                    	FolderActivity.this.finish();
                    }
                })
        .setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int which) {
                    	FolderActivity.this.finish();
                    }
                }).show();
    }

}

