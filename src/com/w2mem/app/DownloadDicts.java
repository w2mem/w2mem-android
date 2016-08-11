package com.w2mem.app;

import java.io.IOException;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.w2mem.app.system.Connector;
import com.w2mem.app.system.Dictionary;
import com.w2mem.app.system.Toaster;

public class DownloadDicts extends ListActivity {
	
	/* USER INTERFACE */
	private ListView uiListAvailableDicts;
	
	/* DATA */
	private List<Dictionary> availableDicts = null;

	/* CONSTANTS */
	static final int PROGRESS_DLG_ID = 666;
	static final int ALERT_DLG_ID = 667;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v2_download_dicts);
		
		((Button) findViewById(R.id.btnDownload)).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						SparseBooleanArray checked = uiListAvailableDicts.getCheckedItemPositions();
						if (checked.size() != 0) {
							new DownloadDictTask().execute();
						} else {
							Toaster.EmptyDict();
						}
					}
				}	
		);
		
		// interface binding
		uiListAvailableDicts = getListView();
		uiListAvailableDicts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// creates progress bar
		ProgressDialog progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// TODO use resources instead of hard-coded string
		progress.setMessage("Loading");
		progress.setIndeterminate(true);
		progress.setCancelable(false);
		progress.show();
		
		String password = getIntent().getExtras().getString("password");
		String login    = getIntent().getExtras().getString("login");  
		
		// establishes connection
		try {
			Connector.connect(login, password);
		} catch (IOException e) {
			Toaster.Error();
		}
		
		// gets list of available for download dicts
		availableDicts = Connector.getListOfDicts();
		if (availableDicts != null) {
			ArrayAdapter<Dictionary> adapter = new ArrayAdapter<Dictionary>(
					DownloadDicts.this,
					android.R.layout.simple_list_item_multiple_choice,
					availableDicts
			);
			setListAdapter(adapter);
		} else {
			Toaster.Error();
		}
		
		progress.hide();
	}

	@Override
	protected Dialog onCreateDialog(int dialogId) {
		ProgressDialog progress = null;
		switch (dialogId) {
		case PROGRESS_DLG_ID:
			progress = new ProgressDialog(this);
			progress.setMessage(getResources().getString(R.string.titleWait));
			break;
			
		case ALERT_DLG_ID:
			final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
			dialog.setContentView(R.layout.dlg_try);
			dialog.setTitle(getResources().getString(R.string.tvSuccess));
			dialog.show();
			((Button) dialog.findViewById(R.id.btnTry))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							finish();
							startActivity(new Intent(getApplicationContext(),
									ChooseCategory.class));
						}
					});

			((Button) dialog.findViewById(R.id.btnLater))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
			break;
		}
		return progress;
	}

	class DownloadDictTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			publishProgress(new Void[] {});
			try {
				downloadDicts();
			} catch (IOException e) {}
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
			showDialog(ALERT_DLG_ID);
		}
		
		private void downloadDicts() throws IOException {
			SparseBooleanArray checked = uiListAvailableDicts.getCheckedItemPositions();
			for (int i = 0; i < availableDicts.size(); i++) {
				if (checked.get(i)) {
					Dictionary dict = (Dictionary) uiListAvailableDicts.getAdapter()
							.getItem(i);
					try {
						//successful
						boolean isSuccessful = Connector.downloadDict(dict.name, dict.id, 1);
						if (isSuccessful == false) {
							Toaster.Error();
						}
					} catch (Exception e) {
						Toaster.Error();
					}
				}
			}
		}
	}

}
