package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumHorizontalAdapter extends RecyclerView.Adapter<AlbumHorizontalAdapter.ViewHolder> {
    private static final String TAG = "AlbumHorizontalAdapter";

    private List<Album> albums;
    private final LayoutInflater mInflater;
    private final Context context;

    public AlbumHorizontalAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.albums = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_horizontal_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.albumTitle.setText(MusicUtil.getReadableString(album.getTitle()));
        holder.albumArtist.setText(MusicUtil.getReadableString(album.getArtistName()));

        CustomGlideRequest.Builder
                .from(context, album.getPrimary(), CustomGlideRequest.ALBUM_PIC, null)
                .build()
                .transform(new RoundedCorners(CustomGlideRequest.CORNER_RADIUS))
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void setItems(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    public Album getItem(int id) {
        return albums.get(id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView albumTitle;
        TextView albumArtist;
        ImageView more;
        ImageView cover;

        ViewHolder(View itemView) {
            super(itemView);

            albumTitle = itemView.findViewById(R.id.album_title_text_view);
            albumArtist = itemView.findViewById(R.id.album_artist_text_view);
            more = itemView.findViewById(R.id.album_more_button);
            cover = itemView.findViewById(R.id.album_cover_image_view);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            more.setOnClickListener(this::openMore);

            albumTitle.setSelected(true);
        }

        @Override
        public void onClick(View view) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("album_object", albums.get(getBindingAdapterPosition()));

            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment) {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_albumPageFragment, bundle);
            } else if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.albumListPageFragment) {
                Navigation.findNavController(view).navigate(R.id.action_albumListPageFragment_to_albumPageFragment, bundle);
            } else if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.downloadFragment) {
                Navigation.findNavController(view).navigate(R.id.action_downloadFragment_to_albumPageFragment, bundle);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            openMore(v);
            return true;
        }

        private void openMore(View view) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("album_object", albums.get(getBindingAdapterPosition()));
            Navigation.findNavController(view).navigate(R.id.albumBottomSheetDialog, bundle);
        }
    }
}
