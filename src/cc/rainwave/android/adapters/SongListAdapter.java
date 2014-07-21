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

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cc.rainwave.android.R;
import cc.rainwave.android.Utility;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.views.CountdownView;

public class SongListAdapter extends ArrayAdapter<Song> {
    private static final String TAG = "SongListAdapter";

    private boolean mVoted = false;

    private boolean mShowAlbum = true;

    private boolean mShowArtist = true;

    private boolean mShowRequest = true;

    private boolean mShowRating = true;

    private boolean mShowTime = false;

    private boolean mShowCooldown = false;

    private SparseArray<String> mStatusLabel = new SparseArray<String>();

    /** Vote deadline */
    private long mDeadline = -1;

    /** Item XML ID */
    private int mItemLayout;

    public SongListAdapter(Context ctx, int resId, Song[] songs) {
        this(ctx, resId, Arrays.asList(songs));
    }

    public SongListAdapter(Context ctx, int resId, List<Song> songs) {
        super(ctx, resId, songs);
        mItemLayout = resId;
    }

    public boolean rushVotes() {
        long utc = System.currentTimeMillis() / 1000;
        return mDeadline > 0 && (mDeadline - utc) <= 15;
    }

    public boolean hasVoted() {
        return mVoted;
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).getId();
    }

    public void setShowAlbum(boolean showAlbum) {
        mShowAlbum = showAlbum;
    }

    public void setShowArtist(boolean showArtist) {
        mShowArtist = showArtist;
    }

    public void setShowRequest(boolean showRequest) {
        mShowRequest = showRequest;
    }

    public void setShowRating(boolean showRating) {
        mShowRating = showRating;
    }

    public void setShowCooldown(boolean showCooldown) {
        mShowCooldown = showCooldown;
    }

    public void setShowTime(boolean showTime) {
        mShowTime = showTime;
    }

    public void setStatusLabel(int id, int resId) {
        setStatusLabel(id,  getContext().getResources().getString(resId));
    }

    public void setStatusLabel(int id, String label) {
        mStatusLabel.put(id, label);
    }

    public void clearStatusLabels() {
        mStatusLabel.clear();
    }

    public void setDeadline(long utc) {
        mDeadline = utc;
    }

    private void setVoteStatus(boolean state) {
        mVoted = state;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        Song s = getItem(i);
        Holder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mItemLayout, null);

            holder = new Holder();
            holder.accent = convertView.findViewById(R.id.accent);
            holder.album = convertView.findViewById(R.id.album);
            holder.artist = convertView.findViewById(R.id.artist);
            holder.cooldown = convertView.findViewById(R.id.cooldown);
            holder.rating = convertView.findViewById(R.id.circle);
            holder.requestor = convertView.findViewById(R.id.requestor);
            holder.song = convertView.findViewById(R.id.song);
            holder.time = convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        }
        else {
            holder = (Holder) convertView.getTag();
        }

        ((TextView) holder.song).setText(s.getTitle());

        if(mShowAlbum) {
            Album album = s.getDefaultAlbum();
            ((TextView) holder.album).setText(album.getName());
        }
        else {
            if(holder.album != null) {
                holder.album.setVisibility(View.GONE);
            }
        }

        if(mShowArtist) {
            ((TextView) holder.artist).setText(s.collapseArtists());
        }
        else {
            if(holder.artist != null) {
                holder.artist.setVisibility(View.GONE);
            }
        }

        if (mShowRequest) {
            holder.accent = convertView.findViewById(R.id.accent);
            if(holder.accent != null) {
                int resId = s.isRequest() ? R.drawable.accent_song_hilight : R.drawable.accent_song;
                ((ImageView) holder.accent).setBackgroundResource(resId);
            }

            holder.requestor = convertView.findViewById(R.id.requestor);
            holder.requestor.setVisibility(s.isRequest() ? View.VISIBLE : View.GONE);

            if(s.isRequest()) {
                Resources r = getContext().getResources();
                String requestorLabel = String.format(r.getString(R.string.label_requestor), s.getRequestor());
                ((TextView) holder.requestor).setText(requestorLabel);
            }
        }
        else {
            if(holder.accent != null) {
                holder.accent.setVisibility(View.GONE);
            }

            holder.requestor = convertView.findViewById(R.id.requestor);
            if(holder.requestor != null) {
                holder.requestor.setVisibility(View.GONE);
            }
        }

        if(mShowRating) {
            holder.rating.setVisibility(View.VISIBLE);
            ((CountdownView) holder.rating).setPrimary(s.getUserRating());
            ((CountdownView) holder.rating).setSecondary(s.getCommunityRating());

            CountdownView v = (CountdownView) holder.rating;
            String label = mStatusLabel.get(s.getId());
            if (label != null) {
                v.setAlternateText(label);
            }
            else {
                v.setAlternateText(R.string.label_unrated);
            }
        }
        else {
            if(holder.rating != null) {
                holder.rating.setVisibility(View.GONE);
            }
        }

        if(mShowCooldown) {
            if(s.isCooling()) {
                holder.cooldown.setVisibility(View.VISIBLE);
                String cooldown = Utility.getCooldownString(getContext(), s.getCooldown());
                ((TextView) holder.cooldown).setText(cooldown);
                convertView.setBackgroundResource(R.drawable.gradient_cooldown);
            }
            else {
                holder.cooldown.setVisibility(View.GONE);
                convertView.setBackgroundResource(0);
            }
        }
        else {
            if(holder.cooldown != null) {
                holder.cooldown.setVisibility(View.GONE);
            }
        }

        if(mShowTime) {
            ((TextView) convertView.findViewById(R.id.time)).setText(s.getLengthString());
        }
        else {
            if(holder.time != null) {
                holder.time.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    static class Holder {
        View song, artist, album, time, cooldown, rating, accent, requestor; 
    }
}
