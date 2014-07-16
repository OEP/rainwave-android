package cc.rainwave.android.adapters;

import cc.rainwave.android.R;
import cc.rainwave.android.api.types.Album;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class AlbumListAdapter extends ArrayAdapter<Album> {

    public AlbumListAdapter(Context context, int resource, Album[] objects) {
        super(context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Album a = getItem(position);
        if(a.isCooling()) {
            v.setBackgroundResource(R.drawable.gradient_cooldown);
        }
        else {
            v.setBackgroundResource(0);
        }
        return v;
    }
}
