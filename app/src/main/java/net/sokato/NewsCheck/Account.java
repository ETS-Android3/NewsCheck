package net.sokato.NewsCheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**This activity is where the user can modify the application's
 * parameters, as well as his account's parameters
 * It is a separate activity and not a fragment to allow us to reload
 * the main activity afterwards, making the changes effective without
 * the need for a reboot**/

public class Account extends AppCompatActivity {

    private Button logoutButton;
    private Button verifyAccount;
    private ImageView userIcon;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        user = FirebaseAuth.getInstance().getCurrentUser();

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> AuthUI.getInstance()
                .signOut(Account.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        //When disconnected, we go back to the main activity
                        Intent intent = new Intent(Account.this, MainActivity.class);
                        startActivity(intent);
                    }
                }));

        verifyAccount = findViewById(R.id.requestVerificationButton);
        verifyAccount.setOnClickListener(v -> {
          dispatchTakePictureIntent();
        });

        //Once again, if the user has an account picture set up
        //we try to load it
        userIcon = findViewById(R.id.accountParametersIcon);
        if(user.getPhotoUrl() != null) {
            Glide.with(Account.this)
                    .load(user.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(userIcon);
        }
    }

    //This function is used to start the picture taking process
    //It sends an intent for the default photo app, if there is one, to
    //open up and take a picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "net.sokato.NewsCheck",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    //This function is called when we return from the photo app, this is where we choose
    //what to do based on the result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If everything was okay
        if(resultCode == Activity.RESULT_OK){
            uploadPhoto();
        //If the user cancelled the picture
        }else if(resultCode == Activity.RESULT_CANCELED) {
            //TODO: handle when the user cancels the picture
        }
    }

    //This function uploads the photo to Firebase for processing
    private void uploadPhoto(){
        //We store the picture as the user's UID.jpg
        StorageReference imageRef = storageReference.child(user.getUid()+".jpg");
        imageRef.putFile(Uri.fromFile(new File(currentPhotoPath)));
    }

    //This function is responsible for creating the file in which the picture will be
    //stored on the device
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}