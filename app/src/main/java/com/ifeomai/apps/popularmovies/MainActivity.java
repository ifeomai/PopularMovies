package com.ifeomai.apps.popularmovies;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener {


    private RecyclerView mRecyclerViewMovies;
    private NetworkUtils.SortOrder mSortOrder;
    private static final String SORT_ORDER = "sort_order";
    private ProgressBar mProgressLoading;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private MainActivityViewModel model;


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
        model = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        loadMovieData();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SORT_ORDER, mSortOrder);
        super.onSaveInstanceState(outState);

    }


    private void loadMovieData() {
        // ViewModel Changes

        model.getMoviesCollection().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                mProgressLoading.setVisibility(View.INVISIBLE);
                if (movies != null  && isOnline()) {
                    showMovieGridView();
                    mMovieAdapter.setMovieData(movies);
                } else {
                    showErrorMessage();
                }

            }
        });
    }


    @Override
    public void onItemClick(Movie movie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentStartDetail = new Intent(context, destinationClass);
        intentStartDetail.putExtra("Movie", movie);
        startActivity(intentStartDetail);
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
                mSortOrder = NetworkUtils.SortOrder.POPULAR;
                Context context = this;
                Toast.makeText(context, getString(R.string.sort_popular_toast), Toast.LENGTH_SHORT)
                        .show();
                loadMovieData();
                return true;
            }
            case R.id.action_sort_rating: {
                startActivity(new Intent(MainActivity.this, RatingsActivity.class));
                break;
            }
            case R.id.action_show_favorites: {
//                mSortOrder = NetworkUtils.SortOrder.FAVORITES;
//                Context context = this;
//                Toast.makeText(context, getString(R.string.show_favorites_toast), Toast.LENGTH_SHORT)
//                        .show();
//                loadMovieData();
//                return true;
                startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                break;
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
