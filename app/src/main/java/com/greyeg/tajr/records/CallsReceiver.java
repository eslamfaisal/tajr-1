package com.greyeg.tajr.records;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.greyeg.tajr.activities.EmptyCallActivity;
import com.greyeg.tajr.activities.LoginActivity;
import com.greyeg.tajr.activities.OrderActivity;
import com.greyeg.tajr.helper.CurrentCallListener;
import com.greyeg.tajr.helper.SharedHelper;
import com.greyeg.tajr.models.SimpleOrderResponse;
import com.greyeg.tajr.over.MissedCallNoOrderService;
import com.greyeg.tajr.over.MissedCallOrderService;
import com.greyeg.tajr.server.Api;
import com.greyeg.tajr.server.BaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by VS00481543 on 25-10-2017.
 */

public class CallsReceiver extends BroadcastReceiver {

    static final String TAG = "State";
    static final String TAG1 = " Inside State";
    static Boolean recordStarted;
    public static String phoneNumber;
    public static String name;

    private static String savedNumber;

    public static CurrentCallListener currentCallListener;
    public static boolean inOrderActivity = false;
    public static boolean inCall = false;

    public static void setCurrentCallListener(CurrentCallListener listener) {
        currentCallListener = listener;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        Toast.makeText(context, "تم بدء مكالمة", Toast.LENGTH_SHORT).show();
        Boolean switchCheckOn = pref.getBoolean("switchOn", true);
        if (switchCheckOn) {

            try {
                if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                    savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                    final Intent intent2 = new Intent(context, EmptyCallActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            context.startActivity(intent2);
                        }
                    }, 100);

                } else {
  }

                System.out.println("Receiver Start");

//            boolean callWait=pref.getBoolean("recordStarted",false);
                Bundle extras = intent.getExtras();
                String state = extras.getString(TelephonyManager.EXTRA_STATE);
                Log.d(TAG, " onReceive: " + state);
                //   Toast.makeText(context, "Call detected(Incoming/Outgoing) " + state, Toast.LENGTH_SHORT).show();
                final Intent intent2 = new Intent(context, EmptyCallActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        context.startActivity(intent2);
                    }
                }, 1000);

                if (extras != null) {
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        Toast.makeText(context, "incoming call  "+incomingNumber, Toast.LENGTH_SHORT).show();
                        Api api = BaseClient.getBaseClient().create(Api.class);
                        api.getPhoneData(
                                SharedHelper.getKey(context, LoginActivity.TOKEN),
                                SharedHelper.getKey(context, LoginActivity.USER_ID),
                                incomingNumber
                        ).enqueue(new Callback<SimpleOrderResponse>() {
                            @Override
                            public void onResponse(Call<SimpleOrderResponse> call, Response<SimpleOrderResponse> response) {
                                if (response.body().getCode().equals("1401")){
                                    Intent startHoverIntent = new Intent(context, MissedCallNoOrderService.class);
                                    context.startService(startHoverIntent);
                                }else if (response.body().getCode().equals("1200")){
                                    OrderActivity.order  = response.body().getOrder();
                                    MissedCallOrderService.showFloatingMenu(context);
                                }
                            }

                            @Override
                            public void onFailure(Call<SimpleOrderResponse> call, Throwable t) {

                            }
                        });

                        Log.d(TAG, "incomingNumber: " + incomingNumber);
                        Log.d(TAG, "EXTRA_SUBSCRIPTION_ID: " + intent.getStringExtra("android.intent.extra.EXTRA_SUBSCRIPTION_ID"));

                        Log.d(TAG1, " Inside " + state);
                    /*int j=pref.getInt("numOfCalls",0);
                    pref.edit().putInt("numOfCalls",++j).apply();
                    Log.d(TAG, "onReceive: num of calls "+ pref.getInt("numOfCalls",0));*/
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)/*&& pref.getInt("numOfCalls",1)==1*/) {

                        int j = pref.getInt("numOfCalls", 0);
                        pref.edit().putInt("numOfCalls", ++j).apply();
                        Log.d(TAG, "onReceive: num of calls " + pref.getInt("numOfCalls", 0));

                        Log.d(TAG1, " recordStarted in offhook: " + recordStarted);
                        Log.d(TAG1, " Inside " + state);

                        phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                        Log.d(TAG1, " Phone Number in receiver " + phoneNumber);

                        if (pref.getInt("numOfCalls", 1) == 1) {
                            Intent reivToServ = new Intent(context, RecorderService.class);
                            reivToServ.putExtra("number", phoneNumber);
                            context.startService(reivToServ);
                            inCall = true;
                            //name=new CommonMethods().getContactName(phoneNumber,context);


                        }

                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        int k = pref.getInt("numOfCalls", 1);
                        pref.edit().putInt("numOfCalls", --k).apply();
                        int l = pref.getInt("numOfCalls", 0);
                        Log.d(TAG1, " Inside " + state);
                        recordStarted = pref.getBoolean("recordStarted", false);
                        Log.d(TAG1, " recordStarted in idle :" + recordStarted);
                        if (recordStarted && l == 0) {
                            Log.d(TAG1, " Inside to stop recorder " + state);

                            context.stopService(new Intent(context, RecorderService.class));
                            inCall = false;

                            pref.edit().putBoolean("recordStarted", false).apply();
                            int serialNumber = pref.getInt("serialNumData", 1);
//                            new DatabaseManager(context).addCallDetails(new CallDetails(serialNumber, phoneNumber, new CommonMethods().getTIme(), new CommonMethods().getDate(), "not_yet"));

//                            List<CallDetails> list = new DatabaseManager(context).getAllDetails();
//                            for (CallDetails cd : list) {
//                                String log = "Serial Number : " + cd.getSerial() + " | Phone num : " + cd.getNum() + " | Time : " + cd.getTime1() + " | Date : " + cd.getDate1();
//                                Log.d("Database ", log);
//                            }

                            //recordStarted=true;
                            pref.edit().putInt("serialNumData", ++serialNumber).apply();
                            pref.edit().putBoolean("recordStarted", true).apply();


                            if (currentCallListener != null) {
                                currentCallListener.callEnded(serialNumber,phoneNumber);
                            }

                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}