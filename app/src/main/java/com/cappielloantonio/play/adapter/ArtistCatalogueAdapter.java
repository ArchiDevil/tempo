package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArtistCatalogueAdapter extends RecyclerView.Adapter<ArtistCatalogueAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "ArtistCatalogueAdapter";

    private final LayoutInflater inflater;
    private final MainActivity activity;
    private final Context context;
    private final Filter filtering = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Artist> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(artistFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Artist item : artistFull) {
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
            artists.clear();
            artists.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    private List<Artist> artists;
    private List<Artist> artistFull;

    public ArtistCatalogueAdapter(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.artists = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_library_catalogue_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Artist artist = artists.get(position);

        holder.textArtistName.setText(MusicUtil.getReadableString(artist.getName()));

        CustomGlideRequest.Builder
                .from(
                        context,
                        artist.getId(),
                        CustomGlideRequest.ARTIST_PIC,
                        // artist.getImageUrl()
                        null
                )
                .build()
                .transform(new RoundedCorners(CustomGlideRequest.CORNER_RADIUS))
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public Artist getItem(int position) {
        return artists.get(position);
    }

    public void setItems(List<Artist> artists) {
        this.artists = artists;
        this.artistFull = new ArrayList<>(artists);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filtering;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView textArtistName;
        ImageView cover;

        ViewHolder(View itemView) {
            super(itemView);

            textArtistName = itemView.findViewById(R.id.artist_name_label);
            cover = itemView.findViewById(R.id.artist_catalogue_cover_image_view);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            textArtistName.setSelected(true);
        }

        @Override
        public void onClick(View view) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("artist_object", artists.get(getBindingAdapterPosition()));

            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.searchFragment) {
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_artistPageFragment, bundle);
            } else if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.libraryFragment) {
                Navigation.findNavController(view).navigate(R.id.action_libraryFragment_to_artistPageFragment, bundle);
            } else if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.artistCatalogueFragment) {
                Navigation.findNavController(view).navigate(R.id.action_artistCatalogueFragment_to_artistPageFragment, bundle);
            }

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        @Override
        public boolean onLongClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("artist_object", artists.get(getBindingAdapterPosition()));
            Navigation.findNavController(v).navigate(R.id.artistBottomSheetDialog, bundle);
            return true;
        }
    }
}
