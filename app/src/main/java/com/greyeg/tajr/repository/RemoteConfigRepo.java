package com.greyeg.tajr.repository;

import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.greyeg.tajr.App;
import com.greyeg.tajr.BuildConfig;
import com.greyeg.tajr.R;

public class RemoteConfigRepo {

    private static RemoteConfigRepo remoteConfigRepo;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private MutableLiveData<Boolean> isLatestVersion;
    private MutableLiveData<String> error=new MutableLiveData<>();

    public static RemoteConfigRepo getInstance() {
        return remoteConfigRepo == null ? remoteConfigRepo = new RemoteConfigRepo() : remoteConfigRepo;
    }

    private RemoteConfigRepo() {

    }

    public MutableLiveData<Boolean> getRemoteConfigValue() {
        isLatestVersion=new MutableLiveData<>();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig
                .fetchAndActivate()
                .addOnCompleteListener( new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()){
                            String latestAppVersion=mFirebaseRemoteConfig.getString("latest_app_version");
                            try {
                                latestAppVersion=latestAppVersion.replace(".","");
                                int latestVersion=Integer.valueOf(latestAppVersion);
                                int currentVersion=Integer.valueOf(BuildConfig.VERSION_NAME.replace(".",""));

                                if (currentVersion<latestVersion){
                                        isLatestVersion.setValue(false);
                                    }else {
                                        isLatestVersion.setValue(true);
                                }

                            }catch (Exception e){
                                error.setValue(Resources.getSystem().getString(R.string.server_error));

                                Log.d("REMOTEECONFIGG","exception "+ e.getMessage());
                            }
                        }else {

                            error.setValue(Resources.getSystem().getString(R.string.server_error));
                        }

                    }
                });

        return isLatestVersion;
    }


    public MutableLiveData<String> getError() {
        return error;
    }
}