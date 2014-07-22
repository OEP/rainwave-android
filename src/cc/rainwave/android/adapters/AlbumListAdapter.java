package cc.rainwave.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cc.rainwave.android.R;
import cc.rainwave.android.api.types.Album;

public class AlbumListAdapter extends FilterableAdapter<Album> {

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
