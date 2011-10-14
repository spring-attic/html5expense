package org.springsource.html5expense;

import android.os.Bundle;

import com.phonegap.DroidGap;

public class Html5expense extends DroidGap {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
    }
}