package net.sokato.NewsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import net.sokato.NewsCheck.Fragments.ArticleFragment;
import net.sokato.NewsCheck.models.Articles;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private final List<Articles> articles;
    private final Context context;
    private OnItemClickListener onItemClickListener;

    DocumentReference docRef;
    DocumentSnapshot document;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Adapter(List<Articles> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Articles article = articles.get(position);
                ArticleFragment articleFragment = new ArticleFragment();

                Bundle bundle = new Bundle();
                bundle.putString("URL", article.getUrl());

                articleFragment.setArguments(bundle);

                MainActivity parent = (MainActivity)context;
                parent.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_enter, R.anim.left_exit).replace(R.id.fragment_container, articleFragment).addToBackStack(null).commit();

            }
        };
        return new MyViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holders, int position) {
        final MyViewHolder holder = holders;
        Articles model = articles.get(position);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(Utils.getRandomDrawableColor());
        requestOptions.error(Utils.getRandomDrawableColor());
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.centerCrop();

        Glide.with(context).load(model.getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);
        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());
        holder.author.setText(model.getAuthor());
        holder.source.setText(model.getSource().getName());
        holder.time.setText(" \u2022 " + Utils.DateToTimeFormat(model.getPublishedAt()));
        holder.publicationDate.setText(Utils.DateFormat(model.getPublishedAt()));

        Log.e("URL :", model.getUrl());

        docRef = db.collection("Articles").document(model.getUrl().replace("/", ""));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    document = task.getResult();
                    if(document != null && document.exists()) {
                        model.setRating((float)(double)document.get("rating")); //Because double != Double
                        Log.e("aaaa: ", Float.toString(model.getRating()));
                        holder.ratingBar.setVisibility(View.VISIBLE);   //Done here because otherwise it could start
                        holder.ratingBar.setRating(model.getRating());  //to display before the value was loaded
                    }else{
                        model.setRating(-1f);
                        holder.ratingBar.setVisibility(View.INVISIBLE);
                    }
                }else{
                    model.setRating(-1f);
                    holder.ratingBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void addItems(List<Articles> articles){

        /*DocumentReference docRef;
        for(Articles article : articles){
            docRef = db.collection("Articles").document(article.getUrl().replace("/", ""));
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document;
                    if(task.isSuccessful()){
                        document = task.getResult();
                        if(document != null && document.exists()) {
                            article.setRating((float)(double)document.get("rating")); //Because double != Double
                            Log.e("aaaa: ", Float.toString(article.getRating()));
                        }else{
                            article.setRating(-1f);
                        }
                    }else{
                        article.setRating(-1f);
                    }
                }
            });
        }*/
        this.articles.addAll(articles);
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title, author, description, publicationDate, source, time;
        ImageView imageView;
        RatingBar ratingBar;
        ProgressBar progressBar;
        OnItemClickListener onItemClickListener;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            author = itemView.findViewById(R.id.author);
            publicationDate = itemView.findViewById(R.id.publicationDate);
            source = itemView.findViewById(R.id.source);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.img);
            progressBar = itemView.findViewById(R.id.photo_loading);
            ratingBar = itemView.findViewById(R.id.rating);
            this.onItemClickListener = onItemClickListener;

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

}