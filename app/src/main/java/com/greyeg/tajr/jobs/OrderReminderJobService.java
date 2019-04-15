package com.greyeg.tajr.jobs;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.greyeg.tajr.R;
import com.greyeg.tajr.activities.LoginActivity;
import com.greyeg.tajr.activities.OrderActivity;
import com.greyeg.tajr.helper.SharedHelper;
import com.greyeg.tajr.models.SimpleOrderResponse;
import com.greyeg.tajr.server.Api;
import com.greyeg.tajr.server.BaseClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by aviator on 16/03/18.
 */

public class OrderReminderJobService extends JobService {

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        Api api = BaseClient.getBaseClient().create(Api.class);
        api.getFuckenOrders(SharedHelper.getKey(this, LoginActivity.TOKEN)).enqueue(new Callback<SimpleOrderResponse>() {
            @Override
            public void onResponse(Call<SimpleOrderResponse> call, Response<SimpleOrderResponse> response) {
                Log.d("OrderReminderJobService", "onResponse: " + response.message());
                if (response.body() != null) {
                    if (response.body().getOrder() != null) {
                        if (getAutoNotifiction())
                        createNotification(String.valueOf(response.body().getRemainig_orders()));
                        jobFinished(jobParameters, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<SimpleOrderResponse> call, Throwable t) {
                Log.d("OrderReminderJobService", "failer: " + t.getMessage());

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(5);
                jobFinished(jobParameters, false);
            }
        });


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }


    @SuppressLint("NewApi")
    public void createNotification(String first) {


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;

            channel = new NotificationChannel("5", "eslam", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("5");
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "5")
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentTitle("orders")
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .addAction(R.drawable.ic_call_end_red, getResources().getString(R.string.start_work),
                            PendingIntent.getActivity(this, 0, new Intent(this,
                                    OrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0))
                    .setContentText(getString(R.string.remaining) + " " + first + " " + getString(R.string.order))
                    .setSmallIcon(R.drawable.ic_launcher);


            notificationManager.notify(5, builder.build());

        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentTitle("orders")
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .addAction(R.drawable.ic_call_end_red, getResources().getString(R.string.start_work),
                            PendingIntent.getActivity(this, 0, new Intent(this,
                                    OrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0))
                    .setContentText(getString(R.string.remaining) + " " + first + " " + getString(R.string.order))
                    .setSmallIcon(R.drawable.ic_launcher);


            notificationManager.notify(5, builder.build());
        }

    }

    private boolean getAutoNotifiction() {
        SharedPreferences auto = PreferenceManager.getDefaultSharedPreferences(this);
        return auto.getBoolean("autoNotifiction", false);
    }
}