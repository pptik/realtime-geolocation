package pptik.org.realtimelocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    private MapView mapset;
    private Context context;
    private IMapController mapController;
    private static final String ACTION_STRING_ACTIVITY = "broadcast_event";
    ManagerRabbitMQ manage;

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
            Log.i(this.getClass().getSimpleName(), message);

        }
    };

    @Override
    protected void onDestroy(){
        manage.dispose();
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}
