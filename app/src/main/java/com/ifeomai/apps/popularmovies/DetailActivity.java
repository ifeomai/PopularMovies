package com.ifeomai.apps.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private Movie movie;
    private TextView mOverviewDisplay;
    private TextView mTitle;
    private ImageView mPoster;
    private TextView mRelease;
    private TextView mRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mRelease = findViewById(R.id.tv_release_date);
        mOverviewDisplay = findViewById(R.id.tv_display_movie_detail);
        mRating = findViewById(R.id.tv_rating);
        mPoster =  findViewById(R.id.iv_detail_poster);
        mTitle =  findViewById(R.id.tv_display_movie_title);

        Intent intentStartActivity = getIntent();

        if (intentStartActivity != null) {
            if (intentStartActivity.hasExtra("Movie")) {
                movie = (Movie) intentStartActivity.getSerializableExtra("Movie");
                mOverviewDisplay.setText(movie.mOverview);
                mTitle.setText(movie.mTitle);
                mRating.setText(movie.mUserRating);
                mRelease.setText(movie.mReleaseDate);

                Context context = this;
                Picasso.with(context).load(movie.mPosterURL).into(mPoster);
            }
        }
    }

}
