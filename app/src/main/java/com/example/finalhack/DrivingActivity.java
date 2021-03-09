package com.example.finalhack;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This example shows how to build routes between two points and display them on the map.
 * Note: Routing API calls count towards MapKit daily usage limits. Learn more at
 * https://tech.yandex.ru/mapkit/doc/3.x/concepts/conditions-docpage/#conditions__limits
 */
public class DrivingActivity extends Activity implements DrivingSession.DrivingRouteListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private final String MAPKIT_API_KEY = "f3ff9065-bb96-43fb-8167-c658509ea6be";
    private Point ROUTE_START_LOCATION;
    private Point ROUTE_END_LOCATION;
    double[] array ={42.968558, 47.404655,  42.975792,47.437336,  42.985912, 47.481517,   42.984792, 47.497129,  42.983789, 47.508693,  42.963706, 47.546930};
    ArrayList<Double> arrayList = new ArrayList<>();

    Button bt;
    int i = 0;

    private Point moveCamera;

    private String MY_LOG ="LOG";
    private String check ="";

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    Point point= new Point(0,0);
    double Latitude1,Longitude1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity2 mc = new MainActivity2();
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        new ParsAllRecipe().execute();

        setContentView(R.layout.activity_driving);
        super.onCreate(savedInstanceState);

        bt = findViewById(R.id.bt);

        Latitude1 = getIntent().getDoubleExtra("Latitude", 0);
        Longitude1 = getIntent().getDoubleExtra("Longitude",0);
        moveCamera = new Point(Latitude1,Longitude1);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.getMap().move(new CameraPosition(
                moveCamera, 5, 0, 0));
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ROUTE_START_LOCATION = new Point(array[i],array[i+1]);
                for (int i = 2; i < array.length; i+=2) {
                    ROUTE_END_LOCATION = new Point(array[i], array[i+1]);
                    submitRequest();
                    for (int j = 0; j < i; j++) {
                        ROUTE_START_LOCATION = new Point(array[i],array[i+1]);
                    }
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
    public void onDrivingRoutes(List<DrivingRoute> routes) {
        for (DrivingRoute route : routes) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    @Override
    public void onDrivingRoutesError(Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, this);
    }

    class ParsAllRecipe extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String urlHome = "http://a0521536.xsph.ru";

            try {
                Document doc = Jsoup.connect(urlHome).get();

                String url = urlHome+""+moveCamera+2;
                itemRecipes(url);


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        private void itemRecipes(String url){
            try {

                String imgRecipe;
                Document doc = Jsoup.connect(url).get();
                Elements els = doc.select("body");
                for(Element el : els){
                    imgRecipe = el.select("body").text();
                    String[] words = imgRecipe.split(",");
                    for (String word : words) {
                        arrayList.add(Double.parseDouble(word));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
