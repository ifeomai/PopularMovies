package com.ifeomai.apps.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ifeomai.apps.popularmovies.Utils.NetworkUtils;
import com.ifeomai.apps.popularmovies.Utils.Review;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mOverviewDisplay;
    private TextView mTitle;
    private ImageView mPoster;
    private TextView mRelease;
    private TextView mRating;
    private Movie movie;
    private LinearLayout mRootLinearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mRelease = findViewById(R.id.tv_release_date);
        mOverviewDisplay = findViewById(R.id.tv_display_movie_detail);
        mRating = findViewById(R.id.tv_rating);
        mPoster =  findViewById(R.id.iv_detail_poster);
        mTitle =  findViewById(R.id.tv_display_movie_title);
        mRootLinearLayout = findViewById(R.id.ll_root);

        setupUI();
    }

    private void setupUI(){
        Intent intentStartActivity = getIntent();

        if (intentStartActivity != null) {
            Bundle extras = intentStartActivity.getExtras();
            if (extras != null && intentStartActivity.hasExtra("Movie")) {
                movie = (Movie) intentStartActivity.getSerializableExtra("Movie");
                mOverviewDisplay.setText(movie.mOverview);
                mTitle.setText(movie.mTitle);
                mRating.setText(movie.mUserRating);
                mRelease.setText(movie.mReleaseDate);

                Context context = this;
                Picasso.with(context).load(movie.mPosterURL).into(mPoster);

                //Load reviews
                new FetchMovieDetailsAsync(this).execute();
            }
        }
    }


    @Override
    public void onClick(View v) {

    }

    static class FetchMovieDetailsAsync extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = "FetchMovieDetails";

        JSONArray reviews;
        int review_count;

        private final WeakReference<DetailActivity> activityReference;

        // only retain a weak reference to the activity
        FetchMovieDetailsAsync(DetailActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected Void doInBackground(Void... params) {
            // get a reference to the activity if it is still there
            DetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            //get reviews
            Review returnedReviews = NetworkUtils.getReviews(activity.movie.mMovieId);
            reviews = returnedReviews.mReviewArray;
            review_count = returnedReviews.mReviewCount;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // get a reference to the activity if it is still there
            DetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Ensure there is at least one review
            if (review_count != 0) {

                activity.mRootLinearLayout.addView(createLineView());

                TextView header = new TextView(activity);
                LinearLayout.LayoutParams header_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                header_params.setMargins(30, 10, 20, 20);
                header.setLayoutParams(header_params);
                header.setText(R.string.reviews);
                header.setTextSize(25);
                header.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                activity.mRootLinearLayout.addView(header);

                for (int i = 0; i < review_count; i++) {
                    TextView tv = new TextView(activity);
                    LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv_params.setMargins(30, 10, 20, 20);
                    tv.setLayoutParams(tv_params);
                    tv.setTextColor(activity.getResources().getColor(R.color.black));
                    try {
                        String review = reviews.getJSONObject(i).getString("content");
                        tv.setText(review);
                        activity.mRootLinearLayout.addView(tv);
                        activity.mRootLinearLayout.addView(createLineView());
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSON Error", e);
                    }
                }
            }

        }


        public View createLineView() {
            // get a reference to the activity if it is still there
            DetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            View v = new View(activity);
            v.setBackgroundColor(activity.getResources().getColor(R.color.black));
            LinearLayout.LayoutParams v_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    3);
            v_params.topMargin = 30;
            v_params.bottomMargin = 30;
            v.setLayoutParams(v_params);
            return v;
        }
    }
}
