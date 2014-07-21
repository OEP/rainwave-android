package cc.rainwave.android.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.widget.Filter;

/**
 * This filter uses the toString() method of the input type to do a .contains()
 * to match the search constraint. The result is a more permissive filter that
 * users are more used to seeing.
 * 
 * @param <T>
 *            type of object to filter on
 */
public abstract class SubsequenceFilter<T> extends Filter {

    private boolean mIgnoreCase;

    private List<T> mSearchSet;

    public SubsequenceFilter(boolean ignoreCase, T[] searchSet) {
        this(ignoreCase, Arrays.asList(searchSet));
    }

    public SubsequenceFilter(boolean ignoreCase, List<T> searchSet) {
        mIgnoreCase = ignoreCase;
        mSearchSet = searchSet;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        String text = constraint != null ? constraint.toString() : null;
        FilterResults results = new FilterResults();
        if (text != null && mIgnoreCase) {
            text = text.toLowerCase(Locale.US);
        }
        if (text == null || text.length() == 0) {
            results.values = mSearchSet;
            results.count = mSearchSet.size();
            return results;
        }

        // Do the actual search
        List<T> matchList = new ArrayList<T>();
        for (T item : mSearchSet) {
            String repr = item.toString();
            if (mIgnoreCase) {
                repr = repr.toLowerCase(Locale.US);
            }
            if (repr.contains(constraint)) {
                matchList.add(item);
            }
        }
        results.values = matchList;
        results.count = matchList.size();
        return results;
    }
}
