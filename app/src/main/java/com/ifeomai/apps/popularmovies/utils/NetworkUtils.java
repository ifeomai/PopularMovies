package com.ifeomai.apps.popularmovies.utils;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ifeomai.apps.popularmovies.BuildConfig;
import com.ifeomai.apps.popularmovies.FavoritesProvider;
import com.ifeomai.apps.popularmovies.MainActivity;
import com.ifeomai.apps.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String IMAGE_URL_PREFIX = "https://image.tmdb.org/t/p/w185";
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String API_KEY = BuildConfig.MOVIE_API_KEY;
    private final static String LANG_PARAM = "language";
    private final static String LANG = "en-US";
    private final static String PAGE_PARAM = "page";
    private final static String PAGE = "1";
    private final static String KEY_PARAM = "api_key";


    public enum SortOrder {
        POPULAR("popular"), RATING("top_rated"), FAVORITES("favorites");

        private String sortOrderString;

        SortOrder(String s) {
            sortOrderString = s;
        }

        String getSortOrderString() {
            return sortOrderString;
        }
    }

    public static List<Movie> getMovies(SortOrder order) {

        String jsonResponse ;
        try {
            jsonResponse = getResponseFromHttpUrl(buildUrl(order));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("NetTalk","error in accessing network: "+ e.toString());
            return null;
        }

        return Movie.createMovies(parseMovieDbJSON(jsonResponse));
    }
    public static List<Map<String, String>> getFavoriteCollection(Context context){

        List<Map<String,String>> movieFavoriteCollection = new ArrayList<>();

        Uri favorites = Uri.parse("content://com.ifeomai.apps.popularmovies/favorites");
        Cursor c = context.getContentResolver().query(favorites, null, null, null, "_id");
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

                    movieFavoriteCollection.add(mapMovieData);

                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return movieFavoriteCollection;
    }
    private static List<Map<String,String>> parseMovieDbJSON(String jsonString) {

        List<Map<String,String>> movieCollection = new ArrayList<>();
        JSONObject jObject ;

        try {
            Map<String, String> mapMovieData;
            jObject = new JSONObject(jsonString);
            JSONArray ReturnedMovieList = jObject.getJSONArray("results");

            for(int i=0;i<ReturnedMovieList.length();i++){
                JSONObject movieJSON = ReturnedMovieList.getJSONObject(i);

                mapMovieData = new HashMap<>();
                mapMovieData.put("rating",String.valueOf(movieJSON.getDouble("vote_average")));
                mapMovieData.put("poster",IMAGE_URL_PREFIX + movieJSON.getString("poster_path"));
                mapMovieData.put("title",movieJSON.getString("original_title"));
                mapMovieData.put("releaseDate",movieJSON.getString("release_date"));
                mapMovieData.put("overview",movieJSON.getString("overview"));
                mapMovieData.put("id",movieJSON.getString("id"));

                movieCollection.add(mapMovieData);

            }
        }
        catch(Exception e){
            Log.d("NetworkError","something went wrong: "+ e.toString());
            return null;
        }

        return movieCollection;
    }

    private static URL buildUrl(SortOrder order) {

        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL +order.getSortOrderString()).buildUpon()
                .appendQueryParameter(KEY_PARAM, API_KEY)
                .appendQueryParameter(LANG_PARAM, LANG)
                .appendQueryParameter(PAGE_PARAM, PAGE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static Review getReviews(String MovieId){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        //get reviews
        try {
            URL url = new URL(MOVIEDB_BASE_URL + MovieId + "/reviews" + "?api_key=" + API_KEY);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            String reviewJsonStr = buffer.toString();

            JSONObject main = new JSONObject(reviewJsonStr);

            String results = main.getString("results");
            JSONArray reviews = new JSONArray(results);
            int review_count = main.getInt("total_results");

            return new Review(reviews,review_count);

        } catch (Exception e) {
            // Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    public static Trailer getTrailers(String MovieId){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        //get reviews
        try {
            URL url = new URL(MOVIEDB_BASE_URL + MovieId + "/videos" + "?api_key=" + API_KEY);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            String trailerJsonStr = buffer.toString();

            JSONObject main = new JSONObject(trailerJsonStr);

            String results = main.getString("results");
            JSONArray trailers = new JSONArray(results);
            int trailer_count = trailers.length();
            String[] youtube_ids = null ;
            //Ensure there is at least one trailer
            if (trailer_count != 0) {
                youtube_ids = new String[trailer_count];
                for (int i = 0; i < trailer_count; i++) {
                    JSONObject obj = trailers.getJSONObject(i);
                    youtube_ids[i] = obj.getString("key");
                }
            }
            return new Trailer( youtube_ids,trailer_count);


        } catch (Exception e) {
            // Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }
}
