package cc.rainwave.android.adapters;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import cc.rainwave.android.widget.SubsequenceFilter;

/**
 * This is just a dumb extension of ArrayAdapter to override the default filter
 * with a case-insensitive SubsequenceFilter<T>.
 * 
 * @param <T>
 *            type parameter to pass on to ArrayAdapter
 */
public class FilterableAdapter<T> extends ArrayAdapter<T> {

    private Filter mFilter;

    private List<T> mObjects;

    public FilterableAdapter(Context context, int resource, T[] objects) {
        this(context, resource, Arrays.asList(objects));
    }

    public FilterableAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        mObjects = objects;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    public T getItem(int i) {
        return mObjects.get(i);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SubsequenceFilter<T>(true, mObjects) {
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                        FilterResults results) {
                    mObjects = (List<T>) results.values;
                    if (results.count == 0) {
                        notifyDataSetInvalidated();
                    } else {
                        notifyDataSetChanged();
                    }
                }
            };
        }
        return mFilter;
    }
}
