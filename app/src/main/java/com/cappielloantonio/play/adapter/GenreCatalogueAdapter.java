package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.model.Genre;

import java.util.List;

public class GenreCatalogueAdapter extends RecyclerView.Adapter<GenreCatalogueAdapter.ViewHolder> {
    private static final String TAG = "GenreCatalogueAdapter";

    private List<Genre> genres;
    private LayoutInflater mInflater;
    private Context context;
    private ItemClickListener itemClickListener;

    public GenreCatalogueAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_library_catalogue_genre, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Genre genre = genres.get(position);

        holder.textGenre.setText(genre.getName());
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textGenre;

        ViewHolder(View itemView) {
            super(itemView);

            textGenre = itemView.findViewById(R.id.genre_label);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public Genre getItem(int position) {
        return genres.get(position);
    }

    public void setItems(List<Genre> genres) {
        this.genres = genres;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
