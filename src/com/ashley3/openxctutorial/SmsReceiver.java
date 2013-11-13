package com.ashley3.openxctutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle; 
import android.telephony.SmsMessage;
import android.util.Log;


public class SmsReceiver extends BroadcastReceiver {
	public static final String TAG = SmsReceiver.class.getCanonicalName();

	public void onReceive(Context context, Intent intent) {
		Log.e(TAG,"received a text");
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;

		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			} 
			for (SmsMessage msg : msgs) {
				String strFrom = msg.getDisplayOriginatingAddress();
				String strMsg = msg.getDisplayMessageBody();
				Log.v(TAG,"from: "+strFrom+";  message: "+strMsg);
				MainActivity.getInstance().speakWords("received a text from: "+strFrom+" that says "+strMsg);
			} 
		}
    }
}