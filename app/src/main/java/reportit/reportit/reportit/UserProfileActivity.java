package reportit.reportit.reportit;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;

import reportit.reportit.reportit.Data.UserModel;

public class UserProfileActivity extends AppCompatActivity {

    Button edit_profile_btn, savechanges_btn;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    ImageView profilepicture, mapImage;
    TextView fullname_txt;
    LinearLayout save_card;
    static EditText email_field, fullname_field, mobile_field, address_field, gender_field,longitude_field,latitude_field;
    String profile_image_url;
    String email, name, contact, gender;
    Double latitude, longitude;
    private String city;
    private String state;
    private String country;
    private String knownName;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String selected_placeimaeURL = "";
    Uri photoPath;

    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;

    String pass;
    private String address;
    private Button signoutBtn;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        signoutBtn = findViewById(R.id.sign_out_btn);
        mapImage = findViewById(R.id.map_image);
        edit_profile_btn = findViewById(R.id.edit_profile_btn);
        savechanges_btn = findViewById(R.id.savechanges_btn);
        save_card = findViewById(R.id.save_card);
        fullname_txt = findViewById(R.id.fullname_txt);
        profilepicture = findViewById(R.id.patient_profile_picture);
        email_field = findViewById(R.id.email_field);
        fullname_field = findViewById(R.id.fullname_field);
        mobile_field = findViewById(R.id.mobile_field);
        address_field = findViewById(R.id.address_field);
        gender_field = findViewById(R.id.gender_field);
        longitude_field = findViewById(R.id.longitude_field);
        latitude_field = findViewById(R.id.latitude_field);

        rotateLoading = findViewById(R.id.rotateloading);

        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(UserProfileActivity.this)
                        .setTitle("Sign Out !!")
                        .setMessage("Are You Sure To Sign Out ?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(UserProfileActivity.this,LoginActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        mapImage.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
            startActivityForResult(i, SECOND_ACTIVITY_REQUEST_CODE);
        });

        saveLogin = loginPreferences.getBoolean("savepassword", false);

        if (saveLogin) {
            pass = loginPreferences.getString("pass", "");
        }

        save_card.setVisibility(View.GONE);
        email_field.setEnabled(false);
        fullname_field.setEnabled(false);
        mobile_field.setEnabled(false);
        address_field.setEnabled(false);
        profilepicture.setEnabled(false);
        savechanges_btn.setEnabled(false);
        mapImage.setEnabled(false);
        gender_field.setEnabled(false);
        longitude_field.setVisibility(View.GONE);
        latitude_field.setVisibility(View.GONE);

        savechanges_btn.setOnClickListener(v -> {
            latitude = Double.valueOf(latitude_field.getText().toString());
            longitude = Double.valueOf(longitude_field.getText().toString());
            gender = gender_field.getText().toString();
            name = fullname_field.getText().toString();
            email = email_field.getText().toString();
            contact = mobile_field.getText().toString();
            address = address_field.getText().toString();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getApplicationContext(), "please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(contact)) {
                Toast.makeText(getApplicationContext(), "please enter your mobile number", Toast.LENGTH_SHORT).show();
                return;
            }

            save_card.setVisibility(View.GONE);
            fullname_field.setEnabled(false);
            mobile_field.setEnabled(false);
            address_field.setEnabled(false);
            profilepicture.setEnabled(false);
            savechanges_btn.setEnabled(false);
            mapImage.setEnabled(false);
            edit_profile_btn.setEnabled(true);

            if (photoPath == null) {
                UpdateUserData(name, email, contact, gender, profile_image_url, city, state, country, knownName, longitude, latitude);
            } else {
                uploadImage(name, email, contact, gender, city, state, country, knownName, longitude, latitude);
            }
        });

        profilepicture.setOnClickListener(v -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .start(UserProfileActivity.this));

        edit_profile_btn.setOnClickListener(v -> {
            save_card.setVisibility(View.VISIBLE);
            fullname_field.setEnabled(true);
            mobile_field.setEnabled(true);
            address_field.setEnabled(false);
            profilepicture.setEnabled(true);
            savechanges_btn.setEnabled(true);
            mapImage.setEnabled(true);
            edit_profile_btn.setEnabled(false);
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        rotateLoading.start();

        returndata();
    }

    public void returndata() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        final String userId = user.getUid();

        databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        city = userModel.getCity();
                        state = userModel.getState();
                        country = userModel.getCountry();
                        knownName = userModel.getKnownName();
                        mobile_field.setText(userModel.getPhone());
                        email_field.setText(userModel.getEmail());
                        fullname_txt.setText(userModel.getName());
                        fullname_field.setText(userModel.getName());
                        gender_field.setText(userModel.getGender());
                        longitude = userModel.getLongitude();
                        latitude = userModel.getLatitude();
                        String l = String.valueOf(latitude);
                        String m = String.valueOf(longitude);
                        longitude_field.setText(m);
                        latitude_field.setText(l);

                        gender = userModel.getGender();
                        address_field.setText(userModel.getCountry() + " ,"
                                + userModel.getState() + " ,"
                                + userModel.getCity() + " , "
                                + userModel.getKnownName());
                        profile_image_url = userModel.getImageUrl();

                        Picasso.get()
                                .load(profile_image_url)
                                .placeholder(R.drawable.user1)
                                .error(R.drawable.user1)
                                .into(profilepicture);

                        rotateLoading.stop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void UpdateUserData(String name, String email, String phone, String gender, String imageurl, String city, String state, String country, String knownName
            , double longitude, double latitude) {
        UserModel userModel = new UserModel(getUid(), name, email, phone, gender, imageurl, city, state, country, knownName, longitude, latitude);
        databaseReference.child("Users").child(getUid()).setValue(userModel);

        Toast.makeText(UserProfileActivity.this, "saved", Toast.LENGTH_SHORT).show();

        if (photoPath == null) {
            returndata();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void uploadImage(final String name, final String email, final String phone, final String gender, String city, String state, String country, String knownName,
                             final double longitude, final double latitude) {
        rotateLoading.start();

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

            rotateLoading.stop();

            selected_placeimaeURL = downloadUri.toString();

            UpdateUserData(name, email, phone, gender, selected_placeimaeURL, city, state, country, knownName, longitude, latitude);

            Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();

            returndata();
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getApplicationContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
        });
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
                address_field.setText(country + "," + state + "," + city + "," + knownName);
            } else {
                Toast.makeText(UserProfileActivity.this, "Location Does not saved ! Try again", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.user1)
                            .error(R.drawable.user1)
                            .into(profilepicture);

                    selected_placeimaeURL = photoPath.toString();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
