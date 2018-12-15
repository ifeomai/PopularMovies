package com.ifeomai.apps.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ifeomai.apps.popularmovies.utils.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener {


    private RecyclerView mRecyclerViewMovies;
    private NetworkUtils.SortOrder mSortOrder;
    private static final String SORT_ORDER = "sort_order";
    private ProgressBar mProgressLoading;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerViewMovies = findViewById(R.id.rv_movies);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message);

        GridLayoutManager layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.cols), GridLayout.VERTICAL,false);
        mRecyclerViewMovies.setLayoutManager(layoutManager);
        mRecyclerViewMovies.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter();
        mMovieAdapter.setClickListener(this);
        mRecyclerViewMovies.setAdapter(mMovieAdapter);
        mProgressLoading =  findViewById(R.id.pb_loading);

        if (savedInstanceState == null) {
            mSortOrder = NetworkUtils.SortOrder.POPULAR;
        } else {
            mSortOrder = (NetworkUtils.SortOrder) savedInstanceState.getSerializable(SORT_ORDER);
        }

        loadMovieData();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SORT_ORDER, mSortOrder);
        super.onSaveInstanceState(outState);
    }

    private void loadMovieData() {
        if (isOnline()){
            showMovieGridView();
            new GetMoviesAsync(this).execute(mSortOrder);
        }
        else {
            showErrorMessage();
        }

    }

    @Override
    public void onItemClick(Movie movie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentStartDetail = new Intent(context, destinationClass);
        intentStartDetail.putExtra("Movie", movie);
        startActivity(intentStartDetail);
    }


    static class GetMoviesAsync extends AsyncTask<NetworkUtils.SortOrder, Void, List<Map<String,String>>> {

        private final WeakReference<FavoriteActivity> activityReference;

        // only retain a weak reference to the activity
        GetMoviesAsync(FavoriteActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // get a reference to the activity if it is still there
            FavoriteActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // modify the activity's UI
            // TextView textView = activity.findViewById(R.id.textview);
            //textView.setText(result);

            activity.mProgressLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Map<String,String>> doInBackground(NetworkUtils.SortOrder... sortOrder) {
            // get a reference to the activity if it is still there
            FavoriteActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;
           // if(activity.mSortOrder == NetworkUtils.SortOrder.FAVORITES) {
                return getFavoriteCollection();
//            }
//            return NetworkUtils.getMovies(sortOrder[0]);
        }

        @Override
        protected void onPostExecute(List<Map<String,String>> moviesCollection) {

            // get a reference to the activity if it is still there
            FavoriteActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mProgressLoading.setVisibility(View.INVISIBLE);
            if (moviesCollection != null) {
                activity.showMovieGridView();
                List<Movie> movieData = Movie.createMovies(moviesCollection);
                activity.mMovieAdapter.setMovieData(movieData);
            } else {
                activity.showErrorMessage();
            }
        }

        List<Map<String, String>> getFavoriteCollection(){
            // get a reference to the activity if it is still there
            FavoriteActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            List<Map<String,String>> movieCollection = new ArrayList<>();

            Uri favorites = Uri.parse("content://com.ifeomai.apps.popularmovies/favorites");
            Cursor c = activity.getContentResolver().query(favorites, null, null, null, "_id");
            try{
                if (c.getCount() == 0){
                    return null;
                }
                if(c.moveToFirst()) {
                    do {
                        Map<String, String> mapMovieData = new HashMap<>();
                        mapMovieData.put("rating", c.getString(c.getColumnIndex(FavoritesProvider.USER_RATING)));
                        mapMovieData.put("poster", c.getString(c.getColumnIndex(FavoritesProvider.POSTER_URL)));
                        mapMovieData.put("title", c.getString(c.getColumnIndex(FavoritesProvider.TITLE)));
                        mapMovieData.put("releaseDate", c.getString(c.getColumnIndex(FavoritesProvider.RELEASE_DATE)));
                        mapMovieData.put("overview", c.getString(c.getColumnIndex(FavoritesProvider.SYNOPSIS)));
                        mapMovieData.put("id", c.getString(c.getColumnIndex(FavoritesProvider._ID)));

                        movieCollection.add(mapMovieData);

                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }

            return movieCollection;
        }
    }

    private void showMovieGridView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerViewMovies.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerViewMovies.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_popular: {
                startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                break;
            }
            case R.id.action_sort_rating: {
                startActivity(new Intent(FavoriteActivity.this, RatingsActivity.class));
                break;
            }
            case R.id.action_show_favorites: {
                mSortOrder = NetworkUtils.SortOrder.FAVORITES;
                Context context = this;
                Toast.makeText(context, getString(R.string.show_favorites_toast), Toast.LENGTH_SHORT)
                        .show();
                loadMovieData();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isOnline() {
        Context context = this;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
