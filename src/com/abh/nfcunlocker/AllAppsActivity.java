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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

public class AllAppsActivity extends ListActivity  {
	private PackageManager packageManager = null;
	private List<ApplicationInfo> applist = null;
	private ApplicationAdapter listadaptor = null;
	private List<String> listApps;
	
	private JSONObject root, applications;
	
	AllAppsActivity aaa;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(this.getClass().toString(),"onCreate");
		setContentView(R.layout.allappsbackground);
		readFromJSON();
		writeToJSON();
		aaa = this;
		packageManager = getPackageManager();
		new LoadApplications().execute();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(this.getClass().toString(),"onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(this.getClass().toString(),"onOptionsItemSelected");
		boolean result = true;

		switch (item.getItemId()) {
		case R.id.menu_about: {
			displayAboutDialog();
			
			break;
		}
		default: {
			result = super.onOptionsItemSelected(item);

			break;
		}
		}

		return result;
	}

	private void displayAboutDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Aide");
		builder.setMessage("Voulez-vous décocher toutes les applications ?	");
		
		
		builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int id) {
//		    	   Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://javatechig.com"));
//		    	   startActivity(browserIntent);
		    	   try {
					applications.put("listapps", "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	   writeToJSON();
		    	   finish();
		    	   dialog.cancel();
		       }
		   });
		builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int id) {
		            dialog.cancel();
		       }
		});
		 
		builder.show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.d(this.getClass().toString(),"onListItemClick");
		ApplicationInfo app = applist.get(position);
		try {
			Intent intent = packageManager
					.getLaunchIntentForPackage(app.packageName);
			Log.d(this.getClass().toString(),"nom de l'appli");
			Log.d(this.getClass().toString(),app.packageName);
			if (null != intent) {
				startActivity(intent);
			}
		} catch (ActivityNotFoundException e) {
			Toast.makeText(AllAppsActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(AllAppsActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
		ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo info : list) {
			try {
				if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
					applist.add(info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return applist;
	}

	private class LoadApplications extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress = null;

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(this.getClass().toString(),"doInBackground");
			applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
			listadaptor = new ApplicationAdapter(AllAppsActivity.this,
					R.layout.snippet_list_row, applist);

			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.d(this.getClass().toString(),"onCancelled");
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(this.getClass().toString(),"onPostExecute");
			setListAdapter(listadaptor);
			progress.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			Log.d(this.getClass().toString(),"onPreExecute");
			progress = ProgressDialog.show(AllAppsActivity.this, null,
					"Loading application info...");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			Log.d(this.getClass().toString(),"onProgressUpdate");
		}
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
	
	
}