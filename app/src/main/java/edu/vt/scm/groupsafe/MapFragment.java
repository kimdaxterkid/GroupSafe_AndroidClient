package edu.vt.scm.groupsafe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
                                                     LocationListener {

    private MapView mMapView;
    private SupportMapFragment mSupportMapFragment;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    private MainActivity activity;

    private Map<GroupMember, Marker> markers;
    private Circle circle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.buildGoogleApiClient();
        mGoogleApiClient.connect();

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.mapView, mSupportMapFragment).commit();
        }

        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (googleMap != null) {
                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        if (ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getContext(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED) {

                            googleMap.setMyLocationEnabled(true);
                        }

                        MapFragment.this.googleMap = googleMap;
                    }
                }
            });
        }

        markers = new HashMap<GroupMember, Marker>();
        circle = null;

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {

        activity.location = location;

        if (activity.group != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);

            activity.mChatFragment.userLocationChanged(location.getLatitude(), location.getLongitude());

            if (activity.username.equals(activity.group.host.getUsername())) {
                if (circle != null) {
                    circle.setCenter(latLng);
                } else {
                    circle = googleMap.addCircle(new CircleOptions().center(latLng)
                            .radius(activity.group.getVicinity())
                            .strokeWidth(0)
                            .fillColor(0x3300ccff));
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        //TODO:mLocationRequest.setSmallestDisplacement(float smallestDisplacementMeters);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            //TODO:ask user for runtime permission
            Toast.makeText(getContext(), "Location Services Required", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

        //TODO:boot user back to login screen?
        Toast.makeText(getContext(), "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    public void initializeMap() {

        for (GroupMember member : activity.group.memberList) {
            Location location = member.getLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            markers.put(member, googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(member.getUsername())));
            if (member == activity.group.host) {
                circle = googleMap.addCircle(new CircleOptions().center(latLng)
                        .radius(activity.group.getVicinity())
                        .strokeWidth(0)
                        .fillColor(0x3300ccff));
            }
        }
    }

    public void destroyMap() {

        for (Marker marker : markers.values()){
            marker.remove();
        }
        markers.clear();
        if (circle != null) {
            circle.remove();
            circle = null;
        }
    }

    public void updateMap(String code, GroupMember user) {

        if (code.equals("left")) {
            markers.get(user).remove();
            markers.remove(user);
        } else if (code.equals("location")) {
            Marker marker = markers.get(user);
            if (marker != null) { marker.remove(); }
            Location location = user.getLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            markers.put(user, googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(user.getUsername())));
            if (user == activity.group.host) {
                if (circle != null) {
                    circle.setCenter(latLng);
                } else {
                    circle = googleMap.addCircle(new CircleOptions().center(latLng)
                            .radius(activity.group.getVicinity())
                            .strokeWidth(0)
                            .fillColor(0x3300ccff));
                }
            }
        } else if (code.equals("newHost")) {
            Location location = user.getLocation();
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (circle != null) {
                    circle.setCenter(latLng);
                } else {
                    circle = googleMap.addCircle(new CircleOptions().center(latLng)
                            .radius(activity.group.getVicinity())
                            .strokeWidth(0)
                            .fillColor(0x3300ccff));
                }
            }
        }
    }
}
