package com.w2mem.app;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.w2mem.app.data.DatabaseHelper;
import com.w2mem.app.system.Toaster;
import com.w2mem.app.system.WordPair;

public class AddWordPairs extends ListActivity {
	public static final String PARENT_DICT = "parent_dict";

	/* USER INTERFACE */
	private EditText uiEditWord;
	private EditText uiEditTranslate;
	private ListView uiListWordPairs;

	/* DATA */
	private long dictId = 0;
	private List<WordPair> wordPairsValues;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v1_add_topic);

		// interface binding
		uiEditWord = (EditText) findViewById(R.id.etWord);
		uiEditTranslate = (EditText) findViewById(R.id.etTranslate);
		uiListWordPairs = getListView();
		registerForContextMenu(uiListWordPairs);

		// gets dict id from intent
		dictId = getIntent().getExtras().getLong(PARENT_DICT);
		
		wordPairsValues = DatabaseHelper.getAllWordPairsFromDict(dictId);

		ArrayAdapter<WordPair> adapter = new ArrayAdapter<WordPair>(this,
				android.R.layout.simple_list_item_1, wordPairsValues);
		setListAdapter(adapter);

		((Button) findViewById(R.id.btnAddCouple)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						@SuppressWarnings("unchecked")
						ArrayAdapter<WordPair> adapter = (ArrayAdapter<WordPair>) getListAdapter();
						String word = uiEditWord.getText().toString();
						String translate = uiEditTranslate.getText().toString();
						if (word.matches("") == false && translate.matches("") == false) {
							WordPair cWord = DatabaseHelper.addAndReturnPair(word, translate, dictId);
							adapter.add(cWord);
							adapter.notifyDataSetChanged();
							uiEditWord.setText("");
							uiEditTranslate.setText("");
						}
					}
				});
	}

	@Override
	protected void onPause() {
		DatabaseHelper.close();
		super.onPause();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.addcouplescontext, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int menuItemId = item.getItemId();
		final int currentSelectionItemPosition = 
				((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
		
		switch (menuItemId) {
		case R.id.cxMenuDeleteDict:
			@SuppressWarnings("unchecked")
			ArrayAdapter<WordPair> adapter = (ArrayAdapter<WordPair>) getListAdapter();
			WordPair coupleWord = adapter
					.getItem(currentSelectionItemPosition);
			DatabaseHelper.removeWordPair(coupleWord.id);
			adapter.remove(coupleWord);
			adapter.notifyDataSetChanged();
			return true;

		case R.id.cxMenuEdit:
			showMyDialog(currentSelectionItemPosition);
			return true;
		default:
			return false;
		}
	}

	private void showMyDialog(final int id) {
		// show dialog
		final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
		dialog.setContentView(R.layout.dlg_edit_pair);
		dialog.setTitle(getResources().getString(R.string.dialogTitleNewWord));
		dialog.show();

		@SuppressWarnings("unchecked")
		final ArrayAdapter<WordPair> adapter = (ArrayAdapter<WordPair>) getListAdapter();
		final EditText NewWord = (EditText) dialog
				.findViewById(R.id.etDialogNewWord);
		final EditText NewTranslate = (EditText) dialog
				.findViewById(R.id.etDialogNewTranslate);
		NewWord.setText(adapter.getItem(id).word);
		NewTranslate.setText(adapter.getItem(id).translation);

		// dialog ok button click handler
		((Button) dialog.findViewById(R.id.btnDialogAccept)).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						final String newWord = NewWord.getText().toString();
						final String newTranslation = NewTranslate.getText().toString();
						// updates database
						if (newWord.matches("") == false && newTranslation.matches("") == false) {
							long _id = adapter.getItem(id).id;
							DatabaseHelper.changeWordPair(newWord, newTranslation, _id);
							// updates ListView
							wordPairsValues.get(id).word = newWord;
							wordPairsValues.get(id).translation = newTranslation;
							adapter.notifyDataSetChanged();
							dialog.dismiss();
						} else {
							Toaster.UnfilledFields();
						}
					}
				});

		// dialog cancel button click handler
		dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}
}
