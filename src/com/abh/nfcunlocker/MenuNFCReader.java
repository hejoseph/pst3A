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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MenuNFCReader extends Activity {

    private TextView tagRead, message;
    private NfcAdapter nfcAdapter;               /* Je définis le nfc adapter pour voir si un lecteur nfc est présent */
    private Button status;

    
    
	// Content of JSON file
	private JSONObject root;
	private JSONObject settings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        readFromJSON();
        
        setContentView(R.layout.activity_menu_nfcreader);

        tagRead = (TextView) findViewById(R.id.tagRead);
        message = (TextView) findViewById(R.id.commentary);
        status = (Button) findViewById(R.id.status);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);    /* Si le nfcAdapter n'existe pas (renvoie null)...*/

        if (nfcAdapter == null) {                           /* ...l'application ne sera pas utilisable */
            Toast.makeText(this, "Vous n'avez pas de lecteur NFC. Application non utilisable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /* Si le nfc n'est pas activé au démarrage de l'appli, on conseille de le faire en renvoyant vers les réglages */
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Veuillez activer la technologie NFC avant de commencer ! Voici l'écran de réglages", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }

//        status.setOnClickListener(new View.OnClickListener() {
//                 @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(MenuNFCReader.this, NFCWriter.class);
//                    startActivity(intent);
//                }
//        });
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

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
      //      tagRead.setText("OUI !");

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null && rawMsgs.length > 0) { /* Si on a pu lire un message */
            	
                NdefMessage ndefMessage = (NdefMessage) rawMsgs[0]; /* On le caste en NDEFMessage */
                NdefRecord[] records = ndefMessage.getRecords(); /* On obtient alors l'ensemble de ses records */
                
                if (records.length > 0) {
                    NdefRecord record = records[0]; /* On suppose que seul le premier record contient des données */
                    String msg = new String(record.getPayload()); /* On extrait alors son payload */
                    try {
						settings.put("tag", msg);
						writeToJSON();
						message.setText("Tag Id is registered ! ");
					} catch (JSONException e) {
						message.setText("Problem Tag, cannot be saved !  ");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
                }

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
    
}