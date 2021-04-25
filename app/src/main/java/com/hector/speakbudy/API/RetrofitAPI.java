package com.hector.speakbudy.API;


import com.hector.speakbudy.DataModels.ResponseDataModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitAPI {
    @Multipart
    @POST("api")
    Call<ResponseDataModel> uploadVideo(
            @Part MultipartBody.Part video,
            @Part("description") RequestBody requestBody
    );
}
