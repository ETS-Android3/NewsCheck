package net.sokato.NewsCheck.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.sokato.NewsCheck.Adapter;
import net.sokato.NewsCheck.CommentAdapter;
import net.sokato.NewsCheck.MainActivity;
import net.sokato.NewsCheck.R;
import net.sokato.NewsCheck.Utils;
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This fragment is the one shown when the user clicks on
 * an article. It shows a cardView to access the article in the
 * article in the default browser, and loads the rating and comments
 * that have been posted on the article.
 *
 * A timer is used in order to make sure the user has read the article
 * before commenting or rating it. The user currently can only comment
 * or rate the article one minute after having opened it
 *
 * This fragments is also where the user can rate an article and comment on it,
 * using a ratingBar, and a comment system.**/

public class ArticleFragment extends Fragment {

    private String URL;
    private String URLToImage;
    private float rating;
    private float totalRating;
    private int numRatings;
    private String status = "";  //String holding the status of the user posting a comment
    private RecyclerView commentsView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private FloatingActionButton newComment;
    private RatingBar userRating;

    private CardView cardView;
    private ImageView imgP;

    private final Handler handler = new Handler(Looper.getMainLooper());;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //Variable used to prevent the user from reacting too fast
    //In less than one minute, we assume that he didn't read the
    //article
    private boolean canComment = false;
    private final int interval = 60000; // 1 Minute
    private final Handler commentHandler = new Handler();
    private final Runnable runnable = new Runnable(){
        public void run() {
            canComment = true;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        if (getArguments() != null) {
            URL = getArguments().getString("URL");
            URLToImage = getArguments().getString("URLToImage");
            rating = getArguments().getFloat("rating");
            totalRating = getArguments().getFloat("totalRatings");
            numRatings = getArguments().getInt("numRatings");
        }

        //checking if the article exists, if not, initialising it
        DocumentReference docRef = db.collection("Articles").document(URL.replace("/", ""));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document != null && !document.exists()) {
                        //The article doesn't exist, we need to initialise it;
                        Map<String, Object> articleData = new HashMap<>();
                        articleData.put("rating", -1);
                        articleData.put("numRatings", 0);
                        articleData.put("totalRatings", 0);
                        docRef.set(articleData);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newComment = getView().findViewById(R.id.newComment);

        //The user clicks on the new comment button
        newComment.setOnClickListener(v -> {
            if(user == null){
                //If he is not logged in, we say it
                Toast.makeText(getActivity(), R.string.needToLogin, Toast.LENGTH_LONG).show();
            }else if(!canComment) {
                //If the user hasn't been here for long enough
                Toast.makeText(getActivity(), R.string.needToStay, Toast.LENGTH_LONG).show();
            }else{
                //Else, everything is good, we let the use comment on the article
                CommentFragment commentFragment;
                //Put the comment at the root of the article
                ((MainActivity) getActivity()).setCurrentComment(db.collection("Articles").document(URL.replace("/", "")).collection("Comments"));

                //Launch the comment fragment
                commentFragment = new CommentFragment();
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_enter, R.anim.left_exit).replace(R.id.fragment_container, commentFragment).addToBackStack(null).commit();
            }
        });

        LoadComments();

        userRating = getView().findViewById(R.id.userRating);

        if(user == null){
            userRating.setRating(rating);
            userRating.setIsIndicator(true); //To prevent the user from changing the value
        }else {
            db.collection("Articles").document(URL.replace("/", "")).collection("Ratings")
                    .document(((MainActivity) getActivity()).getUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        //We check if the user has already rated the article
                        if(!documentSnapshot.exists()) { //If not
                            //We use this to see if the user has modified the
                            //rating bar. If he did and left it then, we assume
                            //that it is his final rating
                            userRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                                if(canComment) {
                                    //We remove previous tasks if they existed
                                    handler.removeCallbacks(sendRating);
                                    //And we create a new one
                                    handler.postDelayed(sendRating, 2500);
                                }else{
                                    Toast.makeText(getActivity(), R.string.needToStay, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
        }

        commentsView = getView().findViewById(R.id.commentsView);
        layoutManager = new LinearLayoutManager(getActivity());
        commentsView.setLayoutManager(layoutManager);
        commentsView.setItemAnimator(new DefaultItemAnimator());
        commentsView.setNestedScrollingEnabled(false);

        adapter = new CommentAdapter(comments, getActivity());
        commentsView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        cardView = getActivity().findViewById(R.id.articleCardView);
        imgP = getActivity().findViewById(R.id.imgP);

        cardView.setOnClickListener(v -> {
            //We launch the browser to see the article
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
            startActivity(browserIntent);

            //We need the activity up for one minute before the use can comment
            //To make sure that he read the article
            commentHandler.postDelayed(runnable, interval);
        });

        //We load the image
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(Utils.getRandomDrawableColor());
        requestOptions.error(Utils.getRandomDrawableColor());
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.centerCrop();

        Glide.with(getActivity()).load(URLToImage)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {

                    final ProgressBar progressBar = getActivity().findViewById(R.id.articleLoading);

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgP);

    }

    //Runnable used to start the rating process
    //a few seconds after the user finished rating
    Runnable sendRating = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("rating", userRating.getRating());

            db.collection("Articles").document(URL.replace("/", "")).collection("Ratings")
                    .document(((MainActivity) getActivity()).getUser().getUid())
                    .set(ratingData)
                    .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateRating(userRating.getRating());
                } else {
                    Toast.makeText(getActivity(), R.string.ratingError, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    void LoadComments(){
        //Populate the comment list
        db.collection("Articles").document(URL.replace("/", "")).collection("Comments")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            adapter.flushComments();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Comment comment = new Comment();
                                comment.setAuthor(document.get("AuthorName").toString());
                                comment.setCommentText(document.get("CommentBody").toString());
                                comment.setAuthorID(document.get("AuthorID").toString());
                                comment.setOrder(0);
                                comment.setParent(db.collection("Articles")
                                        .document(URL.replace("/", ""))
                                        .collection("Comments")
                                        .document(document.getId())
                                        .collection("Comments"));
                                getStatus(comment);
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.commentsLoadingFailed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void getStatus(Comment comment){

        db.collection("Users").document(comment.getAuthorID())
                .get()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        status = "";
                    }else if(!task.getResult().exists()){
                        Log.e("fuck", "");
                        status = "";
                    }else{
                        status = task.getResult().getString("accountType");
                        if(status!=null && status.equals("normal user")){
                            status = "";
                        }
                        comment.setAuthorStatus(status);
                        adapter.addItem(comment);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    void updateRating(float newRating){

        totalRating += newRating;
        numRatings++;

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", totalRating/numRatings);
        ratingData.put("numRatings", numRatings);
        ratingData.put("totalRatings", totalRating);

        db.collection("Articles").document(URL.replace("/", ""))
                .set(ratingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRating.setOnRatingBarChangeListener(null);
                        userRating.setRating(totalRating/numRatings);
                        userRating.setIsIndicator(true); //To prevent the user from changing the value
                        Toast.makeText(getActivity(), R.string.ratingSent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.ratingError, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
