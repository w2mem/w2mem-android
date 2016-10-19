package com.w2mem.app.system;

import com.w2mem.app.R;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
	
	private static Context context = null;
	
	/* 
	 * Initializes Toaster by providing application context.
	 * MUST BE CALLED BEFORE USING TOASTER CLASS FOR TOASTS.
	 */
	public static void initialize(Context context) {
		Toaster.context = context;
	}
	
	/* Displays general error message. */
	public static void Error() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnError),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message if Internet isn't available. */
	public static void NoInternet() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnNoInternet),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message for wrong user info. */ 
	public static void BadUserInfo() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnBadLoginPass),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message for unfilled text fields. */
	public static void UnfilledFields() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnNoFilledField),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message for empty downloadable dictionaries list. */
	public static void NoDictsToDownload() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnNoDicts),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message if chosen dict id empty. */
	public static void EmptyDict() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnEmptyTopic),
				Toast.LENGTH_LONG).show();
	}
	
	/* Displays error message when no dicts were selected for trainig. */
	public static void ChooseDict() {
		Toast.makeText(context,
				context.getResources().getString(R.string.wrnChooseDict),
				Toast.LENGTH_LONG).show();
	}
	
}
