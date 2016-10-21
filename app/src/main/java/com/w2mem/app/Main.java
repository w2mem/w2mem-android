package com.w2mem.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.w2mem.app.data.DatabaseHelper;
import com.w2mem.app.data.SettingsHelper;
import com.w2mem.app.system.Connector;
import com.w2mem.app.system.Toaster;

import java.io.IOException;

public class Main extends BaseActivity implements View.OnClickListener {
    /* USER INTERFACE */
    private Button uiButtonStartOnline;
    private Button uiButtonStartOffline;
    private Button uiButtonNoProfile;

    /* CONSTANTS */
    static final int PROGRESS_DLG_ID = 666;
    // first digit is major release version, last two digits -- minor
    // e.g. 110 stands for 1.1
    static final int VERSION = 110;

    /* initializes view on load */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ver);

        Toaster.initialize(getApplicationContext());
        DatabaseHelper.initialize(getApplicationContext());

        buttonInitialization();
        loadPreferences();
    }

    /* initializes horizontal view */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.front);

        buttonInitialization();
        //loadPreferences();
    }

    /* USER INTERFACE */
    /* binds and initializes main menu buttons */
    public void buttonInitialization() {
        uiButtonStartOnline = (Button) findViewById(R.id.btnStartOnline);   // "Start learn!"
        uiButtonStartOffline = (Button) findViewById(R.id.btnStartOffline);  // "Download dictionaries"
        uiButtonNoProfile = (Button) findViewById(R.id.btnNoProfile);     // "Don't have a profile?"

        // Button font set up
        Typeface typeArial;
        typeArial = Typeface.create("arial", Typeface.BOLD);
        uiButtonStartOffline.setTypeface(typeArial);
        uiButtonStartOnline.setTypeface(typeArial);
        uiButtonNoProfile.setTypeface(typeArial);

        // Button listeners set up
        uiButtonStartOffline.setOnClickListener(this);
        uiButtonStartOnline.setOnClickListener(this);
        uiButtonNoProfile.setOnClickListener(this);
    }

    /* Click handler */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btnStartOffline:
                intent.setClass(getApplicationContext(), ChooseCategory.class);
                startActivity(intent);
                break;
            case R.id.btnStartOnline:
                showLoginDialog();
                break;
            case R.id.btnNoProfile:
                intent.setClass(getApplicationContext(), NoProfile.class);
                startActivity(intent);
                break;
        }
    }

    /* OTHER */
    /*
     * Loads version info to check for new available dictionaries
	 * and user login info if it has been saved before
	 */
    public void loadPreferences() {
        SharedPreferences settings = getSharedPreferences(
            SettingsHelper.PREFS_NAME, MODE_PRIVATE);
        SettingsHelper.initialize(settings);
        /* Version control */
        /* if no information on version exists - loads dictionaries */
        if (SettingsHelper.getAppVersion() == 0) {
            new SaveDictTask().execute();
            SettingsHelper.updateAppVersion(VERSION);
        } else if (SettingsHelper.getAppVersion() < VERSION) {
            DatabaseHelper.removeAllStandartDicts();
            new SaveDictTask().execute();
            SettingsHelper.updateAppVersion(VERSION);
        }
    }

    /* DIALOGS */
    /* Login dialog */
    private void showLoginDialog() {
        final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
        dialog.setContentView(R.layout.dlg_login);
        dialog.setTitle(getResources().getString(R.string.dialogTitleAuthorization));
        dialog.show();

        // interface binding
        Button btnSignIn = (Button) dialog.findViewById(R.id.btnAuthorize);
        final EditText etLogin = (EditText) dialog.findViewById(R.id.etLogin);
        final EditText etPassword = (EditText) dialog.findViewById(R.id.etPassword);
        final CheckBox cbSaveUserInfo = (CheckBox) dialog.findViewById(R.id.cbRememberMe);

        // user login is being saved automatically regardless of checkbox state
        etLogin.setText(SettingsHelper.getLogin());
        // if both pass and login are correct - auto fill user data
        if (SettingsHelper.getSaveUserInfo()) {
            etPassword.setText(SettingsHelper.getPassword());
            cbSaveUserInfo.setChecked(true);
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String login = etLogin.getText().toString();
                String password = etPassword.getText().toString();
                login(dialog, login, password, cbSaveUserInfo.isChecked());
            }
        });
    }

    //TODO move to LoginHelper
    public void login(Dialog dialog, final String login, final String password, final boolean saveUserInfo) {
        if (password.matches("") || login.matches("")) {
            Toaster.UnfilledFields();
            return;
        }
        if (isOnline()) {
            dialog.dismiss();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        return Connector.connect(login, password);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean loginSuccessful) {
                    super.onPostExecute(loginSuccessful);
                    if (loginSuccessful) {
                        // user login is being saved automatically regardless of checkbox state
                        SettingsHelper.saveLogin(login);
                        if (saveUserInfo) {
                            // if checkbox is checked - saves user's password
                            SettingsHelper.savePassword(login, password);
                        } else {
                            // otherwise - clear previously saved password
                            SettingsHelper.clearUserInfo();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("password", password);
                        intent.putExtra("login", login);
                        intent.setClass(getApplicationContext(), DownloadDicts.class);
                        startActivity(intent);
                    } else {
                        switch (Connector.getErrorCode()) {
                            case Connector.ERROR_BAD_INFO:
                                Toaster.BadUserInfo();
                                break;
                            case Connector.ERROR_NO_DICTS:
                                Toaster.NoDictsToDownload();
                                break;
                        }
                    }
                }
            }.execute();

        } else {
            // no Internet access
            Toaster.NoInternet();
        }
    }

    /*
     * Checks network status,
     * return true if network available,
     * return false if network not available
     */
    private boolean isOnline() {
        ConnectivityManager connectMgr;
        connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectMgr.getActiveNetworkInfo() != null &&
            connectMgr.getActiveNetworkInfo().isAvailable() &&
            connectMgr.getActiveNetworkInfo().isConnected());
    }

    /* Welcome dialog */
    @Override
    protected Dialog onCreateDialog(int dialogId) {
        ProgressDialog progress = null;
        switch (dialogId) {
            case PROGRESS_DLG_ID:
                // displays spinning progress dialog while app loading dictionaries
                progress = new ProgressDialog(this);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage(
                    getResources().getString(R.string.titleWaitSaving));
                progress.setIndeterminate(true);
                progress.setCancelable(false);
                break;
        }
        return progress;
    }

    /* Welcome dialog */
    private void showDeleteDialog() {
        final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
        dialog.setContentView(R.layout.dlg_welcome);
        dialog.setTitle(getResources().getString(R.string.dialogTitleWelcome));
        dialog.show();

        ((Button) dialog.findViewById(R.id.btnIUnderstand)).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
    }

    class SaveDictTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(new Void[]{});
            saveAllOriginalDicts();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            showDialog(PROGRESS_DLG_ID);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dismissDialog(PROGRESS_DLG_ID);
            showDeleteDialog();
        }
    }

    /* Original dicts saving */
    private void saveAllOriginalDicts() {
        try {
            DatabaseHelper.loadDefaultDicts();
        } catch (IOException e) {
        }
    }
}
