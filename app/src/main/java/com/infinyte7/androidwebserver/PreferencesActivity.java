package com.infinyte7.androidwebserver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.infinyte7.androidwebserver.Constants.*;

public class PreferencesActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences defSharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);

        Preference prefDocumentRoot = findPreference(getString(R.string.pk_document_root));
        prefDocumentRoot.setSummary(defSharedPref.getString(getString(R.string.pk_document_root), ""));

        Preference prefPort = findPreference(getString(R.string.pk_port));
        prefPort.setSummary(defSharedPref.getString(getString(R.string.pk_port), "8080"));

        Intent incomingIntent = getIntent();
        Bundle incomingExtras = incomingIntent.getExtras();
        if (incomingExtras != null) {
            int incomingIndex = incomingExtras.getInt("item");
            if (incomingIndex >= 0 && incomingIndex <= 2 ) {
                PreferenceScreen screen = getPreferenceScreen();
                screen.onItemClick(null, null, incomingIndex , 0);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        SharedPreferences.Editor prefEdit = sharedPreferences.edit();

        String pref_allow_cors = getString(R.string.pk_allow_cors);
        if(pref_allow_cors.equals(key)) {
            Boolean defaultValue = sharedPreferences.getBoolean(pref_allow_cors,true);
            String allowCors = "";
            if(defaultValue) {
                //allowCors = "in HTTP header allowed";
            } else {
                //allowCors = "in HTTP header disallowed";
            }
            prefEdit.putBoolean(getString(R.string.pk_pref_changed), true).commit();
            //pref.setSummary(allowCors);
        }

        String pref_config_loopback_ip = getString(R.string.pk_config_loopback_ip);
        if(pref_config_loopback_ip.equals(key)) {
            Boolean isLoopback = sharedPreferences.getBoolean(pref_config_loopback_ip,true);
            String loopbackConfigStr = "";
            if(isLoopback) {
                //loopbackConfigStr = "in HTTP header allowed";
                //Log.i("Loopback::","Enabled");
            } else {
                //loopbackConfigStr = "in HTTP header disallowed";
            }
            prefEdit.putBoolean(getString(R.string.pk_pref_changed), true).commit();
            //pref.setSummary(loopbackConfigStr);
        }

        String pref_document_root = getString(R.string.pk_document_root);
        if (pref_document_root.equals(key)) {
            String defaultDocumentRoot = MainActivity.getFilesDir(this).getPath() + "/html/";
            String documentRoot = sharedPreferences.getString(pref_document_root, defaultDocumentRoot);
            int docRootLength = documentRoot.length();
            if (! new File(documentRoot).canRead() || docRootLength == 0){
                documentRoot = defaultDocumentRoot;
                docRootLength = documentRoot.length();
                Toast.makeText(PreferencesActivity.this,
                        "Document root doesn't exists. Set to default.",
                        Toast.LENGTH_LONG
                ).show();
                Log.w("SAWS", "Document root doesn't exists. Set to default.");
                prefEdit.putString(getString(R.string.pk_document_root), defaultDocumentRoot).commit();
            } else if (documentRoot.charAt(docRootLength - 1) != '/') {
                // existing directory readable with and without trailing slash
                // but slash need for correct filename forming
                documentRoot = documentRoot + "/";
                prefEdit.putString(getString(R.string.pk_document_root), documentRoot).commit();
            }
            prefEdit.putBoolean(getString(R.string.pk_pref_changed), true).commit();
            pref.setSummary(documentRoot);
        }

        String pref_port = getString(R.string.pk_port);
        if (pref_port.equals(key)) {
            Integer port;
            String portAsString = sharedPreferences.getString(pref_port,"8080");
            try {
                port = Integer.valueOf(portAsString);
            } catch (NumberFormatException e) {
                port = 8080;
                Log.w(Constants.LOG_TAG, "Port preferences may be empty");
            }
            if (port < 1024 || port > 65535 || portAsString.length() == 0) {
                port = 8080;
                portAsString = Integer.toString(port);
                Toast.makeText(PreferencesActivity.this,
                        "Port less then 1024 or grate then 65535. Set to default.",
                        Toast.LENGTH_LONG
                ).show();
                Log.w("SAWS", "Port less then 1024 or grate then 65535. Set to default.");
                prefEdit.putString(getString(R.string.pk_port), portAsString).commit();
            }
            prefEdit.putBoolean(getString(R.string.pk_pref_changed), true).commit();
            pref.setSummary(portAsString);
        }
    }
}
