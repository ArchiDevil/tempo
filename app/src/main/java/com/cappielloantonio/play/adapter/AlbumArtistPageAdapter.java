package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.model.Album;

import java.util.List;

public class AlbumArtistPageAdapter extends RecyclerView.Adapter<AlbumArtistPageAdapter.ViewHolder> {
    private static final String TAG = "AlbumArtistPageAdapter";
    private List<Album> albums;
    private LayoutInflater mInflater;
    private Context context;
    private ItemClickListener itemClickListener;

    public AlbumArtistPageAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.albums = albums;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_artist_page_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.textAlbumName.setText(album.getTitle());

        CustomGlideRequest.Builder
                .from(context, album.getPrimary(), album.getBlurHash(), CustomGlideRequest.PRIMARY)
                .build()
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textAlbumName;
        ImageView cover;

        ViewHolder(View itemView) {
            super(itemView);

            textAlbumName = itemView.findViewById(R.id.album_name_label);
            cover = itemView.findViewById(R.id.artist_page_album_cover_image_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public Album getItem(int position) {
        return albums.get(position);
    }

    public void setItems(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
