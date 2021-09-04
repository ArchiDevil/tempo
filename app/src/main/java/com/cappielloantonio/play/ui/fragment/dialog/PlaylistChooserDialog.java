package com.cappielloantonio.play.ui.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.PlaylistHorizontalAdapter;
import com.cappielloantonio.play.databinding.DialogPlaylistChooserBinding;
import com.cappielloantonio.play.viewmodel.PlaylistChooserViewModel;

import java.util.Objects;

public class PlaylistChooserDialog extends DialogFragment {
    private static final String TAG = "ServerSignupDialog";

    private DialogPlaylistChooserBinding bind;
    private PlaylistChooserViewModel playlistChooserViewModel;

    private PlaylistHorizontalAdapter playlistHorizontalAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bind = DialogPlaylistChooserBinding.inflate(LayoutInflater.from(requireContext()));
        playlistChooserViewModel = new ViewModelProvider(requireActivity()).get(PlaylistChooserViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);

        builder.setView(bind.getRoot())
                .setTitle("Add to a playlist")
                .setNeutralButton("Create", (dialog, id) -> {
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        initPlaylistView();
        setSongInfo();
        setButtonAction();
    }

    private void setSongInfo() {
        if (getArguments() != null) {
            playlistChooserViewModel.setSongToAdd(getArguments().getParcelable("song_object"));
        } else {
            playlistChooserViewModel.setSongToAdd(null);
        }
    }

    private void setButtonAction() {
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAccent, null));
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent, null));

        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("song_object", playlistChooserViewModel.getSongToAdd());

            PlaylistEditorDialog dialog = new PlaylistEditorDialog();
            dialog.setArguments(bundle);
            dialog.show(requireActivity().getSupportFragmentManager(), null);

            Objects.requireNonNull(getDialog()).dismiss();
        });
    }

    private void initPlaylistView() {
        bind.playlistDialogRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.playlistDialogRecyclerView.setHasFixedSize(true);

        playlistHorizontalAdapter = new PlaylistHorizontalAdapter(requireContext(), playlistChooserViewModel, this);
        bind.playlistDialogRecyclerView.setAdapter(playlistHorizontalAdapter);

        playlistChooserViewModel.getPlaylistList().observe(requireActivity(), playlists -> {
            if(playlists != null) {
                if (playlists.size() > 0) {
                    if (bind != null) bind.noPlaylistsCreatedTextView.setVisibility(View.GONE);
                    if (bind != null) bind.playlistDialogRecyclerView.setVisibility(View.VISIBLE);
                    playlistHorizontalAdapter.setItems(playlists);
                } else {
                    if (bind != null) bind.noPlaylistsCreatedTextView.setVisibility(View.VISIBLE);
                    if (bind != null) bind.playlistDialogRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}
