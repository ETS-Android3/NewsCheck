package net.sokato.NewsCheck.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.sokato.NewsCheck.MainActivity;
import net.sokato.NewsCheck.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class CommentFragment extends Fragment {

    private String URL;
    private FloatingActionButton sendComment;
    private EditText commentText;

    private FirebaseFirestore db;
    private FirebaseUser user;

    private CollectionReference collection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState){
        if (getArguments() != null) {
            URL = getArguments().getString("URL");
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Finding where to put the comment in the database
        db = FirebaseFirestore.getInstance();
        collection = ((MainActivity)getActivity()).getCurrentComment();

        commentText = Objects.requireNonNull(getView()).findViewById(R.id.commentText);

        sendComment = Objects.requireNonNull(getView()).findViewById(R.id.sendComment);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collection != null && !commentText.getText().toString().equals("")){
                    //Create the data to be sent to the database
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("AuthorName", user.getDisplayName()); //TODO : update the display name on the account parameters page
                    commentData.put("CommentBody", commentText.getText().toString());
                    commentData.put("AuthorID", user.getUid());

                    collection.add(commentData)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), R.string.ErrorSendingComment, Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getActivity(), R.string.CommentSent, Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack();
                                }
                            });

                }
            }
        });
    }

}
