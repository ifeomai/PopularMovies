package com.ifeomai.apps.popularmovies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.ifeomai.apps.popularmovies.utils.NetworkUtils;

public class MainActivityViewModelFactory implements ViewModelProvider.Factory {
    private NetworkUtils.SortOrder mOptions;

    public  MainActivityViewModelFactory(NetworkUtils.SortOrder options){
        mOptions = options;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
       // return (T) new MainActivityViewModel(mOptions);
        return (T) new MainActivityViewModel();
    }
}
