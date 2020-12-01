package com.cappielloantonio.play.ui.fragment.bottomsheetdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.model.Album;
import com.cappielloantonio.play.model.Artist;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.viewmodel.SongBottomSheetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SongBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private static final String TAG = "SongBottomSheetDialog";

    private SongBottomSheetViewModel songBottomSheetViewModel;
    private Song song;

    private ImageView coverSong;
    private TextView titleSong;
    private TextView artistSong;
    private ToggleButton thumbToggle;

    private TextView playRadio;
    private TextView playNext;
    private TextView addToQueue;
    private TextView Download;
    private TextView addToPlaylist;
    private TextView goToAlbum;
    private TextView goToArtist;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_song_dialog, container, false);

        song = this.getArguments().getParcelable("song_object");

        songBottomSheetViewModel = new ViewModelProvider(requireActivity()).get(SongBottomSheetViewModel.class);
        songBottomSheetViewModel.setSong(song);

        init(view);

        return view;
    }

    private void init(View view) {
        coverSong = view.findViewById(R.id.song_cover_image_view);
        CustomGlideRequest.Builder
                .from(requireContext(), songBottomSheetViewModel.getSong().getPrimary(), songBottomSheetViewModel.getSong().getBlurHash(), CustomGlideRequest.PRIMARY, CustomGlideRequest.TOP_QUALITY)
                .build()
                .into(coverSong);

        titleSong = view.findViewById(R.id.song_title_text_view);
        titleSong.setText(songBottomSheetViewModel.getSong().getTitle());

        artistSong = view.findViewById(R.id.song_artist_text_view);
        artistSong.setText(songBottomSheetViewModel.getSong().getArtistName());

        thumbToggle = view.findViewById(R.id.button_favorite);
        thumbToggle.setChecked(songBottomSheetViewModel.getSong().isFavorite());
        thumbToggle.setOnClickListener(v -> {
            songBottomSheetViewModel.setFavorite();
            dismissBottomSheet();
        });

        playRadio = view.findViewById(R.id.play_radio_text_view);
        playRadio.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Play radio", Toast.LENGTH_SHORT).show();
            dismissBottomSheet();
        });

        playNext = view.findViewById(R.id.play_next_text_view);
        playNext.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Play next", Toast.LENGTH_SHORT).show();
            dismissBottomSheet();
        });

        addToQueue = view.findViewById(R.id.add_to_queue_text_view);
        addToQueue.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Add to queue", Toast.LENGTH_SHORT).show();
            dismissBottomSheet();
        });

        Download = view.findViewById(R.id.download_text_view);
        Download.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Download", Toast.LENGTH_SHORT).show();
            dismissBottomSheet();
        });

        addToPlaylist = view.findViewById(R.id.add_to_playlist_text_view);
        addToPlaylist.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Add to playlist", Toast.LENGTH_SHORT).show();
            dismissBottomSheet();
        });

        goToAlbum = view.findViewById(R.id.go_to_album_text_view);
        goToAlbum.setOnClickListener(v -> {
            Album album = songBottomSheetViewModel.getAlbum();

            if(album != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("album_object", album);
                NavHostFragment.findNavController(this).navigate(R.id.albumPageFragment, bundle);
            }
            else Toast.makeText(requireContext(), "Error retrieving album", Toast.LENGTH_SHORT).show();

            dismissBottomSheet();
        });

        goToArtist = view.findViewById(R.id.go_to_artist_text_view);
        goToArtist.setOnClickListener(v -> {
            Artist artist = songBottomSheetViewModel.getArtist();
            if(artist != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("artist_object", artist);
                NavHostFragment.findNavController(this).navigate(R.id.artistPageFragment, bundle);
            }
            else Toast.makeText(requireContext(), "Error retrieving artist", Toast.LENGTH_SHORT).show();

            dismissBottomSheet();
        });
    }

    @Override
    public void onClick(View v) {
        dismissBottomSheet();
    }

    private void dismissBottomSheet() {
        dismiss();
    }
}