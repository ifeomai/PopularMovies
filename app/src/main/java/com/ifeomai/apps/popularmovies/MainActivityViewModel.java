package com.ifeomai.apps.popularmovies;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import android.os.AsyncTask;


import com.ifeomai.apps.popularmovies.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<List<Movie>> moviesCollection;

    public MutableLiveData<List<Movie>> getMoviesCollection() {
        if (moviesCollection == null){
            moviesCollection = new MutableLiveData<>();
        }
        return moviesCollection;
    }


    public MainActivityViewModel() {
        new GetMoviesAsync().execute(NetworkUtils.SortOrder.POPULAR);
        //moviesCollection.setValue(NetworkUtils.getMovies(NetworkUtils.SortOrder.POPULAR));
    }



    private class GetMoviesAsync extends AsyncTask<NetworkUtils.SortOrder, Void, List<Movie>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Movie> doInBackground(NetworkUtils.SortOrder... sortOrder) {
            NetworkUtils.SortOrder option = sortOrder[0];
            return NetworkUtils.getMovies(option);
        }

        @Override
        protected void onPostExecute(List<Movie> maps) {
            //super.onPostExecute(maps);
            moviesCollection.setValue(maps);
        }
    }


}


