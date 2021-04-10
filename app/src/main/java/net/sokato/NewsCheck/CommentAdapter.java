package net.sokato.NewsCheck;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.sokato.NewsCheck.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private List<Comment> comments;
    private Context context;
    private Adapter.OnItemClickListener onItemClickListener;

    DocumentReference docRef;
    DocumentSnapshot document;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CommentAdapter(List<Comment> comments, Context context){
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @Override
    public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
