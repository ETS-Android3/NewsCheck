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
import net.sokato.NewsCheck.Fragments.CommentFragment;
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyCommentViewHolder>{

    private List<Comment> comments;
    private Context context;
    private OnItemClickListener onItemClickListener;

    DocumentReference docRef;
    DocumentSnapshot document;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CommentAdapter(List<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @Override
    public MyCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment, parent, false);
        onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                //TODO : handle clicks on comments

            }
        };



        return new MyCommentViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCommentViewHolder holders, int position) {
        final MyCommentViewHolder holder = holders;
        Comment model = comments.get(position);

        holder.authorName.setText(model.getAuthor());
        holder.authorStatus.setText(model.getAuthorStatus());
        holder.commentBody.setText(model.getCommentText());

        holder.respondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).setCurrentComment(model.getParent());
                //Launch the comment fragment
                CommentFragment commentFragment = new CommentFragment();
                ((MainActivity)context).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_enter, R.anim.left_exit).replace(R.id.fragment_container, commentFragment).addToBackStack(null).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public class MyCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView authorName, authorStatus, commentBody, respondButton;
        RecyclerView childCommentsView;
        OnItemClickListener onItemClickListener;

        public MyCommentViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            authorName = itemView.findViewById(R.id.authorName);
            authorStatus = itemView.findViewById(R.id.authorStatus);
            commentBody = itemView.findViewById(R.id.commentBody);
            childCommentsView = itemView.findViewById(R.id.childCommentsView);
            respondButton = itemView.findViewById(R.id.respondButton);
            this.onItemClickListener = onItemClickListener;

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void flushComments(){
        this.comments.clear();
    }

    public void addItem(Comment comment){
        this.comments.add(comment);
    }

}
