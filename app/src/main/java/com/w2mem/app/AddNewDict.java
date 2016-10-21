
package com.w2mem.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.w2mem.app.constants.DictType;
import com.w2mem.app.data.DatabaseHelper;
import com.w2mem.app.system.Toaster;

public class AddNewDict extends BaseActivity {
	/* USER INTERFACE */
	private EditText uiEditDictName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v1_add_dict_name);
		
		int dictTypeParam = getIntent().getExtras().getInt(DictType.DICT_TYPE_EXTRA);
		//TODO change it, for the love of god, change it as soon as possible
		if (dictTypeParam == 3) {
			dictTypeParam = 2;
		}
		final int dictType = dictTypeParam;

		uiEditDictName = (EditText) findViewById(R.id.etName);

		((Button) findViewById(R.id.btnNext))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						
						String dictName = uiEditDictName.getText().toString();
						boolean fieldIsNotEmpty = (dictName.matches("") == false);
						if (fieldIsNotEmpty) {
							DatabaseHelper.open();
							long dictId = DatabaseHelper.addDict(dictName, dictType, 0);
							DatabaseHelper.close();
							// switch view
							Intent intent = new Intent();
							intent.setClass(getApplicationContext(), AddWordPairs.class);
							intent.putExtra(AddWordPairs.PARENT_DICT, dictId);
							startActivity(intent);
							finish();
						} else {
							Toaster.UnfilledFields();
						}
					}
				});
	}

	@Override
	protected void onPause() {
		DatabaseHelper.close();
		super.onPause();
	}
}
