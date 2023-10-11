package com.cappielloantonio.tempo.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.util.UnstableApi;

import com.cappielloantonio.tempo.R;
import com.cappielloantonio.tempo.databinding.DialogDownloadDirectoryBinding;
import com.cappielloantonio.tempo.interfaces.DialogClickCallback;

@OptIn(markerClass = UnstableApi.class)
public class DownloadDirectoryDialog extends DialogFragment {
    private DialogDownloadDirectoryBinding bind;

    private final DialogClickCallback dialogClickCallback;

    public DownloadDirectoryDialog(DialogClickCallback dialogClickCallback) {
        this.dialogClickCallback = dialogClickCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bind = DialogDownloadDirectoryBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(bind.getRoot())
                .setTitle(R.string.download_directory_dialog_title)
                .setPositiveButton(R.string.download_directory_dialog_positive_button, null)
                .setNegativeButton(R.string.download_directory_dialog_negative_button, null);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        setButtonAction();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void setButtonAction() {
        AlertDialog dialog = ((AlertDialog) getDialog());

        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialogClickCallback.onPositiveClick();
                dialog.dismiss();
            });

            Button negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                dialogClickCallback.onNegativeClick();
                dialog.dismiss();
            });
        }
    }
}
