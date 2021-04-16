package net.sokato.NewsCheck.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import net.sokato.NewsCheck.R;

import javax.annotation.Nullable;

public class CommentFragment extends Fragment {

    private String URL;
    private FloatingActionButton sendComment;
    private EditText commentText;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState){
        if (getArguments() != null) {
            URL = getArguments().getString("URL");
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commentText = getView().findViewById(R.id.commentText);
        sendComment = getView().findViewById(R.id.sendComment);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : check if the comment is empty, if not, send it
                //Maybe we can use the activity to store the database path
            }
        });
    }

}
