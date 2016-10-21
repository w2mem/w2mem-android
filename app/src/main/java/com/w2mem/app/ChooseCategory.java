package com.w2mem.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.w2mem.app.constants.DictType;

@SuppressWarnings("unused")
public class ChooseCategory extends BaseActivity implements
		View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v1_choose_category);

		((Button) findViewById(R.id.btnStartOwnDict)).setOnClickListener(this);
		((Button) findViewById(R.id.btnStartDefaultDict)).setOnClickListener(this);

		/*
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int ht = displaymetrics.heightPixels;
	
		((Button) findViewById(R.id.btnStartOwnDict)).setHeight((int)ht/4-10);
		((Button) findViewById(R.id.btnStartDefaultDict)).setHeight((int)ht/4-10);
		//((Button) findViewById(R.id.btnStartDefaultDict)).setHeight((int)ht/4-10);
		//((Button) findViewById(R.id.btnStartOwnDict)).setHeight((int)ht/4-10);
	    */
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ChooseDict.class);
		switch (v.getId()) {
		case R.id.btnStartOwnDict:
			intent.putExtra(DictType.DICT_TYPE_EXTRA, DictType.USER_DICT);
			startActivity(intent);
			break;
		case R.id.btnStartDefaultDict:
			intent.putExtra(DictType.DICT_TYPE_EXTRA, DictType.DEFAULT_DICT);
			startActivity(intent);
			break;
		}	
	}
}
