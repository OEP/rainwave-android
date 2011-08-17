package cc.rainwave.android.adapters;

import cc.rainwave.android.R;
import cc.rainwave.android.api.types.Station;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationListAdapter extends BaseAdapter {
	
	private Station mStations[];
	
	private Context mContext;
	
	public StationListAdapter(Context ctx, Station stations[]) {
		mContext = ctx;
		mStations = stations;
	}

	@Override
	public int getCount() {
		return (mStations == null) ? 0 : mStations.length;
	}

	@Override
	public Object getItem(int position) {
		return mStations[position];
	}

	@Override
	public long getItemId(int position) {
		return ((Station) getItem(position)).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_station, null);
			
			TextView line1 = (TextView) convertView.findViewById(R.id.station_title);
			TextView line2 = (TextView) convertView.findViewById(R.id.station_description);
			Station s = (Station) getItem(position);
			
			line1.setText(s.name);
			line2.setText(s.description);
		}
		
		return convertView;
	}

}
