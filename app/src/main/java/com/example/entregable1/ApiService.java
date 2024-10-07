package com.example.entregable1;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("products") // Asegúrate de que esta ruta sea la correcta
    Call<List<Product>> getProducts();
}
