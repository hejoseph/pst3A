package com.abh.nfcunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HistoryActivity extends Activity implements View.OnClickListener{

	private JSONObject root, history;
	private String logs;
	private TextView txt;
	private List<String> liste;
	private String s1;
	private EditText lat, lon;
	private Button clearHistory, bouton2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		readFromJSON();
		writeToJSON();
		try {
			Log.d("History Activity", root.getString("history"));
			Log.d("still", history.getString("logs"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		txt = (TextView) findViewById(R.id.textView);
		clearHistory = (Button) findViewById(R.id.clearHistory);
		clearHistory.setOnClickListener(this);
		try {
			logs = history.getString("logs");
			List<String> items = Arrays.asList(logs.split(","));
			txt.setText("Voici l'historique : \n");
			for (String s : items) {
				txt.append(s+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private OnClickListener clearHistoryListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
//			history.put("logs", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		writeToJSON();
		}
	};

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
			history = root.getJSONObject("history");

		} catch (JSONException e) {
			try {
				history = new JSONObject();
				root.put("history", history);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		
		try {
			logs = history.getString("logs");

		} catch (JSONException e) {
			try {
				logs = "";
				history.put("logs", logs);

			} catch (JSONException e1) {
				e1.printStackTrace();
			}
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

	@Override
	public void onClick(View v) {
		Log.d("history click","clicked");
		
		if (v.getId() == R.id.clearHistory) {
			try {
				history.put("logs", "");
				txt.setText("");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeToJSON();
		}
		
	}

}
