package com.example.gps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.VolumeShaper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.net.NetworkInterface;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener{

    SwipeRefreshLayout swipeRefreshLayout;
    private TextView text_network;

    private TextView text_gps;
    private static final String TAG = "2023";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int My_PERMISSION_ACCESS_COARSE_LOCATION = 2;
    private TextView bestprovider;
    private TextView longtitude;
    private TextView latitude;
    private TextView archivaldata;
    private LocationManager locationManager;
    private Criteria criteria;
    private Location location;
    private String bp;
    private int amount;
    private MapView osm;
    private MapController mapController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        text_network = findViewById(R.id.text_network);
        text_gps = findViewById(R.id.text_gps);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            boolean connection = isNetworkAvailable();
            if (connection) {
                text_network.setText("Internet sucessfully connected");
                text_network.setTextColor(Color.BLACK);
            } else {
                text_network.setText("No internet connection");
                text_network.setTextColor(Color.RED);
            }
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsEnabled){

                    text_gps.setText("GPS on");
                    text_gps.setTextColor(Color.GREEN);
                }else {
                    text_gps.setText("GPS off");
                    text_gps.setTextColor(Color.RED);
                }

        });
        swipeRefreshLayout.setColorSchemeColors(Color.YELLOW);

        bestprovider = findViewById(R.id.bestprovider);
        longtitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        archivaldata = findViewById(R.id.archival_data);

        criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        bp = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, My_PERMISSION_ACCESS_COARSE_LOCATION);

            return;
        }
        location = locationManager.getLastKnownLocation(bp);

        locationManager.requestLocationUpdates(bp, 500, 0.5f, this);
        bestprovider.setText("Best provider: "+bp);
        longtitude.setText("Longitude: "+ location.getLongitude());
        latitude.setText("Latitude: "+ location.getLatitude());
        archivaldata.setText(archivaldata.getText() + ":" + "\n"  + " " + location.getLongitude() + "   :   " + location.getLatitude() + "\n");
        amount += 1;
        Log.d("GPS1", amount + " pomiar: " + bp + " " + location.getLongitude() +  " " + location.getLatitude());



        osm = findViewById(R.id.wys);
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mapController = (MapController) osm.getController();
        mapController.setZoom(18);


        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        mapController.setCenter(geoPoint);
        mapController.animateTo(geoPoint);

        addMarkerToMap(geoPoint);

        osm.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i("GPS1" , "onScroll()");
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i("GPS1", "onZoom()");
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPS1", " permison "+requestCode+" "+permissions[0]+grantResults[0]);
                    Log.d(TAG, "Permisions granted");
                    Toast.makeText(this, "Permisions granted", Toast.LENGTH_SHORT).show();
                    this.recreate();
                } else {
                    Log.d(TAG, "Permision denied");
                    Toast.makeText(this, "Permision denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case My_PERMISSION_ACCESS_COARSE_LOCATION: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPS1", " permison "+requestCode+" "+permissions[0]+grantResults[0]);
                    Log.d(TAG, "Permisions granted");
                    Toast.makeText(this, "Permisions granted", Toast.LENGTH_SHORT).show();
                    this.recreate();
                } else {
                    Log.d(TAG, "Permision denied");
                    Toast.makeText(this, "Permision denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        bp = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED){
            locationManager.requestLocationUpdates(bp, 1000, 0, this);
            bestprovider.setText("Best provider: "+bp);
            longtitude.setText("Longitude: "+ location.getLongitude());
            latitude.setText("Latitude: "+ location.getLatitude());
            archivaldata.setText(archivaldata.getText() + " " + location.getLongitude() + "   :   " + location.getLatitude() + "\n");
            amount += 1;
            Log.d("GPS1", amount + " pomiar: " + bp + " " + location.getLongitude() +  " " + location.getLatitude());
        }

    }

    public void addMarkerToMap (GeoPoint center){
        Marker marker = new Marker(osm);
        marker.setPosition(center);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
        osm.getOverlays().clear();
        osm.getOverlays().add(marker);
        osm.invalidate();

        marker.setTitle("Obecna lokalizacja");
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }
}
//Marcel Parzyszek 4P 20.12.2023