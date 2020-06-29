package reportit.reportit.reportit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import reportit.reportit.reportit.Data.UserModel;

public class ViewProfileActivity extends AppCompatActivity {
    String userID;

    static EditText email_field, fullname_field, mobile_field, address_field, gender_field;
    ImageView profilepicture;
    TextView fullname_txt;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        userID = getIntent().getStringExtra("userID");

        fullname_txt = findViewById(R.id.fullname_txt);
        profilepicture = findViewById(R.id.patient_profile_picture);
        email_field = findViewById(R.id.email_field);
        fullname_field = findViewById(R.id.fullname_field);
        mobile_field = findViewById(R.id.mobile_field);
        address_field = findViewById(R.id.address_field);
        gender_field = findViewById(R.id.gender_field);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        returndata();
    }

    public void returndata() {
        databaseReference.child("Users").child(userID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        mobile_field.setText(userModel.getPhone());
                        email_field.setText(userModel.getEmail());
                        fullname_txt.setText(userModel.getName());
                        fullname_field.setText(userModel.getName());
                        gender_field.setText(userModel.getGender());
                        address_field.setText(userModel.getCountry() + " ,"
                                + userModel.getState() + " ,"
                                + userModel.getCity() + " , "
                                + userModel.getKnownName());

                        Picasso.get()
                                .load(userModel.getImageUrl())
                                .placeholder(R.drawable.user1)
                                .error(R.drawable.user1)
                                .into(profilepicture);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
