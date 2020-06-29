package reportit.reportit.reportit;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import reportit.reportit.reportit.Data.ReportModel;
import reportit.reportit.reportit.Data.UserModel;

public class AdminStartActivity extends AppCompatActivity {

    TextView logout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DividerItemDecoration dividerItemDecoration;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String key;
    List<ReportModel> list;
    ReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_start);

        logout = findViewById(R.id.logout);
        recyclerView = findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        list = new ArrayList<>();

        logout.setOnClickListener(view -> new AlertDialog.Builder(AdminStartActivity.this)
                .setTitle("Sign Out !!")
                .setMessage("Are You Sure To Sign Out ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AdminStartActivity.this, LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show());

        databaseReference.child("AllReports").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ReportModel reportModel = snapshot.getValue(ReportModel.class);
                    list.add(reportModel);
                    key = reportModel.getId();
                }

                adapter = new ReportAdapter(list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.reportVH> {
        List<ReportModel> reportModels;

        ReportAdapter(List<ReportModel> reportModels) {
            this.reportModels = reportModels;
        }

        @NonNull
        @Override
        public reportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_report, parent, false);
            return new reportVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull reportVH holder, final int position) {
            final ReportModel reportModel = reportModels.get(position);
            String description = reportModel.getDescription();
            String dateAndTime = reportModel.getDateAndTome();
            final String key = reportModel.getId();
            String reporterID = reportModel.getReporterID();
            String image = reportModel.getImageurl();

            Picasso.get()
                    .load(image)
                    .error(R.drawable.user1)
                    .placeholder(R.drawable.user1)
                    .into(holder.report_image);

            holder.descriptionTxt.setText(description);
            holder.dateAndTimeTxt.setText(dateAndTime);

            holder.report_location.setOnClickListener(v -> databaseReference.child("Reports").child(reporterID).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ReportModel reportModel1 = dataSnapshot.getValue(ReportModel.class);

                    double longitude = reportModel1.getLongitude();
                    double latitude = reportModel1.getLatitude();

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latitude + "," + longitude));
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AdminStartActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                }
            }));
            holder.viewProfile.setOnClickListener(view -> {
                Intent intent = new Intent(AdminStartActivity.this, ViewProfileActivity.class);
                intent.putExtra("userID", reporterID);
                startActivity(intent);
            });

            holder.call_image.setOnClickListener(view -> {
                databaseReference.child("Users").child(reporterID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        String mobile = userModel.getPhone();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + mobile));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            });

            holder.reportLayout.setOnClickListener(view -> new AlertDialog.Builder(AdminStartActivity.this)
                    .setTitle("Delete Report")
                    .setMessage("Are you want to delete this report?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //databaseReference.child("Reports").child(reporterID).child(key).removeValue();
                        databaseReference.child("AllReports").child(key).removeValue();
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", null)
                    .show());
        }

        @Override
        public int getItemCount() {
            return reportModels.size();
        }

        class reportVH extends RecyclerView.ViewHolder {
            ImageView report_location, report_image, call_image;
            TextView descriptionTxt, dateAndTimeTxt;
            Button viewProfile;
            LinearLayout reportLayout;

            reportVH(@NonNull View itemView) {
                super(itemView);

                reportLayout = itemView.findViewById(R.id.report_layout);
                report_location = itemView.findViewById(R.id.location);
                report_image = itemView.findViewById(R.id.report_image);
                descriptionTxt = itemView.findViewById(R.id.description);
                dateAndTimeTxt = itemView.findViewById(R.id.date_and_time);
                call_image = itemView.findViewById(R.id.phonenumber_btn);
                viewProfile = itemView.findViewById(R.id.view_profile_btn);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.finishAffinity();
    }
}