package reportit.reportit.reportit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class PreviousReportsFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DividerItemDecoration dividerItemDecoration;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String key;
    List<ReportModel> list;

    ReportAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_previous_reports, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        list = new ArrayList<>();

        databaseReference.child("Reports").child(getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ReportModel reportModel = snapshot.getValue(ReportModel.class);
                    list.add(reportModel);
                    key = snapshot.getKey();
                }

                adapter = new ReportAdapter(list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return view;
    }

    class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.reportVH> {
        List<ReportModel> reportModels;

        ReportAdapter(List<ReportModel> reportModels) {
            this.reportModels = reportModels;
        }

        @NonNull
        @Override
        public reportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_report, parent, false);
            return new reportVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull reportVH holder, final int position) {
            final ReportModel reportModel = reportModels.get(position);
            String description = reportModel.getDescription();
            String dateAndTime = reportModel.getDateAndTome();
            final String key = reportModel.getId();
            String image = reportModel.getImageurl();

            Picasso.get()
                    .load(image)
                    .error(R.drawable.user1)
                    .placeholder(R.drawable.user1)
                    .into(holder.report_image);

            holder.descriptionTxt.setText(description);
            holder.dateAndTimeTxt.setText(dateAndTime);

            holder.report_location.setOnClickListener(v -> databaseReference.child("Reports").child(getUID()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
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
                }
            }));
            holder.viewProfile.setVisibility(View.GONE);
            holder.call_image.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return reportModels.size();
        }

        class reportVH extends RecyclerView.ViewHolder {
            ImageView report_location, report_image,call_image;
            TextView descriptionTxt, dateAndTimeTxt;
            Button viewProfile;

            reportVH(@NonNull View itemView) {
                super(itemView);

                report_location = itemView.findViewById(R.id.location);
                report_image = itemView.findViewById(R.id.report_image);
                descriptionTxt = itemView.findViewById(R.id.description);
                dateAndTimeTxt = itemView.findViewById(R.id.date_and_time);
                call_image = itemView.findViewById(R.id.phonenumber_btn);
                viewProfile = itemView.findViewById(R.id.view_profile_btn);
            }
        }
    }

    private String getUID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}