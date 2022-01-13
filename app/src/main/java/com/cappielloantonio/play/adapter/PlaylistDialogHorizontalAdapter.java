package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.model.Playlist;
import com.cappielloantonio.play.ui.dialog.PlaylistChooserDialog;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.PlaylistChooserViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDialogHorizontalAdapter extends RecyclerView.Adapter<PlaylistDialogHorizontalAdapter.ViewHolder> {
    private static final String TAG = "PlaylistDialogHorizontalAdapter";

    private final Context context;
    private final LayoutInflater mInflater;

    private final PlaylistChooserViewModel playlistChooserViewModel;
    private final PlaylistChooserDialog playlistChooserDialog;

    private List<Playlist> playlists;

    public PlaylistDialogHorizontalAdapter(Context context, PlaylistChooserViewModel playlistChooserViewModel, PlaylistChooserDialog playlistChooserDialog) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlists = new ArrayList<>();

        this.playlistChooserViewModel = playlistChooserViewModel;
        this.playlistChooserDialog = playlistChooserDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_horizontal_playlist_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        holder.playlistTitle.setText(MusicUtil.getReadableString(playlist.getName()));
        holder.playlistTrackCount.setText(context.getString(R.string.playlist_counted_tracks, playlist.getSongCount()));
        holder.playlistDuration.setText(MusicUtil.getReadableDurationString(playlist.getDuration(), false));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void setItems(List<Playlist> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    public Playlist getItem(int id) {
        return playlists.get(id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView playlistTitle;
        TextView playlistTrackCount;
        TextView playlistDuration;

        ViewHolder(View itemView) {
            super(itemView);

            playlistTitle = itemView.findViewById(R.id.playlist_dialog_title_text_view);
            playlistTrackCount = itemView.findViewById(R.id.playlist_dialog_count_text_view);
            playlistDuration = itemView.findViewById(R.id.playlist_dialog_duration_text_view);

            itemView.setOnClickListener(this);

            playlistTitle.setSelected(true);
        }

        @Override
        public void onClick(View view) {
            playlistChooserViewModel.addSongToPlaylist(playlists.get(getBindingAdapterPosition()).getId());
            playlistChooserDialog.dismiss();
        }
    }
}
