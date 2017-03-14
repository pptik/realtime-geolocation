package pptik.org.realtimelocation.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import pptik.org.realtimelocation.R;
import pptik.org.realtimelocation.models.Tracker;

public class ListAdapter extends BaseAdapter {

    private Context mContext = null;
    private Tracker[] trackers;
    private LayoutInflater mInflater = null;
    private TextView gpsNameText;
    private RadioButton stateRadio;
    private int checkedState;

    public interface MarkerPositionListener{
        public void onMarkerSelected(int position);
    }


    MarkerPositionListener listener;
    public ListAdapter(Context mContext, Tracker[] trackers, int checkedState, MarkerPositionListener listener){
        this.mContext = mContext;
        this.trackers = trackers;
        this.mInflater = (LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
        this.checkedState = checkedState;
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
        stateRadio = (RadioButton)view.findViewById(R.id.state);

        boolean state = (i == checkedState) ? true : false;
        String info = trackers[i].getKeterangan();

        gpsNameText.setText(info);
        stateRadio.setChecked(state);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listener.onMarkerSelected(i);

            }
        });

        stateRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listener.onMarkerSelected(i);
            }
        });

        return view;
    }
}
