package pptik.org.realtimelocation.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import pptik.org.realtimelocation.R;
import pptik.org.realtimelocation.models.Tracker;

public class ListAdapter extends BaseAdapter {

    private Context mContext = null;
    private Tracker[] trackers;
    private LayoutInflater mInflater = null;
    private TextView gpsNameText;

    public ListAdapter(Context mContext, Tracker[] trackers){
        this.mContext = mContext;
        this.trackers = trackers;
        this.mInflater = (LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        if (trackers != null) {
            return trackers.length;
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        view = mInflater.inflate(R.layout.list_item, null);
        gpsNameText = (TextView)view.findViewById(R.id.gps_name);
        gpsNameText.setText(trackers[i].getKeterangan());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Shit", trackers[i].getMac());
            }
        });

        return view;
    }
}
