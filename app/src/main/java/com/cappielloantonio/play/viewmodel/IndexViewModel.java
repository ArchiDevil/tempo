package com.cappielloantonio.play.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.play.repository.DirectoryRepository;
import com.cappielloantonio.play.subsonic.models.Indexes;
import com.cappielloantonio.play.subsonic.models.MusicFolder;

public class IndexViewModel extends AndroidViewModel {
    private final DirectoryRepository directoryRepository;

    private MusicFolder musicFolder;

    private MutableLiveData<Indexes> indexes = new MutableLiveData<>(null);

    public IndexViewModel(@NonNull Application application) {
        super(application);

        directoryRepository = new DirectoryRepository();
    }

    public MutableLiveData<Indexes> getIndexes() {
        return directoryRepository.getIndexes(null, null);
    }

    public String getMusicFolderName() {
        return musicFolder != null ? musicFolder.getName() : "";
    }

    public void setMusicFolder(MusicFolder musicFolder) {
        this.musicFolder = musicFolder;
    }
}
