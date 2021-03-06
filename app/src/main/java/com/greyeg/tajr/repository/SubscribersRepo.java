package com.greyeg.tajr.repository;

import com.greyeg.tajr.models.SubscriberInfo;
import com.greyeg.tajr.server.BaseClient;
import io.reactivex.Single;
import retrofit2.Response;

public class SubscribersRepo {
    
    private static SubscribersRepo subscribersRepo;

    public static SubscribersRepo getInstance() {
        return subscribersRepo==null?subscribersRepo=new SubscribersRepo():subscribersRepo;
    }
    
    public Single<Response<SubscriberInfo>> getSubscriberInfo( String token, String sender_name){
         return BaseClient
                 .getApiService()
                 .getSubscriberInfo(token,sender_name);
    }
    
}
