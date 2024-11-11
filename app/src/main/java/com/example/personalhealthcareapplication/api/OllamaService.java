package com.example.personalhealthcareapplication.api;

import com.example.personalhealthcareapplication.model.SummaryRequest;
import com.example.personalhealthcareapplication.model.SummaryResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OllamaService {
    @Headers("Content-Type: application/json")
    @POST("api/generate")
    Call<ResponseBody> generateSummary(@Body SummaryRequest request);
}
