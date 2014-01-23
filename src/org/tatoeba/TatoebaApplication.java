package org.tatoeba;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.preference.PreferenceManager;

@ReportsCrashes(formKey = "dEM5X25zSlNWbk9WZ3RyZU90NU9pSWc6MA", mode = ReportingInteractionMode.NOTIFICATION, resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resNotifTickerText = R.string.crash_notif_ticker_text, resNotifTitle = R.string.crash_notif_title, resNotifText = R.string.crash_notif_text, resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, // optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class TatoebaApplication extends Application {
	@Override
	public void onCreate()
	{
		super.onCreate();
		ACRA.init(this); // The following line triggers the initialization of ACRA
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
	
	//public static final String API_SERVER = "http://ursamajor.informatik.uni-augsburg.de:2235/";
	public static final String API_SERVER = "http://137.250.169.80:4000/";

}