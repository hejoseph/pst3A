package com.abh.nfcunlocker;
import android.app.Activity;
import android.os.Bundle;


// Ninja 'Home default launcher' when lockscreen is active
public class LockHome extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
        overridePendingTransition(0, 0);
    }
}
