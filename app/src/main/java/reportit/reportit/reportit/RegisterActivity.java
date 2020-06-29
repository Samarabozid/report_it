package reportit.reportit.reportit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import reportit.reportit.reportit.Data.UserModel;

public class RegisterActivity extends AppCompatActivity {

    EditText email_field, password_field, confirm_field, name_field, contact_field;
    TextView location_field;
    CircleImageView profile;
    Button register;
    RadioButton male_rb, female_rb;

    String email, password, name, contact, gender, confirm, imageurl;
    Double latitude, longitude;

    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ProgressDialog progressDialog;
    Uri photoPath;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    private String city;
    private String state;
    private String country;
    private String knownName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profile = findViewById(R.id.profile);
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        confirm_field = findViewById(R.id.confirm_password_field);
        name_field = findViewById(R.id.name_field);
        contact_field = findViewById(R.id.phone_field);
        location_field = findViewById(R.id.location_field);
        female_rb = findViewById(R.id.female_rb);
        male_rb = findViewById(R.id.male_rb);
        register = findViewById(R.id.register_btn);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        location_field.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
            startActivityForResult(i, SECOND_ACTIVITY_REQUEST_CODE);
        });

        profile.setOnClickListener(v -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .start(RegisterActivity.this));

        male_rb.setOnClickListener(v -> gender = "male");

        female_rb.setOnClickListener(v -> gender = "female");

        register.setOnClickListener(v -> {
            email = email_field.getText().toString();
            name = name_field.getText().toString();
            password = password_field.getText().toString();
            confirm = confirm_field.getText().toString();
            contact = contact_field.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "please enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getApplicationContext(), "please enter  name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "password is too short", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!confirm.equals(password)) {
                Toast.makeText(getApplicationContext(), "password is not matching", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(gender)) {
                Toast.makeText(getApplicationContext(), "please select gender", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(contact)) {
                Toast.makeText(getApplicationContext(), "please enter number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoPath == null) {
                Toast.makeText(getApplicationContext(), "please add your picture", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setTitle("User Account");
            progressDialog.setMessage("Please Wait Until Creating Account ...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            progressDialog.setCancelable(false);

            createUser(name, email, contact, gender, imageurl, city, state, country, knownName, longitude, latitude);
        });
    }

    private void createUser(final String name, final String email, final String phone, final String gender, String imageurl, String city, String state, String country, String knownName, final double longitude, final double latitude) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadImage(name, email, phone, gender, city, state, country, knownName, longitude, latitude);
                    } else {
                        String error_message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void uploadImage(final String name, final String email, final String phone, final String gender, String city, String state, String country, String knownName,
                             final double longitude, final double latitude) {
        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + photoPath.getLastPathSegment());

        uploadTask = ref.putFile(photoPath);

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            Uri downloadUri = task.getResult();

            imageurl = downloadUri.toString();
            addUser(name, email, phone, gender, imageurl, city, state, country, knownName, longitude, latitude);
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void addUser(String name, String email, String phone, String gender, String imageurl, String city, String state, String country, String knownName
            , double longitude, double latitude) {
        UserModel userModel = new UserModel(getUID(), name, email, phone, gender, imageurl, city, state, country, knownName, longitude, latitude);
        databaseReference.child("Users").child(getUID()).setValue(userModel);

        Intent intent = new Intent(getApplicationContext(), UserStartActivity.class);
        startActivity(intent);
        progressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String lat = data.getStringExtra("latitude");
                String lon = data.getStringExtra("longitude");
                city = data.getStringExtra("city");
                state = data.getStringExtra("state");
                country = data.getStringExtra("country");
                knownName = data.getStringExtra("knownName");
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);
                location_field.setText(country + ", " + state + ", " + city + ", " + knownName);
            } else {
                Toast.makeText(RegisterActivity.this, "Location Does not saved ! Try again", Toast.LENGTH_SHORT).show();
                location_field.setText("Location *");
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                if (result != null) {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.user1)
                            .error(R.drawable.user1)
                            .into(profile);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private String getUID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}
