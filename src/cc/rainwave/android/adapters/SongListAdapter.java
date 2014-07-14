/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cc.rainwave.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cc.rainwave.android.R;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.views.CountdownView;

public class SongListAdapter extends BaseAdapter {
    private static final String TAG = "SongListAdapter";

    private boolean mVoted = false;

    private ArrayList<View> mViews;

    private ArrayList<Song> mSongs;

    private Context mContext;

    /** Records last known election id vote. Set to -1 to imply no vote. */
    private int mLastVote = -1;

    /** Vote deadline */
    private long mDeadline = -1;

    /** Item XML ID */
    private int mItemLayout;

    public SongListAdapter(Context ctx, int resId, ArrayList<Song> songs) {
        mContext = ctx;
        mSongs = songs;
        mViews = new ArrayList<View>();
        for(int i = 0; i < mSongs.size(); i++) {
            mViews.add(null);
        }
        mItemLayout = resId;
    }

    public boolean rushVotes() {
        long utc = System.currentTimeMillis() / 1000;
        return mDeadline > 0 && (mDeadline - utc) <= 15;
    }

    public boolean hasVoted() {
        return mVoted;
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    @Override
    public int getCount() {
        return (mSongs == null) ? 0 : mSongs.size();
    }

    @Override
    public Object getItem(int i) {
        return (mSongs == null) ? null : mSongs.get(i);
    }

    public Song getSong(int i) {
        return (mSongs == null) ? null : mSongs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return (mSongs == null) ? -1 : mSongs.get(i).getId();
    }

    /**
     * Update the election list to reflect that the given election entry id
     * has been voted for. This does a linear search and updates all of the
     * items in the list as necessary. This does update the UI as well.
     * 
     * @param elec_entry_id the election id of the last known vote
     */
    public void resyncVoteState(int elec_entry_id) {
        for(int i = 0; i < mSongs.size(); i++) {
            Song s = mSongs.get(i);
            if(s.getElectionEntryId() == elec_entry_id) {
                setVoted(i);
            }
            else {
                revert(i);
            }
        }
    }

    /**
     * Search the current songs and accept the election entry ID if it
     * corresponds to one in memory. This does not alter the GUI so it
     * is safe to call before the views have been inflated.
     * 
     * @param elec_entry_id election entry id to search for
     */
    public void updateVoteState(int elec_entry_id) {
        for(int i = 0; i < mSongs.size(); i++) {
            Song s = mSongs.get(i);
            if(s.getElectionEntryId() == elec_entry_id) {
                mLastVote = elec_entry_id;
                setVoteStatus(true);
                return;
            }
        }
    }

    public void setDeadline(long utc) {
        mDeadline = utc;
    }

    private void setVoteStatus(boolean state) {
        mVoted = state; 
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if(mViews.get(i) == null || convertView == null) {
            Song s = mSongs.get(i);

            Resources r = mContext.getResources();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mItemLayout, null);
            mViews.set(i, convertView);

            setTextIfExists(convertView, R.id.song, s.getTitle());
            setTextIfExists(convertView, R.id.album, s.getDefaultAlbum().getName());
            setTextIfExists(convertView, R.id.artist, s.collapseArtists());

            if(s.isRequest()) {
                setImageIfExists(convertView, R.id.accent, R.drawable.accent_song_hilight);
                setVisibilityIfExists(convertView, R.id.requestor, View.VISIBLE);
                setTextIfExists(convertView, R.id.requestor,
                        String.format(r.getString(R.string.label_requestor), s.getRequestor()));
            }

            if(s.getElectionEntryId() == mLastVote) {
                setVoted(((CountdownView)convertView.findViewById(R.id.circle)));
            }
            else {
                revert(((CountdownView)convertView.findViewById(R.id.circle)), s);
            }
        }

        return convertView;
    }

    /**
     * Sets the visibility if we found the View.
     */
    private void setVisibilityIfExists(View parent, int resId, int visibility) {
        if(parent == null) return;
        View v = parent.findViewById(resId);
        if(v == null) return;
        v.setVisibility(visibility);
    }

    /**
     * Attempts to find the provided view ID and sets
     * the image source if it exists and is an ImageView.
     */
    private void setImageIfExists(View parent, int resId, int picId) {
        if(parent == null) return;
        View v = parent.findViewById(resId);
        if(v == null || !(v instanceof ImageView)) return;
        ImageView iv = (ImageView) v;
        iv.setImageResource(picId);
    }

    /**
     * Attempts to find the provided view ID and sets
     * the text if it exists and is a TextView.
     * @param parent, context for findViewById
     * @param resId, the ID to find
     * @param s, the string to set
     */
    private void setTextIfExists(View parent, int resId, String s){
        if(parent == null) return;
        if(s == null) s = "";
        View v = parent.findViewById(resId);
        if(v == null || !(v instanceof TextView)) return;
        TextView tv = (TextView) v;

        tv.setText(s);
    }

    private CountdownView getCountdownView(int i) {
        return (CountdownView) mViews.get(i).findViewById(R.id.circle);
    }

    /**
     * Revert the i-th item to its default state.
     * 
     * @param i the index of the item to revert
     */
    public void revert(int i) {
        Song song = getSong(i);
        CountdownView view = getCountdownView(i);
        revert(view, song);
    }

    /** Make the CountdownView reflect the given song. */
    private void revert(CountdownView view, Song song) {
        view.setBoth(song.getUserRating(), song.getCommunityRating());
        view.setAlternateText(R.string.label_unrated);
    }

    /**
     * Mark the i-th item as being voted for.
     * 
     * @param i index of item for which we are voting
     */
    public void setVoting(int i) {
        CountdownView cnt = getCountdownView(i);
        cnt.setBoth(0, 0);
        cnt.setAlternateText(R.string.label_voting);
    }

    /**
     * Mark the i-th item as the current vote selection.
     * 
     * @param i index of the item for which we have voted
     */
    public void setVoted(int i) {
        setVoted( getCountdownView(i) );
    }

    private void setVoted(CountdownView view) {
        if(view == null) return;
        view.setBoth(0, 0);
        view.setAlternateText(R.string.label_voted);
    }

    public ArrayList<Song> moveSong(int from, int to) {
        Song s = mSongs.remove(from);
        mSongs.add(to, s);
        notifyDataSetChanged();
        return mSongs;
    }

    public Song removeSong(int which) {
        Song s = mSongs.remove(which);
        mViews.remove(which);
        notifyDataSetChanged();
        return s;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for(int i = 0; i < mViews.size(); i++) {
            mViews.set(i, null);
        }
    }
}
