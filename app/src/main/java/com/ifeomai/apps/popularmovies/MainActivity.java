package com.ifeomai.apps.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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

import com.ifeomai.apps.popularmovies.Utils.NetworkUtils;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener {


    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;
    private NetworkUtils.SortOrder mSortOrder;
    private static final String SORT_ORDER = "sort_order";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_movies);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message);

        GridLayoutManager layoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.cols), GridLayout.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter();
        mMovieAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        mLoadingIndicator =  findViewById(R.id.pb_loading);

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
            new FetchMoviesTask().execute(mSortOrder);
        }
        else {
            showErrorMessage();
        }

    }

    @Override
    public void onItemClick(Movie movie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("Movie", movie);
        startActivity(intentToStartDetailActivity);
    }


    public class FetchMoviesTask extends AsyncTask<NetworkUtils.SortOrder, Void, List<Map<String,String>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Map<String,String>> doInBackground(NetworkUtils.SortOrder... sortOrder) {
            return NetworkUtils.getMovies(sortOrder[0]);
        }

        @Override
        protected void onPostExecute(List<Map<String,String>> moviesCollection) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesCollection != null) {
                showMovieGridView();
                List<Movie> movieData = Movie.createMovies(moviesCollection);
                mMovieAdapter.setMovieData(movieData);
            } else {
                showErrorMessage();
            }
        }

    }

    private void showMovieGridView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
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

        if (id == R.id.action_sort_popular) {
            mSortOrder = NetworkUtils.SortOrder.POPULAR;
            Context context = this;
            Toast.makeText(context, getString(R.string.sort_popular_toast), Toast.LENGTH_SHORT)
                    .show();
            loadMovieData();
            return true;
        }
        else if (id == R.id.action_sort_rating) {
            mSortOrder = NetworkUtils.SortOrder.RATING;
            Context context = this;
            Toast.makeText(context, getString(R.string.sort_rating_toast), Toast.LENGTH_SHORT)
                    .show();
            loadMovieData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        Context context = this;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
