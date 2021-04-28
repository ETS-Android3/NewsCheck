package net.sokato.NewsCheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import net.sokato.NewsCheck.Fragments.NewsFragment;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*Because we use lots of fragments instead of activities,
nothing is really drawn in this activity.
We have a few global variables that are here in order to pass
objects between fragments, as it isn't supported by default. */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    private NewsFragment newsFragment;
    private TextView login;
    private TextView accountType;
    private FirebaseUser user;
    private ImageView accountPicture;

    /*The comment to put the new comment under
    This can be the article itself*/
    private CollectionReference currentComment;

    DocumentReference docRef;
    DocumentSnapshot document;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolBar, R.string.nav_drawer_open, R.string.nav_drawer_close){
            @Override
            public void onDrawerStateChanged(int newState) {
                //This function is called every time the drawer is moved
                //from a resting state to another
                login = findViewById(R.id.name);
                accountPicture = findViewById(R.id.accountPicture);
                accountType = findViewById(R.id.accountType);

                //This is where we decide what to draw on the drawer
                if(user == null) {
                    //The login is handled by Firebase, which is a secure solution
                    //And it is easy to implement
                    login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //Start the login activity
                            //We implement the chosen login methods
                            List<AuthUI.IdpConfig> providers = Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build());

                            // Create and launch sign-in intent
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .build(),
                                    7);
                        }
                    });
                }else{
                    loadAccountData();
                }

                //This is used to launch an activity in which the user can
                //modify his parameters
                accountPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(user!=null) {
                            Intent intent = new Intent(MainActivity.this, Account.class);
                            startActivity(intent);
                        }
                    }
                });
                super.onDrawerStateChanged(newState);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null) {  //If this is the first loading of the activity
            //We load the main fragment
            newsFragment = new NewsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newsFragment).commit();
            navigationView.setCheckedItem(R.id.nav_news);
        }

    }

    @Override
    public void onBackPressed(){ //Function called when the user presses the back button
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) { //Function called when the uses chooses an item from the drawer
        switch(item.getItemId()){
            //It only has a single item for now.
            case R.id.nav_news:
                newsFragment = new NewsFragment();
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_enter, R.anim.left_exit).replace(R.id.fragment_container, newsFragment).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                loadAccountData();
            } else {
                Toast.makeText(MainActivity.this, R.string.loginFailure, Toast.LENGTH_LONG).show();
            }
        }

    }

    //This function is tasked with retrieving the user's data
    //from the Firebase database
    void loadAccountData(){

        HashMap<String, Object> defaultData = new HashMap<>();
        defaultData.put("username", "");
        defaultData.put("accountType", "normal user");

        login.setText(user.getDisplayName());

        //If the user has a profile picture, we fetch it
        if(user.getPhotoUrl() != null) {
            Glide.with(getBaseContext())
                    .load(user.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into((ImageView) findViewById(R.id.accountPicture));
        }

        docRef = db.collection("Users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                document = task.getResult();
                if(document != null && document.exists()) {
                    accountType.setText(document.get("accountType").toString());
                }else{
                    docRef.set(defaultData, SetOptions.merge()); //If this is the first connection, we set up the account
                }
            }else{
                Toast.makeText(MainActivity.this, R.string.accountDataLoadFailed, Toast.LENGTH_LONG).show();
            }
        });

    }

    public CollectionReference getCurrentComment() {
        return currentComment;
    }

    public void setCurrentComment(CollectionReference currentComment) {
        this.currentComment = currentComment;
    }

    public FirebaseUser getUser() {
        return user;
    }
}