package com.example.finalhack;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

/**
 * This example shows how to display and customize user location arrow on the map.
 */
public class MainActivity2 extends Activity implements UserLocationObjectListener, InputListener, GeoObjectTapListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private final String MAPKIT_API_KEY = "f3ff9065-bb96-43fb-8167-c658509ea6be";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private UserLocationLayer userLocationLayer;
    public  Point USER_POINT_LOCATION;
    double Longitude,Latitude;

    public Button location,goNext;

    private Map map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        setContentView(R.layout.activity_main2);
        super.onCreate(savedInstanceState);
        mapView = findViewById(R.id.mapview);

        location = findViewById(R.id.location);
        goNext = findViewById(R.id.goNext);


        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition(new Point(55.753215, 37.622504), 14, 0, 0));
        mapView.getMap().addInputListener(this);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);

        userLocationLayer.setObjectListener(this);


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    moveCamera(new Point((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.5)), 10);
                }
            }
        });
        goNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this,DrivingActivity.class);
                if(USER_POINT_LOCATION!=null){
                    Latitude = USER_POINT_LOCATION.getLatitude();
                    Longitude = USER_POINT_LOCATION.getLongitude();
                    intent.putExtra("Latitude", Latitude);
                    intent.putExtra("Longitude",Longitude);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {

        if(USER_POINT_LOCATION != null) {
           map.getMapObjects().clear();
        }

        USER_POINT_LOCATION = point;
        mapView.getMap().getMapObjects().addPlacemark(point);

        PlacemarkMapObject placeMark = map.getMapObjects().addPlacemark(point);
        ImageProvider imageProvider = ImageProvider.fromAsset(this, "mark.png");
        placeMark.setIcon(imageProvider);

//        Circle circle = new Circle(point, 400);
//        map.getMapObjects().addCircle(circle, Color.BLUE, 1, Color.YELLOW);
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }

    @Override
    public void onObjectAdded(UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.5)),
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.83)));

        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));

        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon(
                "icon",
                ImageProvider.fromResource(this, R.drawable.icon),
                new IconStyle().setAnchor(new PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)
        );

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(this, R.drawable.search_result),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        );

        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE);
    }

    @Override
    public void onObjectRemoved(UserLocationView view) {
    }

    @Override
    public void onObjectUpdated(UserLocationView view, ObjectEvent event) {
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity2.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void moveCamera(Point point, float zoom) {

        mapView.getMap().move(
                new CameraPosition(point, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 1),
                null);
    }

    @Override
    public boolean onObjectTap(@NonNull GeoObjectTapEvent geoObjectTapEvent) {
        return false;
    }



}




