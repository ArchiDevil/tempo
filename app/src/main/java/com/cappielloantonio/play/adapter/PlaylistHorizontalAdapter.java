package com.cappielloantonio.play.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.databinding.ItemHorizontalPlaylistBinding;
import com.cappielloantonio.play.interfaces.ClickCallback;
import com.cappielloantonio.play.subsonic.models.Playlist;
import com.cappielloantonio.play.util.MusicUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlaylistHorizontalAdapter extends RecyclerView.Adapter<PlaylistHorizontalAdapter.ViewHolder> implements Filterable {
    private final ClickCallback click;

    private List<Playlist> playlists;
    private List<Playlist> playlistsFull;

    private final Filter filtering = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Playlist> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(playlistsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Playlist item : playlistsFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            playlists.clear();
            playlists.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public PlaylistHorizontalAdapter(ClickCallback click) {
        this.click = click;
        this.playlists = Collections.emptyList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHorizontalPlaylistBinding view = ItemHorizontalPlaylistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        holder.item.playlistDialogTitleTextView.setText(MusicUtil.getReadableString(playlist.getName()));
        holder.item.playlistDialogCountTextView.setText(holder.itemView.getContext().getString(R.string.playlist_counted_tracks, playlist.getSongCount()));
        holder.item.playlistDialogDurationTextView.setText(MusicUtil.getReadableDurationString(playlist.getDuration(), false));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public Playlist getItem(int id) {
        return playlists.get(id);
    }

    public void setItems(List<Playlist> playlists) {
        this.playlists = playlists;
        this.playlistsFull = new ArrayList<>(playlists);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filtering;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemHorizontalPlaylistBinding item;

        ViewHolder(ItemHorizontalPlaylistBinding item) {
            super(item.getRoot());

            this.item = item;

            item.playlistDialogTitleTextView.setSelected(true);

            itemView.setOnClickListener(v -> onClick());
            itemView.setOnLongClickListener(v -> onLongClick());
        }

        public void onClick() {
            Bundle bundle = new Bundle();
            bundle.putParcelable("playlist_object", playlists.get(getBindingAdapterPosition()));

            click.onPlaylistClick(bundle);
        }

        public boolean onLongClick() {
            Bundle bundle = new Bundle();
            bundle.putParcelable("playlist_object", playlists.get(getBindingAdapterPosition()));

            click.onPlaylistLongClick(bundle);

            return false;
        }
    }

    public void sort(String order) {
        switch (order) {
            case com.cappielloantonio.play.model.Playlist.ORDER_BY_NAME:
                playlists.sort(Comparator.comparing(Playlist::getName));
                break;
            case com.cappielloantonio.play.model.Playlist.ORDER_BY_RANDOM:
                Collections.shuffle(playlists);
                break;
        }

        notifyDataSetChanged();
    }
}
