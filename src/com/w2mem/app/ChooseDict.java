package com.w2mem.app;

import java.util.List;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.w2mem.app.constants.DictType;
import com.w2mem.app.data.DatabaseHelper;
import com.w2mem.app.system.Dictionary;
import com.w2mem.app.system.Toaster;

public class ChooseDict extends ListActivity implements
		View.OnClickListener {
	
	/* USER INTERFACE */
	private ListView uiListDicts;
	
	/* DATA */
	private List<Dictionary> dicts;
	private int dictsType = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v1_choose_dict);
		
		dictsType = getIntent().getExtras().getInt(DictType.DICT_TYPE_EXTRA);

		uiListDicts = getListView();
		uiListDicts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// adds editing hint for user's dicts
		if (dictsType != DictType.DEFAULT_DICT) {
			View v = getLayoutInflater().inflate(R.layout.v1_hint, null);
			uiListDicts.addHeaderView(v,null,false);
		}
		setRequestEnv(dictsType);

		((Button) findViewById(R.id.btnAddTopic)).setOnClickListener(this);
		((Button) findViewById(R.id.btnStartTopic)).setOnClickListener(this);
	}

	private void setRequestEnv(int param) {
		switch (param) {
		case DictType.DEFAULT_DICT:
			((Button) findViewById(R.id.btnAddTopic))
					.setVisibility(View.INVISIBLE);
			Display display = getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			LayoutParams params = ((Button) findViewById(R.id.btnStartTopic))
					.getLayoutParams();
			params.width = width;
			((Button) findViewById(R.id.btnStartTopic)).setLayoutParams(params);
			break;
			
		case DictType.USER_DICT:
			registerForContextMenu(uiListDicts);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnStartTopic) {
			int dictPosition = uiListDicts.getCheckedItemPosition();
			if (dictsType == DictType.USER_DICT) {
				// compensates header position shift
				dictPosition -= 1;
			}
			if (dictPosition > ListView.INVALID_POSITION) {
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						TrainDict.class);
				long id = dicts.get(dictPosition).getId();
				if (DatabaseHelper.countWordPairsInDict(id) == 0) {
					Toaster.EmptyDict();
				}
				intent.putExtra(TrainDict.DICT_ID, id);
				startActivity(intent);
			} else {
				Toaster.ChooseDict();
			}
		}

		if (v.getId() == R.id.btnAddTopic) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), AddNewDict.class);
			intent.putExtra(DictType.DICT_TYPE_EXTRA, dictsType);
			startActivity(intent);
		}
	}

	@Override
	protected void onPause() {
		DatabaseHelper.close();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		dicts = DatabaseHelper.getDicts(dictsType);
		ArrayAdapter<Dictionary> adapter = new ArrayAdapter<Dictionary>(this,
				android.R.layout.simple_list_item_single_choice,
				dicts);
		setListAdapter(adapter);

		super.onResume();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.choosetopiccontext, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int menuItemId = item.getItemId();
		final int currentSelectionItemPosition = 
				((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position - 1;
		
		switch (menuItemId) {
		case R.id.cxMenuRename:
			showRenameDialog(currentSelectionItemPosition);
			return true;
			
		case R.id.cxMenuDeleteDict:
			showDeleteDialog(currentSelectionItemPosition);
			return true;
			
		case R.id.cxMenuEdit:
			long dict = dicts.get(currentSelectionItemPosition).id;
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), AddWordPairs.class);
			intent.putExtra(AddWordPairs.PARENT_DICT, dict);
			startActivity(intent);
			return true;
			
		default:
			return false;
		}
	}

	private void showDeleteDialog(final int id) {
		final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
		dialog.setContentView(R.layout.dlg_delete);
		dialog.setTitle(getResources().getString(
				R.string.dialogTitleAcceptDelete));
		dialog.show();

		((Button) dialog.findViewById(R.id.btnAcceptDelete))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						@SuppressWarnings("unchecked")
						ArrayAdapter<Dictionary> adapter = (ArrayAdapter<Dictionary>) getListAdapter();
						DatabaseHelper.removeDict(adapter.getItem(id).id);
						adapter.remove(adapter.getItem(id));
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});

		((Button) dialog.findViewById(R.id.btnCancelDelete))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}

	private void showRenameDialog(final int id) {
		final Dialog dialog = new Dialog(this, R.style.W2memLoginDialog);
		dialog.setContentView(R.layout.dlg_edit_name);
		dialog.setTitle(getResources().getString(R.string.dialogTitleNewName));
		dialog.show();

		final EditText uiNewName = (EditText) dialog
				.findViewById(R.id.etDialogNewName);

		// dialog button handler
		Button okButton = (Button) dialog.findViewById(R.id.btnDialogAccept);

		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				@SuppressWarnings("unchecked")
				ArrayAdapter<Dictionary> adapter = (ArrayAdapter<Dictionary>) getListAdapter();
				// updates in database
				String newName = uiNewName.getText().toString();
				long dictId = adapter.getItem(id).id;
				DatabaseHelper.renameDict(newName, dictId);
				// updates in ListView
				dicts.get(id).name = newName;
				adapter.notifyDataSetChanged();
				
				dialog.dismiss();
			}
		});

		dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}
}
