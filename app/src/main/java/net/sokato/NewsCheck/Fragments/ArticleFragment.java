package net.sokato.NewsCheck.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import net.sokato.NewsCheck.models.Articles;
import net.sokato.NewsCheck.models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleFragment extends Fragment {

    private String URL;
    private float rating;
    private float totalRating;
    private int numRatings;
    private String status = "";
    private android.webkit.WebView webView;
    private RecyclerView commentsView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private NestedScrollView nestedScrollView;
    private FloatingActionButton newComment;
    private RatingBar userRating;

    private final Handler handler = new Handler(Looper.getMainLooper());;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        if (getArguments() != null) {
            URL = getArguments().getString("URL");
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = getView().findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(URL);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        nestedScrollView = getView().findViewById(R.id.nestedScrollView);
        newComment = getView().findViewById(R.id.newComment);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > webView.getHeight() - height) {
                    newComment.show();
                } else if (newComment.isShown()) {
                    newComment.hide();
                }
            }
        });

        newComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user == null){
                    Toast.makeText(getActivity(), R.string.needToLogin, Toast.LENGTH_LONG).show();
                }else{
                    CommentFragment commentFragment;
                    //Put the comment at the root of the article
                    ((MainActivity) getActivity()).setCurrentComment(db.collection("Articles").document(URL.replace("/", "")).collection("Comments"));

                    //Launch the comment fragment
                    commentFragment = new CommentFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_enter, R.anim.left_exit).replace(R.id.fragment_container, commentFragment).addToBackStack(null).commit();
                }
            }
        });

        LoadComments();

        userRating = getView().findViewById(R.id.userRating);

        //TODO : clean this mess of listeners

        if(user == null){
            userRating.setRating(rating);
            userRating.setIsIndicator(true); //To prevent the user from changing the value
        }else {
            db.collection("Articles").document(URL.replace("/", "")).collection("Ratings")
                    .document(((MainActivity) getActivity()).getUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists()) {
                                userRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                                    //We remove previous tasks if they existed
                                    handler.removeCallbacks(sendRating);
                                    //And we create a new one
                                    handler.postDelayed(sendRating, 2500);
                                });
                            }
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
