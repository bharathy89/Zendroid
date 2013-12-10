package edu.codemonkey.zendroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.codemonkey.zendroid.Utilities.Utilities;
import edu.codemonkey.zendroid.constants.Constants;
import edu.codemonkey.zendroid.errors.ZenError;
import edu.codemonkey.zendroid.installpackage.Install;


@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private Button btnStart, btnHelp;
	private Spinner etxtArguments;
	private TextView txtResults;

	public static ProgressDialog progressDialog;

	private static Context context;
	private boolean hasRoot;
	private boolean forceRoot;
	private boolean showLogcat;
	private Spinner commandArray;
	private EditText totalCommand;
	private ListView scanListView;
	public static Handler handler;
	private Button newScan;

	private Thread scanThread;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CustomArrayAdapter arrayAdapter;
	private Button addScript;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.app_name,  /* "open drawer" description */
				R.string.app_name  /* "close drawer" description */
				) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle("Zendroid");
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("Zendroid");
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		scanListView = (ListView) findViewById(R.id.scanList);

		newScan = (Button) findViewById(R.id.newScan);
		newScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createNewScanEnv();
				arrayAdapter.removeSelection();
				mDrawerLayout.closeDrawers();
			}
		});

		arrayAdapter = new CustomArrayAdapter(this, R.layout.scan_list_item);
		arrayAdapter.addAll(LocalDBHandler.getInstance().scanList);
		scanListView.setAdapter(arrayAdapter);
		arrayAdapter.notifyDataSetChanged();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		btnStart = (Button) findViewById(R.id.Start);
		btnHelp = (Button) findViewById(R.id.Help);
		totalCommand = (EditText) findViewById(R.id.totalCommand);
		commandArray = (Spinner) findViewById(R.id.commandSpinner);
		commandArray.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				if(commandArray.getSelectedItemPosition()==0) {
					etxtArguments.setEnabled(true);
					totalCommand.setText("nmap " + Constants.ARGUMENTSMAP.get(etxtArguments.getSelectedItem())+" ");
				} else {
					etxtArguments.setEnabled(false);
					totalCommand.setText(commandArray.getSelectedItem().toString()+" ");
				}
				totalCommand.setSelection(totalCommand.getText().length());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}

		});

		etxtArguments = (Spinner) findViewById(R.id.argumentSpinner);
		etxtArguments.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				totalCommand.setText("nmap " +Constants.ARGUMENTSMAP.get(etxtArguments.getSelectedItem())+" ");
				totalCommand.setSelection(totalCommand.getText().length());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		totalCommand.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					totalCommand.setSelection(totalCommand.getText().length());
				}
			}
		});

		txtResults = (TextView) findViewById(R.id.Results);

		context = this.getApplicationContext();

		btnStart.setEnabled(true); // must be enabled by license server

		btnStart.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				txtResults.setText(">");
				if (scanThread == null || !scanThread.isAlive()) {
					hasRoot = forceRoot ? forceRoot : Utilities.canGetRoot();
					Constants.threadID = System.currentTimeMillis();
					Scan scan = new Scan(Utilities.getApplicationFolder(context,
							"bin"), hasRoot, 0, context, false, totalCommand.getText().toString(), Constants.threadID);
					scanThread = new Thread(scan);
					scanThread.start();
					scanThread.setName("Scan thread: ");
					btnStart.setEnabled(false);
				} else {
					scanThread.interrupt(); // TODO this does nothing
				}
			}
		});

		btnHelp.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (scanThread == null || !scanThread.isAlive()) {
					Constants.threadID = System.currentTimeMillis();
					Scan scan = new Scan(Utilities.getApplicationFolder(context,
							"bin"), hasRoot, 1, context, false, commandArray.getSelectedItem().toString(), Constants.threadID);
					scanThread = new Thread(scan);
					scanThread.setName("Help thread");
					scanThread.start();
				} else {
					scanThread.interrupt(); // TODO this does nothing
				}
			}
		});
		
		addScript = (Button) findViewById(R.id.newScript);
		addScript.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startFolderActivity();
			}
		});

	}
	
	private void startFolderActivity() {
		Intent folderActivityIntent = new Intent(this,FolderActivity.class);
		startActivity(folderActivityIntent);
	}

	private void createNewScanEnv() {
		ScanObject scan = new ScanObject();
		CurrentSelections.getInstance().setCurrentScanObject(scan);
		commandArray.setSelection(0);
		etxtArguments.setSelection(0);
		commandArray.setSelected(true);
		totalCommand.setText("nmap " + Constants.ARGUMENTSMAP.get(etxtArguments.getSelectedItem())+" ");
		txtResults.setText(">");
	}

	public static Context getContext() {
		return (context);
	}

	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO what happens if a thread is running when the handler is resumed?
				ZenError.log("Handler received: " + msg.what + " Object: "
						+ msg.obj);

				if (msg.obj != null && !(msg.obj instanceof String))
				{
					ZenError.log("Warning: handler received unexpected non-null Message.obj not instanceof String.");
				}
				if(msg.what != Constants.POST_TOAST && msg.obj instanceof String && !((String)msg.obj).startsWith(Constants.threadID+"")) {

					Utilities.removeFile(msg.obj.toString().split(" ", 1)[0]);
					return;
				}

				InputMethodManager imm = (InputMethodManager)getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(totalCommand.getWindowToken(), 0);
				switch (msg.what) {
				case Constants.INSTALL_COMPLETE:
					// TODO do nothing, no output to show user.
					break;
				case Constants.INSTALL_ERROR:
					// TODO show error to user.
					break;
				case Constants.PROGRESS_DIALOG_START:
					progressDialog = new ProgressDialog(MainActivity.this);
					progressDialog.setMessage(msg.obj.toString());
					progressDialog.setCancelable(false);
					progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (scanThread != null && scanThread.isAlive()) {
								scanThread = null;
								Constants.threadID = 0;
							}
							progressDialog.dismiss();
							btnStart.setEnabled(true);
						}
					});
					progressDialog.show();
					break;
				case Constants.PROGRESS_DIALOG_DISMISS:
					if(progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
						btnStart.setEnabled(true);
					}
					break;
				case Constants.PROGRESS_DIALOG_CHANGE_TEXT:
					if (progressDialog.isShowing())
						progressDialog.setMessage((String) msg.obj);
					else
						ZenError.log("Progress dialog is not showing but text changed.");
					break;
				case Constants.SCAN_ERROR_NULL_PROCESS:
					txtResults
					.setText("Unable to start compiled Nmap program.");
					btnStart.setEnabled(true);
					break;
				case Constants.SCAN_ERROR_IOEXCEPTION:
					StringBuilder sb = new StringBuilder("An I/O error occured.\n");
					sb.append(msg.obj);
					sb.append('\n');
					if (forceRoot) {
						sb.append("Force Root is turned on - are you sure the \"su\" command is available?");
					}
					AlertDialog.Builder alertIOException = new AlertDialog.Builder(
							MainActivity.this).setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					alertIOException.setMessage(sb);
					alertIOException.show();
					txtResults.setText((String) msg.obj);
					btnStart.setEnabled(true);
					break;
				case Constants.SCAN_ERROR_STANDARD_ERROR:
					AlertDialog.Builder alert = new AlertDialog.Builder(
							MainActivity.this).setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					alert.setMessage((String) msg.obj);
					alert.show();
					btnStart.setEnabled(true);
					break;
				case Constants.SCAN_COMPLETE:
					String result = (String) msg.obj;
					txtResults.setText(result);
					CurrentSelections.getInstance().getCurrentScanObject().setFullCommand(totalCommand.getText().toString());
					CurrentSelections.getInstance().getCurrentScanObject().setResult(""+Constants.threadID);
					break;
				default:
					Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					ZenError.log("Handler received unexpected message that the switch statement does not allow.");
				}
			}
		};


		//		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//		autoCompleteTarget.setText(settings.getString("target", ""));
		//		etxtArguments.setText(settings.getString("args", ""));
		//		forceRoot = settings.getBoolean("forceRoot", false);
		//		int lastVersionRun = settings.getInt("versionLastRun", -1);
		//		showLogcat = settings.getBoolean("showLogcat", false);
		//		ZenError.setLogcatVisible(showLogcat);


		if(LocalDBHandler.VERSIONUPDATED ) {
			LocalDBHandler.VERSIONUPDATED  = false;
			hasRoot = forceRoot ? forceRoot : Utilities.canGetRoot();
			Constants.threadID = System.currentTimeMillis();
			Install install = new Install(MainActivity.context,
					Utilities.getApplicationFolder(MainActivity.context,
							"bin"), hasRoot, Constants.threadID);
			Thread installThread = new Thread(install);
			installThread.setName("Install Thread");
			installThread.start();
		}


		if (forceRoot) {
			ZenError.log("Found true forceRoot key in Shared Preferences.");
			hasRoot = true;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if(item.getTitle().equals("save")) {
			CurrentSelections.getInstance().getCurrentScanObject().saveScan();
			arrayAdapter.clear();
			arrayAdapter.addAll(LocalDBHandler.getInstance().scanList);
			arrayAdapter.notifyDataSetChanged();
		}else if(item.getTitle().equals("share")) {
			AlertDialog.Builder illegalStateBuilder = new AlertDialog.Builder(MainActivity.this);
			illegalStateBuilder.setMessage("Send Mail");
			illegalStateBuilder.setPositiveButton("Yes", sendMailListener).setNegativeButton("No", sendMailListener);
			illegalStateBuilder.show();
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_menu, menu);
		return true;
	}


	private DialogInterface.OnClickListener sendMailListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			switch (which)
			{
			case DialogInterface.BUTTON_POSITIVE:
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Zendroid - Scan Reports");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, CurrentSelections.getInstance().getCurrentScanObject().getFullCommand());
				String extention = CurrentSelections.getInstance().getCurrentScanObject().getFullCommand().startsWith("nmap")?".nmap":(CurrentSelections.getInstance().getCurrentScanObject().getFullCommand().startsWith("ncat")?".ncat":".nping");
				if(CurrentSelections.getInstance().getCurrentScanObject().getResult() != null && !CurrentSelections.getInstance().getCurrentScanObject().getResult().equals("")) {
					emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Utilities.getScanStorageDirectory()+"/"+ CurrentSelections.getInstance().getCurrentScanObject().getResult()+extention));
				}
				MainActivity.this.startActivity(emailIntent);
				break;
			case DialogInterface.BUTTON_NEGATIVE:

				break;
			}
		}
	};


	class CustomArrayAdapter extends ArrayAdapter<ScanObject> {
		Context context;
		View selectedView;
		public CustomArrayAdapter(Context context, int resource) {
			super(context, resource);
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewGroup listItem;

			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				listItem = (ViewGroup) inflater.inflate(R.layout.scan_list_item, null, false);
			}
			else
			{
				listItem = (ViewGroup) convertView;
			}


			final ScanObject scan = getItem(position);
			((TextView)listItem.findViewById(R.id.scanTitle)).setText(scan.getFullCommand());
			((Button)listItem.findViewById(R.id.deleteScan)).setOnClickListener(new OnClickListener() {
				ScanObject scanObj = scan;
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(CurrentSelections.getInstance().getCurrentScanObject().equals(scanObj)) {	
						createNewScanEnv();
						if(selectedView != null) {
							selectedView.setSelected(false);
							selectedView = null;
						}
					}
					scanObj.deleteScan();
					CustomArrayAdapter.this.clear();
					CustomArrayAdapter.this.addAll(LocalDBHandler.getInstance().scanList);
					CustomArrayAdapter.this.notifyDataSetChanged();
				}
			});
			listItem.setOnClickListener(new OnClickListener() {
				ScanObject scanObj = scan;
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CurrentSelections.getInstance().setCurrentScanObject(scanObj);
					if(scanObj.getFullCommand().startsWith("nmap")) {
						commandArray.setSelection(0);
						txtResults.setText(Utilities.readResultsFile(CurrentSelections.getInstance().getCurrentScanObject().getResult(),".nmap"));
					}else if(scanObj.getFullCommand().startsWith("ncat")) {
						commandArray.setSelection(1);
						txtResults.setText(Utilities.readResultsFile(CurrentSelections.getInstance().getCurrentScanObject().getResult(),".ncat"));
					}else {
						commandArray.setSelection(2);
						txtResults.setText(Utilities.readResultsFile(CurrentSelections.getInstance().getCurrentScanObject().getResult(),".nping"));
					}
					totalCommand.setText(scanObj.getFullCommand());
					
					if(selectedView != null) {
						selectedView.setSelected(false);
					}
					selectedView = v;
					v.setSelected(true);
					mDrawerLayout.closeDrawers();
				}
			});
			return listItem;
		}

		public void removeSelection() {
			if(selectedView != null) {
				selectedView.setSelected(false);
				selectedView = null;
			}
		}

	}

}


