package net.sokato.NewsCheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import net.sokato.NewsCheck.api.ApiClient;
import net.sokato.NewsCheck.api.ApiInterface;
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public String API_KEY;
    private int page = 1;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Articles> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        API_KEY = getResources().getString(R.string.API_KEY);

        recyclerView = findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    loadJson();
                }
            }
        });

        adapter = new Adapter(articles, MainActivity.this);
        loadJson();
        recyclerView.setAdapter(adapter); //The first time, we set the adapter
        adapter.notifyDataSetChanged();   //Later on, only the data set will be updated

    }

    public void loadJson(){
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<News> call;
        call = apiInterface.getNews(Utils.getCountry(), API_KEY, page);

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles() != null){
                    adapter.addItems(response.body().getArticles());
                    adapter.notifyDataSetChanged();
                    page++;
                }else{
                    Toast.makeText(MainActivity.this, R.string.noResult, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

            }
        });
    }

}