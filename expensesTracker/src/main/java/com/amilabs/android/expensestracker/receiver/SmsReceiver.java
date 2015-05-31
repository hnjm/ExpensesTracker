package com.amilabs.android.expensestracker.receiver;

import com.amilabs.android.expensestracker.utils.SharedPref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent recieved: " + intent.getAction());
        if (SharedPref.isSmsEnabled(context) && intent != null && SMS_RECEIVED.equals(intent.getAction())) {
            SmsMessage smsMessage = extractSmsMessage(intent);
            processMessage(context, smsMessage);
        }
    }

    private SmsMessage extractSmsMessage(Intent intent) {
        Bundle pdusBundle = intent.getExtras();
        Object[] pdus = (Object[]) pdusBundle.get("pdus");
        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
        return smsMessage;
    }

    private void processMessage(Context context, SmsMessage smsMessage) {
        Log.d(TAG, "smsMessage="+smsMessage.getMessageBody()+" number="+smsMessage.getOriginatingAddress());
        if (smsMessage.getOriginatingAddress().equals(SharedPref.getPhoneNumber(context))) {
            //Intent i = new Intent();
            //i.setClass(context, ExpenseDialogActivity.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(i);
        }
    }

}