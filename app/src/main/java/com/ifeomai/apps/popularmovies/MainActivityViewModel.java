package com.ifeomai.apps.popularmovies;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import android.os.AsyncTask;


import com.ifeomai.apps.popularmovies.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<List<Map<String,String>>> moviesCollection;

    public MutableLiveData<List<Map<String, String>>> getMoviesCollection() {
        if (moviesCollection == null){
            moviesCollection = new MutableLiveData<>();
        }
        return moviesCollection;
    }


    public MainActivityViewModel() {
        new GetMoviesAsync().execute();
        //moviesCollection.setValue(NetworkUtils.getMovies(NetworkUtils.SortOrder.POPULAR));
    }



    private class GetMoviesAsync extends AsyncTask<NetworkUtils.SortOrder, Void, List<Map<String,String>>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Map<String, String>> doInBackground(NetworkUtils.SortOrder... sortOrder) {
            return NetworkUtils.getMovies(NetworkUtils.SortOrder.POPULAR);
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> maps) {
            //super.onPostExecute(maps);
            moviesCollection.setValue(maps);
        }
    }


}


