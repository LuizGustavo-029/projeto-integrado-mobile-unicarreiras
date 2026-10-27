package com.example.estagiopro.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    Call<ApiResponse> login(@Field("email") String email, @Field("senha") String senha);

    @FormUrlEncoded
    @POST("cadastro.php")
    Call<ApiResponse> cadastro(@Field("nome") String nome,
                                @Field("email") String email,
                                @Field("senha") String senha,
                                @Field("curso") String curso,
                                @Field("instituicao_ensino") String instituicao);
}
