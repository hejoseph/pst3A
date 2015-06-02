package com.abh.nfcunlocker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenReceiver extends BroadcastReceiver {

	private Boolean lockscreen = false;
    private JSONObject root, settings;
    private Context thisContext;
	
    public void readFromJSON() {
        // Read from JSON file
        try {
            BufferedReader bRead = new BufferedReader(new InputStreamReader
                    (thisContext.getApplicationContext().openFileInput("settings.json")));
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
            lockscreen = settings.getBoolean("lockscreen");
            Log.d("jo",lockscreen.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		thisContext = context;
        readFromJSON();
        Log.d("mine","inside receiver");
//         If SCREEN_OFF event detected, start lockscreen if enabled
//         As new task and single top, with no animation
        if (lockscreen && intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent intent1 = new Intent(context, LockScreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            Log.d("Service","debugging");
            context.startActivity(intent1);
        }
        else if (lockscreen && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, LockScreenService.class));

            Intent intent1 = new Intent(context, LockScreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);

            context.startActivity(intent1);
        }
	}

}
