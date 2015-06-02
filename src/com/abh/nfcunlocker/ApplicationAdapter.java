package com.abh.nfcunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
	private List<ApplicationInfo> appsList = null;
	private Context context;
	private PackageManager packageManager;
	
	private List<String> listApps;
//	private CheckBox checkBoxLaunch;
	
	private JSONObject root;
	private JSONObject applications;
	
	public ApplicationAdapter(Context context, int textViewResourceId,
			List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		
		Log.d(this.getClass().toString(),"ApplicationAdapter");
		this.context = context;
		this.appsList = appsList;
		
		packageManager = context.getPackageManager();
		
		readFromJSON();
		writeToJSON();
	}

	@Override
	public int getCount() {
		Log.d(this.getClass().toString(),"getCount");
		return ((null != appsList) ? appsList.size() : 0);
	}

	@Override
	public ApplicationInfo getItem(int position) {
		Log.d(this.getClass().toString(),"getItem");
		return ((null != appsList) ? appsList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		Log.d(this.getClass().toString(),"getItemId");
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(this.getClass().toString(),"getView");
		View view = convertView;
		if (null == view) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.snippet_list_row, null);
		}
		Log.d("pb", ""+position);
		ApplicationInfo data = appsList.get(position);
		if (null != data) {
			TextView appName = (TextView) view.findViewById(R.id.app_name);
			TextView packageName = (TextView) view.findViewById(R.id.app_paackage);
			ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
			CheckBox checkBoxLaunch = (CheckBox) view.findViewById(R.id.checkBoxLaunch);
			checkBoxLaunch.setOnClickListener(checkBoxLaunchClickedListener);
			
			appName.setText(data.loadLabel(packageManager));
			packageName.setText(data.packageName);
			Log.d("test package name",data.packageName);
			iconview.setImageDrawable(data.loadIcon(packageManager));
			checkBoxLaunch.setContentDescription(data.packageName);
			Log.d("test package name in checkbox",(String)checkBoxLaunch.getContentDescription());
			String test = (String)checkBoxLaunch.getContentDescription();
			checkBoxLaunch.setChecked(false);
			for(String s : listApps){
				String packname = ((String)checkBoxLaunch.getContentDescription());
				if(packname.equals(s)){
					checkBoxLaunch.setChecked(true);
				}
			}
		}
		Log.d("class ", ""+listApps.size());
		return view;
	}
	
	
	
	private OnClickListener checkBoxLaunchClickedListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Log.d(this.getClass().toString(),"clicked");
			String app_package = (String) v.getContentDescription();
			Log.d(this.getClass().toString(),app_package);
			
			Log.d(this.getClass().toString(), "ho");
			
			if(((CheckBox)v).isChecked()){
				listApps.add(app_package);
			} else {
				for(int i = 0; i<listApps.size(); i++){
					if(listApps.get(i).equals(app_package)){
						listApps.remove(i); 
					}
				}
			}
			Log.d("class ", listApps.toString());
			String recordingApps = "";
			for(String s : listApps){
				if(s == ""){
					continue;
				}
				recordingApps += ",";
				recordingApps += s;
			}
			int s = recordingApps.length();
			if(recordingApps.length() >0){
				recordingApps = recordingApps.substring(1,recordingApps.length());
			}
			
			Log.d("sauvegarde dans JSON", recordingApps);
			try {
				applications.put("listapps", recordingApps);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeToJSON();
//			try {
////				Intent intent = packageManager
////						.getLaunchIntentForPackage(s);
////				Log.d(this.getClass().toString(),"nom de l'appli");
////				Log.d(this.getClass().toString(),s);
////				if (null != intent) {
////					startActivity(intent);
//					
//					Intent i = packageManager
//							.getLaunchIntentForPackage(s);
//					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					context.getApplicationContext().startActivity(i);
//			} catch (ActivityNotFoundException e) {
//				Toast.makeText(context, e.getMessage(),
//						Toast.LENGTH_LONG).show();
//			} catch (Exception e) {
//				Toast.makeText(context, e.getMessage(),
//						Toast.LENGTH_LONG).show();
//			}
			Log.d(this.getClass().toString(), listApps.toString());

		}
	};
	
//	public void onCheckBoxLaunchClicked(){
//		Log.d(this.getClass().toString(), "Hey");
//	}
	
	// Write content to JSON file
		public void writeToJSON() {
			try {
				BufferedWriter bWrite = new BufferedWriter(new OutputStreamWriter(
						context.getApplicationContext().openFileOutput("settings.json", Context.MODE_PRIVATE)));
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
						context.getApplicationContext().openFileInput("settings.json")));
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
			} finally{
				
			}
		}
		
	
};