package com.example.filmbilgileriapiuygulamasi;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://www.omdbapi.com/";
    private static final String API_KEY = "b167925c";
    private SearchView searchView;
    private ListView listView;
    private ArrayList<String> filmVerileri;
    private ArrayAdapter<String> adapter;
    private ImageView filmPosterImageView;
    private List<String> filmPosterleri;
    private int currentIndex = 0;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.aramaKutusu);
        listView = findViewById(R.id.listele);
        filmPosterImageView = findViewById(R.id.filmPoster);

        filmVerileri = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filmVerileri);
        listView.setAdapter(adapter);

        filmPosterleri = new ArrayList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String movieTitle = query;
                apiTalep(API_URL + "?s=" + movieTitle + "&apikey=" + API_KEY);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Hoşgeldiniz mesajını ortada göstermek için işlemleri yapalım
        TextView hoşgeldinizMesajı = findViewById(R.id.hoşgeldinizMesajı);
        hoşgeldinizMesajı.setVisibility(View.INVISIBLE); // İlk başta görünmez yapalım

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hoşgeldinizMesajı.setVisibility(View.VISIBLE); // Mesajı görünür yap
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hoşgeldinizMesajı.setVisibility(View.INVISIBLE); // 2 saniye sonra tekrar görünmez yap
                    }
                }, 2000); // 2 saniye sonra çalıştır
            }
        }, 2000); // 2 saniye sonra çalıştır
    }


    private void degistirHintRenk(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        if (searchPlateId != 0) {
            View searchPlate = searchView.findViewById(searchPlateId);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            if (searchTextId != 0) {
                EditText searchText = searchPlate.findViewById(searchTextId);
                searchText.setHintTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    private void apiTalep(String url) {
        RequestQueue talepSirasi = Volley.newRequestQueue(this);
        StringRequest talep = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("test", "Tüm cevap: " + response);
                jsonAyikla(response);
                baslatTimer();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("test", "Hata mesajı: " + error.toString());
            }
        });
        talepSirasi.add(talep);
    }

    private void jsonAyikla(String jsonCevap) {
        try {
            JSONObject jsonObject = new JSONObject(jsonCevap);

            if (jsonObject.has("Search")) {
                JSONArray filmArray = jsonObject.getJSONArray("Search");

                filmVerileri.clear();
                filmPosterleri.clear();
                currentIndex = 0;

                for (int i = 0; i < filmArray.length(); i++) {
                    JSONObject filmObject = filmArray.getJSONObject(i);

                    String filmAdi = filmObject.getString("Title");
                    String filmYili = filmObject.getString("Year");
                    String filmTur = filmObject.getString("Type");
                    String filmPoster = filmObject.getString("Poster");

                    String filmBilgileri = "Film Adı: " + filmAdi + "\nYıl: " + filmYili + "\nTür: " + filmTur;
                    filmVerileri.add(filmBilgileri);
                    filmPosterleri.add(filmPoster);
                }

                adapter.notifyDataSetChanged();
            } else {
                Log.v("test", "Film bulunamadı.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void baslatTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentIndex >= filmPosterleri.size()) {
                    currentIndex = 0;
                }

                final String resimUrl = filmPosterleri.get(currentIndex);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        yukleVeGosterResim(resimUrl);
                    }
                });

                currentIndex++;
            }
        }, 0, 2000); // 2 saniyede bir resmi değiştir
    }

    private void yukleVeGosterResim(String resimUrl) {
        Picasso.get().load(resimUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Resim başarıyla yüklendiğinde burası çalışır
                // Bitmap'i ImageView'e set et
                filmPosterImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Resim yüklenirken hata oluştuğunda burası çalışır
                Log.v("test", "Resim yüklenemedi: " + resimUrl);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Resim yüklenmeden önce burası çalışır (isteğe bağlı)
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
