package pptik.org.realtimelocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import pptik.org.realtimelocation.models.RequestStatus;
import pptik.org.realtimelocation.models.Tracker;

public class MainActivity extends AppCompatActivity {

    private MapView mapset;
    private Context context;
    private IMapController mapController;
    private static final String ACTION_STRING_ACTIVITY = "broadcast_event";
    ManagerRabbitMQ manage;
    private Tracker[] trackers;
    private String[] trackerMacs;
    private Marker[] markers;
    private boolean isFirsInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapset = (MapView)findViewById(R.id.maposm);
        context = this;
        mapset.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapset.setMultiTouchControls(true);
        mapController = mapset.getController();
        mapController.setZoom(25);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(ACTION_STRING_ACTIVITY));

        manage = new ManagerRabbitMQ(MainActivity.this);
        manage.connectToRabbitMQ();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            RequestStatus requestStatus= new Gson().fromJson(message, RequestStatus.class);
            if(requestStatus.getSuccess()){
                try {
                    JSONObject object = new JSONObject(message);
                    JSONArray jsonArray = object.getJSONArray("data");
                    if(isFirsInit){
                        Log.i("test", "Init");
                        trackers = new Tracker[jsonArray.length()];
                        trackerMacs = new String[jsonArray.length()];
                        isFirsInit = false;
                        // add markers
                        markers = new Marker[jsonArray.length()];
                        for(int i = 0; i < jsonArray.length(); i++){
                            trackers[i] = new Gson().fromJson(jsonArray.get(i).toString(), Tracker.class);
                            trackerMacs[i] = trackers[i].getMac();
                            markers[i] = new Marker(mapset);
                            markers[i].setPosition(new GeoPoint(trackers[i].getData().get(0), trackers[i].getData().get(1)));
                            markers[i].setImage(getResources().getDrawable(R.drawable.car));
                            markers[i].setIcon(getResources().getDrawable(R.drawable.car));
                            markers[i].setTitle(trackers[i].getLokasi()+"\n"+trackers[i].getKeterangan());
                            markers[i].setRelatedObject(trackers[i]);
                            mapset.getOverlays().add(markers[i]);
                        }
                        mapController.animateTo(markers[0].getPosition());

                    }else {
                        Log.i("Test", "Update");
                        requestStatus= new Gson().fromJson(message, RequestStatus.class);
                        if(requestStatus.getSuccess()){
                            if(jsonArray.length() == trackers.length){
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject entity = jsonArray.getJSONObject(i);
                                    if(trackers[i].getMac().equals(entity.getString("Mac"))){ // update markers
                                        trackers[i] = new Gson().fromJson(entity.toString(), Tracker.class);
                                        if(markers[i].getPosition().getLatitude() != trackers[i].getData().get(0) ||
                                                markers[i].getPosition().getLongitude() != trackers[i].getData().get(1)) {
                                            double bearing = bearing(markers[i].getPosition().getLatitude(), markers[i].getPosition().getLongitude(),
                                                    trackers[i].getData().get(0), trackers[i].getData().get(1));
                                            markers[i].setPosition(new GeoPoint(trackers[i].getData().get(0), trackers[i].getData().get(1)));
                                            markers[i].setTitle(trackers[i].getLokasi() + "\n" + trackers[i].getKeterangan());
                                            markers[i].setRelatedObject(trackers[i]);
                                            markers[i].setRotation((float) bearing);
                                          //  animateMarker(markers[i], markers[i].getPosition());
                                        }else {
                                            // same position
                                        }
                                    }
                                }
                                mapset.invalidate();
                            }else {
                                // found new data
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void animateMarker(final Marker marker, final GeoPoint toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mapset.getProjection();
        Point startPoint = proj.toPixels(marker.getPosition(), null);
        final IGeoPoint startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y);
        final long duration = 1500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.getLongitude() + (1 - t) * startGeoPoint.getLongitude();
                double lat = t * toPosition.getLatitude() + (1 - t) * startGeoPoint.getLatitude();
                marker.setPosition(new GeoPoint(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 15);
                }
                mapset.postInvalidate();
            }
        });
    }

    protected static double bearing(double lat1, double lon1, double lat2, double lon2){
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }


    @Override
    protected void onDestroy(){
        manage.dispose();
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}
