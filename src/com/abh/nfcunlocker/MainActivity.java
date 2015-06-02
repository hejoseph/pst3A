package com.abh.nfcunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener,
		CompoundButton.OnCheckedChangeListener {

	public final static String historyDataId ="historyDataId";
	
	ImageButton creditsImageButton, historyImageButton, settingsImageButton, shortcutImageButton;
	TextView pinCodeET;
	Button setPinButton;
	LinearLayout layoutTop;

	private InputMethodManager imm;

	// Content of JSON file
	private JSONObject root;
	private JSONObject settings;

	private boolean onStart;
	private ImageButton syncNfcImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		readFromJSON();
		writeToJSON();
		readFromJSON();

		
		creditsImageButton = (ImageButton) findViewById(R.id.creditsImageButton);
		creditsImageButton.setOnClickListener(creditsImageButtonListener);
		setPinButton = (Button) findViewById(R.id.setPinButton);
		setPinButton.setOnClickListener(this);
		
		shortcutImageButton = (ImageButton) findViewById(R.id.shortcutImageButton);
		shortcutImageButton.setOnClickListener(this);
		
		settingsImageButton = (ImageButton) findViewById(R.id.settingsImageButton);
		settingsImageButton.setOnClickListener(this);
		
		historyImageButton  = (ImageButton) findViewById(R.id.historyImageButton);
		historyImageButton.setOnClickListener(this);
		pinCodeET = (EditText) findViewById(R.id.pinCodeET);
		layoutTop = (LinearLayout) findViewById(R.id.layoutTop);
		
		syncNfcImageButton = (ImageButton) findViewById(R.id.syncNfcImageButton);
		syncNfcImageButton.setOnClickListener(this);
		
		try{
			pinCodeET.setText(settings.getString("pin"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		Switch lockScreenSwitch = (Switch)findViewById(R.id.lockScreenSwitch);
		lockScreenSwitch.setOnCheckedChangeListener(this);
		try {
            if (settings.getBoolean("lockscreen")) {
                onStart = true;
                lockScreenSwitch.setChecked(true);
            }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		layoutTop.bringToFront();
		layoutTop.invalidate();
		
	}

	// Write content to JSON file
	public void writeToJSON() {
		try {
			BufferedWriter bWrite = new BufferedWriter(new OutputStreamWriter(
					openFileOutput("settings.json", Context.MODE_PRIVATE)));
			bWrite.write(root.toString());
			bWrite.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readFromJSON() {
		// Read root from JSON file
		try {
			BufferedReader bRead = new BufferedReader(new InputStreamReader(
					openFileInput("settings.json")));
			root = new JSONObject(bRead.readLine());

		} catch (FileNotFoundException e) {
			e.printStackTrace();

			root = new JSONObject();

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read settings, or put JSON object in root if it doesn't exist
		try {
			settings = root.getJSONObject("settings");

		} catch (JSONException e) {
			try {
				settings = new JSONObject();
				root.put("settings", settings);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		// Read settings, or put if they don't exist
		try {
			Boolean lockscreen = settings.getBoolean("lockscreen");
			String pin = settings.getString("pin");
			String tag = settings.getString("tag");
		} catch (JSONException e) {
			try {

				settings.put("lockscreen", false);
				settings.put("pin", "");
				settings.put("tag", "");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}

	private OnClickListener creditsImageButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
//			Intent mIntent = new Intent(MainActivity.this, LockScreenActivity.class);
//			startActivity(mIntent);
			Intent intent = new Intent(MainActivity.this, InfoNfcUnlocker.class);
			startActivity(intent);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.setPinButton) {

			// // If PIN length between 4 and 6, store PIN and toast successful
			if (pinCodeET.length() >= 4 && pinCodeET.length() <= 6) {

				new AlertDialog.Builder(this)
						.setMessage("Confirmer?")
						.setPositiveButton("Oui",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {

											settings.put("pin", String
													.valueOf(pinCodeET
															.getText()));

										} catch (JSONException e) {
											e.printStackTrace();
										}

										writeToJSON();

										Toast toast = Toast.makeText(
												getApplicationContext(),
												"Pin Code is now set",
												Toast.LENGTH_SHORT);
										toast.show();

										imm.hideSoftInputFromWindow(
												pinCodeET.getWindowToken(), 0);

									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										imm.hideSoftInputFromWindow(
												pinCodeET.getWindowToken(), 0);
										// Do nothing, close dialog
									}
								}).show();
			}

			// Toast user that PIN needs to be at least 4 digits long
			else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"4 characters mini", Toast.LENGTH_LONG);
				toast.show();
			}
		}
		
		if (v.getId() == R.id.syncNfcImageButton) {
			Intent intent = new Intent(MainActivity.this, MenuNFCReader.class);
			startActivity(intent);
		}
		
		if (v.getId() == R.id.historyImageButton) {
			Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
			startActivity(intent);
		}
		
		if(v.getId() == R.id.shortcutImageButton){
			Intent intent = new Intent(MainActivity.this, AllAppsActivity.class);
			startActivity(intent);
		}
		
		if(v.getId() == R.id.settingsImageButton){
			Intent intent = new Intent(MainActivity.this, Instruction.class);
			startActivity(intent);
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			try {
				// If no PIN remembered, toast user to enter a PIN
				Log.d("Bug","debug");
				Log.d("Content",settings.getString("pin"));
				if (settings.getString("pin").equals("")) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Set a pin code please", Toast.LENGTH_LONG);
					toast.show();

					buttonView.setChecked(false);
				}
				// If everything ok, set lockscreen true, start service and
				// store
				else {
					if (!onStart) {
						try {
							settings.put("lockscreen", true);
							Log.d("Mine","start service");
							startService(new Intent(this,LockScreenService.class));
							Log.d("Mine","After service");
							Toast toast = Toast.makeText(
									getApplicationContext(), "Lock is on",
									Toast.LENGTH_SHORT);
							toast.show();

						} catch (JSONException e) {
							e.printStackTrace();
						}

						writeToJSON();
						return;
					}

					onStart = false;
					startService(new Intent(this, LockScreenService.class));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// If unchecked, set lockscreen false, stop service and store
		else {
			try {
				settings.put("lockscreen", false);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			writeToJSON();

			killService(this);

			Toast toast = Toast.makeText(getApplicationContext(),
					"Lock is off", Toast.LENGTH_SHORT);
			toast.show();
		}

	}

	// Stop service
	public void killService(Context context) {
		// stopService(new Intent(context, ScreenLockService.class));
	}
}
