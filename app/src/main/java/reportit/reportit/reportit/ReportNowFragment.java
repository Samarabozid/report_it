package reportit.reportit.reportit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import reportit.reportit.reportit.Data.ReportModel;

import static android.app.Activity.RESULT_OK;

public class ReportNowFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    int toggle = 0;
    private static final int GALLERY_REQUEST = 1;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    private EditText description_field;
    private CircleImageView report_image;
    private Button send;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private String id;
    private String imageurl, description;
    Dialog dialog;
    private String homelat;
    private String homelon;
    private String dateToStr;

    String CHANNEL_ID = "id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_now, container, false);

        buildGoogleAPIClient();
        description_field = view.findViewById(R.id.description_field);
        report_image = view.findViewById(R.id.report_photo);
        send = view.findViewById(R.id.send_btn);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        report_image.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        });

        send.setOnClickListener(view1 -> {
            description = description_field.getText().toString();

            if (TextUtils.isEmpty(description)) {
                Toast.makeText(getActivity(), "please enter small description", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(getActivity(), "please add report picture", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Please Wait Until Saving Your Report ...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            progressDialog.setCancelable(false);

            uploadImage();
        });

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        dateToStr = format.format(today);
        System.out.println(dateToStr);

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            report_image.setImageURI(imageUri);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + imageUri.getLastPathSegment());
        uploadTask = ref.putFile(imageUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            Uri downloadUri = task.getResult();

            imageurl = downloadUri.toString();
            addReport(imageurl, description, dateToStr, lastlocation.getLongitude(), lastlocation.getLatitude());
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void addReport(String imageurl, String description, String dateToStr, double longitude, double latitude) {
        id = databaseReference.child("Reports").child(getUID()).push().getKey();
        toggle = 1;
        loadingDialog();
        ReportModel reportModel = new ReportModel(id, getUID(), imageurl, description, dateToStr, longitude, latitude);
        databaseReference.child("Reports").child(getUID()).child(id).setValue(reportModel);
        databaseReference.child("AllReports").child(id).setValue(reportModel);
        progressDialog.dismiss();
    }

    private void loadingDialog() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.maps_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(ReportNowFragment.this);

        Button getmylocation = dialog.findViewById(R.id.get_my_location);
        Button cancel = dialog.findViewById(R.id.cancel_map);

        getmylocation.setOnClickListener(v -> {
            if (lastlocation == null) {
                Toast.makeText(getContext(), "please refresh your GPS and try again", Toast.LENGTH_SHORT).show();
                return;
            }

            double latitude = lastlocation.getLatitude();
            double longitude = lastlocation.getLongitude();

            LatLng myposition = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myposition));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        });

        cancel.setOnClickListener(v -> {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.commit();
            ft.remove(mapFragment);
            dialog.dismiss();
            Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();

            if (toggle == 1) {
                homelat = String.valueOf(latLng.latitude);
                homelon = String.valueOf(latLng.longitude);
                //dialog.dismiss();
            }
        });
    }

    private final static int LOCATION_REQUEST_CODE = 23;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( getActivity(), new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LOCATION_REQUEST_CODE );
        }else if (ContextCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION )
                == PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions( getActivity(), new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LOCATION_REQUEST_CODE );
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;
    }

    protected synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private String getUID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }
}