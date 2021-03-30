package net.sokato.NewsCheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import net.sokato.NewsCheck.Fragments.NewsFragment;
import net.sokato.NewsCheck.api.ApiClient;
import net.sokato.NewsCheck.api.ApiInterface;
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public String API_KEY;
    private int page = 1;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Articles> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout drawer;

    private NewsFragment newsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        API_KEY = getResources().getString(R.string.API_KEY);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolBar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null) {  //If this is the first loading of the activity
            newsFragment = new NewsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newsFragment).commit();
            navigationView.setCheckedItem(R.id.nav_news);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadNews();
    }

    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_news:
                newsFragment = new NewsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newsFragment).commit();
                loadNews();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadNews(){
        recyclerView = newsFragment.getRecyclerView();
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