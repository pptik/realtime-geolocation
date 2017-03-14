package pptik.org.realtimelocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import pptik.org.realtimelocation.adapter.ListAdapter;
import pptik.org.realtimelocation.models.RequestStatus;
import pptik.org.realtimelocation.models.Tracker;

public class MainActivity extends AppCompatActivity implements ListAdapter.MarkerPositionListener {

    private MapView mapset;
    private Context context;
    private IMapController mapController;
    private static final String ACTION_STRING_ACTIVITY = "broadcast_event";
    ManagerRabbitMQ manage;
    private Tracker[] trackers;
    private String[] trackerMacs;
    private Marker[] markers;
    private boolean isFirsInit = true;
    private ListView listView;
    private ListAdapter adapter;
    private int checkedState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapset = (MapView)findViewById(R.id.maposm);
        listView = (ListView)findViewById(R.id.listView);

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


    private void setListView() {

        adapter = new ListAdapter(context, trackers, checkedState, this);
        listView.setAdapter(adapter);
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
                            String info = trackers[i].getLokasi()+"\n"+trackers[i].getKeterangan()+"\nLokasi Tanggal : "+trackers[i].getDate()+"\nKecepatan : "+trackers[i].getSpeed()+" KM/H";
                            markers[i].setTitle(info);
                            markers[i].setRelatedObject(trackers[i]);
                            mapset.getOverlays().add(markers[i]);
                        }
                        setListView();
                        mapController.animateTo(markers[0].getPosition());

                    }else {
                        Log.i("Test", "Update");
                        requestStatus= new Gson().fromJson(message, RequestStatus.class);
                        OSMarkerAnimation markerAnimation = new OSMarkerAnimation();
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
                                            String info = trackers[i].getLokasi()+"\n"+trackers[i].getKeterangan()+"\nLokasi Tanggal : "+trackers[i].getDate()+"\nKecepatan : "+trackers[i].getSpeed()+" KM/H";
                                            markers[i].setTitle(info);
                                            markers[i].setRelatedObject(trackers[i]);
                                            markers[i].setRotation((float) bearing);

                                            markerAnimation.animate(mapset, markers[i],
                                                    new GeoPoint(trackers[i].getData().get(0), trackers[i].getData().get(1)),
                                                    1500);
                                        }else {
                                            // same position
                                        }
                                    }
                                }
                                setListView();

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

    @Override
    public void onMarkerSelected(int position) {
        Log.i("Pos", String.valueOf(position));
        checkedState = position;
        setListView();
    }
}
