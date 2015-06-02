package com.abh.nfcunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class LockScreenActivity extends Activity implements
		View.OnClickListener {

    private NfcAdapter nfcAdapter;  
	
	private SimpleDateFormat sdf;
	
	private JSONObject root;
	private JSONObject settings, history, applications;
	private String pin;
	private Boolean pinLocked;
	
	private List<String> listApps;

	private String pinEntered = ""; // The PIN the user inputs

	private int flags; // Window flags
	private TextView pinInput;
	private int launcherPickToast = 2;
	private PackageManager packageManager;
	private int componentEnabled;
	private int componentDisabled;
	private ComponentName cnHome;
	private String tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.d(this.getClass().toString(),"inside onCreate");
		
		 nfcAdapter = NfcAdapter.getDefaultAdapter(this); 
		readFromJSON();
		flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		// this.getWindow().addFlags(flags);
		setContentView(R.layout.activity_lock);

		packageManager = getPackageManager();
		componentEnabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		componentDisabled = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

		cnHome = new ComponentName(this, "com.abh.nfcunlocker.LockHome");

		// Enable home launcher activity component
		packageManager.setComponentEnabledSetting(cnHome, componentEnabled,
				PackageManager.DONT_KILL_APP);

		pinInput = (TextView) findViewById(R.id.pinInput);
		ImageButton ic_0 = (ImageButton) findViewById(R.id.ic_0);
		ImageButton ic_1 = (ImageButton) findViewById(R.id.ic_1);
		ImageButton ic_2 = (ImageButton) findViewById(R.id.ic_2);
		ImageButton ic_3 = (ImageButton) findViewById(R.id.ic_3);
		ImageButton ic_4 = (ImageButton) findViewById(R.id.ic_4);
		ImageButton ic_5 = (ImageButton) findViewById(R.id.ic_5);
		ImageButton ic_6 = (ImageButton) findViewById(R.id.ic_6);
		ImageButton ic_7 = (ImageButton) findViewById(R.id.ic_7);
		ImageButton ic_8 = (ImageButton) findViewById(R.id.ic_8);
		ImageButton ic_9 = (ImageButton) findViewById(R.id.ic_9);
		Button delete = (Button) findViewById(R.id.delete);
		Button validate = (Button) findViewById(R.id.validate);
		Button halt = (Button) findViewById(R.id.halt);

		// Set onClick listeners
		ic_0.setOnClickListener(this);
		ic_1.setOnClickListener(this);
		ic_2.setOnClickListener(this);
		ic_3.setOnClickListener(this);
		ic_4.setOnClickListener(this);
		ic_5.setOnClickListener(this);
		ic_6.setOnClickListener(this);
		ic_7.setOnClickListener(this);
		ic_8.setOnClickListener(this);
		ic_9.setOnClickListener(this);
		delete.setOnClickListener(this);
		validate.setOnClickListener(this);
		halt.setOnClickListener(this);

		// if (!isMyLauncherDefault()) {
		// // packageManager.clearPackagePreferredActivities(getPackageName());
		//
		// Intent launcherPicker = new Intent();
		// launcherPicker.setAction(Intent.ACTION_MAIN);
		// launcherPicker.addCategory(Intent.CATEGORY_HOME);
		// startActivity(launcherPicker);
		//
		// if (launcherPickToast > 0) {
		// launcherPickToast -= 1;
		//
		// Toast toast = Toast.makeText(getApplicationContext(),
		// "Bla bla bla Lol",
		// Toast.LENGTH_LONG);
		// toast.show();
		// }
		// }

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (packageManager != null) {
			packageManager.setComponentEnabledSetting(cnHome,
					componentDisabled, PackageManager.DONT_KILL_APP);
		}
	}

	public boolean isMyLauncherDefault() {
		final IntentFilter launcherFilter = new IntentFilter(Intent.ACTION_MAIN);
		launcherFilter.addCategory(Intent.CATEGORY_HOME);

		List<IntentFilter> filters = new ArrayList<IntentFilter>();
		filters.add(launcherFilter);

		final String myPackageName = getPackageName();
		List<ComponentName> activities = new ArrayList<ComponentName>();

		// packageManager.getPreferredActivities(filters, activities,
		// "com.moonpi.tapunlock");

		for (ComponentName activity : activities) {
			if (myPackageName.equals(activity.getPackageName())) {
				return true;
			}
		}

		return false;
	}

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
		// Read from JSON file
		try {
			BufferedReader bRead = new BufferedReader(new InputStreamReader(
					openFileInput("settings.json")));
			root = new JSONObject(bRead.readLine());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read settings object from root
		try {
			settings = root.getJSONObject("settings");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Read required items from settings object
		try {
			pin = settings.getString("pin");
			tag = settings.getString("tag");
			pinLocked = settings.getBoolean("pinLocked");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			history = root.getJSONObject("history");

		} catch (JSONException e) {
			try {
				history = new JSONObject();
				root.put("history", history);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		// Read settings, or put JSON object in root if it doesn't exist
		try {
			applications = root.getJSONObject("applications");

		} catch (JSONException e) {
			try {
				applications = new JSONObject();
				root.put("applications", applications);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		// Read settings, or put if they don't exist
		try {
			String apps = applications.getString("listapps");
			List<String > listAppsTmp = Arrays.asList(apps.split(","));
			listApps = new ArrayList<String>(listAppsTmp);
			
		} catch (JSONException e) {
			try {
				applications.put("listapps", "");
				listApps = new ArrayList<String>();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} 
	}
	
	private void setPin(String s) {
		pinEntered += s;
		pinInput.setText(pinEntered);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.ic_0) {
			setPin("0");
		}

		else if (v.getId() == R.id.ic_1) {
			if (pinEntered.length() < pin.length())
				setPin("1");
		}

		else if (v.getId() == R.id.ic_2) {
			if (pinEntered.length() < pin.length())
				setPin("2");
		}

		else if (v.getId() == R.id.ic_3) {
			if (pinEntered.length() < pin.length())
				setPin("3");
		}

		else if (v.getId() == R.id.ic_4) {
			if (pinEntered.length() < pin.length())
				setPin("4");
		}

		else if (v.getId() == R.id.ic_5) {
			if (pinEntered.length() < pin.length())
				setPin("5");
		}

		else if (v.getId() == R.id.ic_6) {
			if (pinEntered.length() < pin.length())
				setPin("6");
		}

		else if (v.getId() == R.id.ic_7) {
			if (pinEntered.length() < pin.length())
				setPin("7");
		}

		else if (v.getId() == R.id.ic_8) {
			if (pinEntered.length() < pin.length())
				setPin("8");
		}

		else if (v.getId() == R.id.ic_9) {
			if (pinEntered.length() < pin.length())
				setPin("9");
		}

		else if (v.getId() == R.id.delete) {
			Log.d("hi", "1");
			if (pinEntered.length() > 0) {
				Log.d("hi", "2");
				if (pinEntered.length() == 1) {
					Log.d("hi", "3");
					pinEntered = "";
					pinInput.setText(pinEntered);
				}

				else {
					Log.d("hi", "4");
					pinEntered = pinEntered.substring(0,
							pinEntered.length() - 1);
					pinInput.setText(pinEntered);
				}
			}
		}
		
		else if (v.getId() == R.id.validate) {
			if (pinEntered.length() == pin.length()) {
                // If correct PIN entered, reset pinEntered, remove handle callbacks and messages,
                // Set home launcher activity component disabled and finish
                if (pinEntered.equals(pin)) {
                    pinEntered = "";
                    pinInput.setText(pinEntered);
                    finishActivity();
                } else {
                	pinEntered = "";
                    pinInput.setText(pinEntered);
                    
                }
			}
		}
		
		else if (v.getId() == R.id.halt) {
			finishActivity();
		}
		
	}
	
	private void finishActivity(){
		String s1, s  ="";
		sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date d = new Date();
		s1 = sdf.format(d);
		try {
			s = history.getString("logs");
//			String[] s1 = s.split(",");
			if(s.length() > 0){
				s+=",";
			}
			s+=s1;
			history.put("logs", s);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writeToJSON();
		finish();
		launchingApps();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}

	@Override
    protected void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, new Intent(getApplicationContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null); /* Je laisse les paramètres à NULL car je lis tout type de tag, je n'ai pas besoin de filtre */
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
      //      tagRead.setText("OUI !");
        	Log.d("inside LockScreenActivity","read Tag");
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null && rawMsgs.length > 0) { /* Si on a pu lire un message */
            	Log.d("inside LockScreenActivity","Something is being read");
            	Log.d("my class -tag-",tag);
                NdefMessage ndefMessage = (NdefMessage) rawMsgs[0]; /* On le caste en NDEFMessage */
                NdefRecord[] records = ndefMessage.getRecords(); /* On obtient alors l'ensemble de ses records */
                
                if (records.length > 0) {
                    NdefRecord record = records[0]; /* On suppose que seul le premier record contient des données */
                    String msg = new String(record.getPayload()); /* On extrait alors son payload */
                    	Log.d("msg read",msg);
						if(tag.equals(msg)){
							finishActivity();
						}        
                }

            }
        }
	
    }
	
	private void launchingApps(){
		for(String s : listApps){
			Intent intent = packageManager
					.getLaunchIntentForPackage(s);
					Log.d(this.getClass().toString(),"nom de l'appli");
					Log.d(this.getClass().toString(),s);
					if (null != intent) {
						startActivity(intent);
					}
		}
	}
	
}
