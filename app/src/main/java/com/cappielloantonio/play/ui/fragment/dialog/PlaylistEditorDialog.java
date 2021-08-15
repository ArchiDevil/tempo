package com.cappielloantonio.play.ui.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.R;
import com.cappielloantonio.play.adapter.PlaylistDialogSongHorizontalAdapter;
import com.cappielloantonio.play.databinding.DialogPlaylistEditorBinding;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;
import com.cappielloantonio.play.viewmodel.PlaylistEditorViewModel;

import java.util.Collections;
import java.util.Objects;

public class PlaylistEditorDialog extends DialogFragment {
    private static final String TAG = "ServerSignupDialog";

    private DialogPlaylistEditorBinding bind;
    private PlaylistEditorViewModel playlistEditorViewModel;

    private String playlistName;
    private PlaylistDialogSongHorizontalAdapter playlistDialogSongHorizontalAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bind = DialogPlaylistEditorBinding.inflate(LayoutInflater.from(requireContext()));
        playlistEditorViewModel = new ViewModelProvider(requireActivity()).get(PlaylistEditorViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);

        builder.setView(bind.getRoot())
                .setTitle("Create playlist")
                .setPositiveButton("Save", (dialog, id) -> {
                })
                .setNeutralButton("Delete", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        setParameterInfo();
        setButtonAction();
        initSongsView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void setParameterInfo() {
        if (getArguments() != null) {
            if (getArguments().getParcelable("song_object") != null) {
                playlistEditorViewModel.setSongToAdd(getArguments().getParcelable("song_object"));
                playlistEditorViewModel.setPlaylistToEdit(null);
            }
            else if (getArguments().getParcelable("playlist_object") != null) {
                playlistEditorViewModel.setSongToAdd(null);
                playlistEditorViewModel.setPlaylistToEdit(getArguments().getParcelable("playlist_object"));

                if (playlistEditorViewModel.getPlaylistToEdit() != null) {
                    bind.playlistNameTextView.setText(MusicUtil.getReadableString(playlistEditorViewModel.getPlaylistToEdit().getName()));
                }
            }
        } else {
            playlistEditorViewModel.setSongToAdd(null);
            playlistEditorViewModel.setPlaylistToEdit(null);
        }
    }

    private void setButtonAction() {
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAccent, null));
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent, null));
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent, null));

        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInput()) {
                if (playlistEditorViewModel.getSongToAdd() != null) {
                    playlistEditorViewModel.createPlaylist(playlistName);
                } else if (playlistEditorViewModel.getPlaylistToEdit() != null) {
                    playlistEditorViewModel.updatePlaylist(playlistName);
                }

                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            playlistEditorViewModel.deletePlaylist();
            Objects.requireNonNull(getDialog()).dismiss();
        });
    }

    private void initSongsView() {
        bind.playlistSongRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.playlistSongRecyclerView.setHasFixedSize(true);

        playlistDialogSongHorizontalAdapter = new PlaylistDialogSongHorizontalAdapter(requireContext());
        bind.playlistSongRecyclerView.setAdapter(playlistDialogSongHorizontalAdapter);

        playlistEditorViewModel.getPlaylistSongLiveList().observe(requireActivity(), songs -> {
            playlistDialogSongHorizontalAdapter.setItems(songs);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            int originalPosition = -1;
            int fromPosition = -1;
            int toPosition = -1;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if (originalPosition == -1)
                    originalPosition = viewHolder.getBindingAdapterPosition();

                fromPosition = viewHolder.getBindingAdapterPosition();
                toPosition = target.getBindingAdapterPosition();

                Collections.swap(playlistDialogSongHorizontalAdapter.getItems(), fromPosition, toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

                return false;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                /*
                 * Qui vado a riscivere tutta la table Queue, quando teoricamente potrei solo swappare l'ordine degli elementi interessati
                 * Nel caso la coda contenesse parecchi brani, potrebbero verificarsi rallentamenti pesanti
                 */
                playlistEditorViewModel.orderPlaylistSongLiveListAfterSwap(playlistDialogSongHorizontalAdapter.getItems());

                originalPosition = -1;
                fromPosition = -1;
                toPosition = -1;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                playlistEditorViewModel.removeFromPlaylistSongLiveList(viewHolder.getBindingAdapterPosition());
                bind.playlistSongRecyclerView.getAdapter().notifyItemRemoved(viewHolder.getBindingAdapterPosition());
            }
        }
        ).attachToRecyclerView(bind.playlistSongRecyclerView);
    }

    private boolean validateInput() {
        playlistName = bind.playlistNameTextView.getText().toString().trim();

        if (TextUtils.isEmpty(playlistName)) {
            bind.playlistNameTextView.setError("Required");
            return false;
        }

        return true;
    }
}
