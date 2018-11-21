package com.ifeomai.apps.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.ifeomai.apps.popularmovies.Utils.NetworkUtils;
import com.ifeomai.apps.popularmovies.Utils.Review;
import com.ifeomai.apps.popularmovies.Utils.Trailer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

public class DetailActivity extends AppCompatActivity {

    private TextView mOverviewDisplay;
    private TextView mTitle;
    private ImageView mPoster;
    private TextView mRelease;
    private TextView mRating;
    private Movie movie;
    private LinearLayout mRootLinearLayout;
    private String mMovieId ;
    private  String mPosterUrl;

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

        Button mMarkFavoriteButton = findViewById(R.id.markFavorite_button);
        mMarkFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites();
            }
        });
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
                mMovieId = movie.mMovieId;

                Context context = this;
                mPosterUrl = movie.mPosterURL;
                Picasso.with(context).load(movie.mPosterURL).into(mPoster);

                //Load reviews
                new FetchMovieDetailsAsync(this).execute();
            }
        }
    }

//    @Override
//    public void onClick(View v) {
//
//    }

    private void addToFavorites() {
        ContentValues values = new ContentValues();
        values.put(FavoritesProvider._ID, mMovieId);
        values.put(FavoritesProvider.TITLE, mTitle.getText().toString());
        values.put(FavoritesProvider.SYNOPSIS, mOverviewDisplay.getText().toString());
        values.put(FavoritesProvider.USER_RATING, mRating.getText().toString());
        values.put(FavoritesProvider.RELEASE_DATE, mRelease.getText().toString());
        values.put(FavoritesProvider.POSTER_URL,mPosterUrl);
//
//        BitmapDrawable drawable = (BitmapDrawable) poster_image.getDrawable();
//        Bitmap bmp = drawable.getBitmap();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] image = stream.toByteArray();
//        values.put(FavoritesProvider.POSTER, image);

        Uri uri = DetailActivity.this.getContentResolver().insert(FavoritesProvider.CONTENT_URI, values);

        if(uri != null && uri.toString().equals("Duplicate"))
            Toast.makeText(DetailActivity.this, R.string.fav_exists, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DetailActivity.this, R.string.fav_success, Toast.LENGTH_SHORT).show();
    }

    static class FetchMovieDetailsAsync extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = "FetchMovieDetails";

        JSONArray reviews;
        int review_count;

        String[] youtube_ids;
        int trailer_count;

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

            // get Trailer
            Trailer returnedTrailers = NetworkUtils.getTrailers(activity.movie.mMovieId);
            youtube_ids = returnedTrailers != null ? returnedTrailers.mYoutube_ids : new String[0];
            trailer_count = returnedTrailers.mTrailer_count;

            //get reviews
            Review returnedReviews = NetworkUtils.getReviews(activity.movie.mMovieId);
            reviews = returnedReviews != null ? returnedReviews.mReviewArray : null;
            review_count = returnedReviews.mReviewCount;


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // get a reference to the activity if it is still there
            DetailActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Ensure there is at least one trailer
            if (trailer_count != 0) {

                activity.mRootLinearLayout.addView(createLineView());


                for (int i = 0; i < trailer_count; i++) {
                    Button b = new Button(activity);
                    LinearLayout.LayoutParams b_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    b_params.setMargins(30, 10, 20, 20);
                    b.setLayoutParams(b_params);
                    b.setText(String.format("Watch Trailer %s", Integer.toString(i + 1)));
                    b.setId(i + 1001);
                    b.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                    b.setTextColor(activity.getResources().getColor(R.color.white));
                    b.setTextSize(18);
                    b.setPadding(20, 10, 20, 10);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String youtube_id = youtube_ids[view.getId()];
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtube_id));
                                view.getContext().startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + youtube_id));
                                String title = "Watch video via";
                                Intent chooser = Intent.createChooser(intent, title);
                                if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                                    view.getContext().startActivity(chooser);
                                }
                            }

                        }
                    });
                    activity.mRootLinearLayout.addView(b);
                }
            }

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


        View createLineView() {
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
