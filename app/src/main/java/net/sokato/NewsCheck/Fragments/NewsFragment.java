    package net.sokato.NewsCheck.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.sokato.NewsCheck.Adapter;
import net.sokato.NewsCheck.MainActivity;
import net.sokato.NewsCheck.R;
import net.sokato.NewsCheck.Utils;
import net.sokato.NewsCheck.api.ApiClient;
import net.sokato.NewsCheck.api.ApiInterface;
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**This is the main fragment, it displays the most recent popular news, fetched from NewsAPI**/

public class NewsFragment extends Fragment {

    public String API_KEY;
    private int page = 1;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Articles> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        API_KEY = getResources().getString(R.string.API_KEY);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        //We use this listener to see if the user has reached the end of the articles
        //If he has, we try to load some more
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    loadJson();
                }
            }
        });

        //We use this adapter to display the articles
        adapter = new Adapter(articles, getActivity());
        loadJson();
        recyclerView.setAdapter(adapter); //The first time, we set the adapter
        adapter.notifyDataSetChanged();   //Later on, only the data set will be updated
    }

    //This function fetches the articles from NewsAPI and sends them to the adapter
    //It gets the data from NewsAPI as a json data, that is unpacked and sent
    //to the adapter for display
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
                    page++;  //The page variable is used not load the same articles each time
                }else{
                    Toast.makeText(getActivity(), R.string.noResult, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Toast.makeText(getActivity(), R.string.articlesLoadingFailure, Toast.LENGTH_LONG).show();
            }
        });
    }
}
