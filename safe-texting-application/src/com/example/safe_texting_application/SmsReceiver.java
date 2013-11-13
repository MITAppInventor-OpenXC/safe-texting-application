package com.example.safe_texting_application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SmsReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
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
				MainActivity.getInstance().speakWords(strMsg);
			}
		}
    }
}