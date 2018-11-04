package com.ifeomai.apps.popularmovies.Utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static String IMAGE_URL_PREFIX = "https://image.tmdb.org/t/p/w185";
    private static String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static String API_KEY = "";
    final static String LANG_PARAM = "language";
    final static String LANG = "en-US";
    final static String PAGE_PARAM = "page";
    final static String PAGE = "1";
    final static String KEY_PARAM = "api_key";


    public enum SortOrder {
        POPULAR("popular"), RATING("top_rated");

        private String sortOrderString;

        SortOrder(String s) {
            sortOrderString = s;
        }

        public String getSortOrderString() {
            return sortOrderString;
        }
    }

    public static List<Map<String,String>> getMovies(SortOrder order) {

        String jsonResponse = null;
        try {
            jsonResponse = getResponseFromHttpUrl(buildUrl(order));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("NetTalk","error in accessing network: "+ e.toString());
            return null;
        }

        return parseMovieDbJSON(jsonResponse);
    }

    public static List<Map<String,String>> parseMovieDbJSON(String jsonString) {

        List<Map<String,String>> movieCollection = new ArrayList<>();
        JSONObject jObject = null;

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

                movieCollection.add(mapMovieData);

            }
        }
        catch(Exception e){
            Log.d("NetworkError","something went wrong: "+ e.toString());
            return null;
        }

        return movieCollection;
    }

    public static URL buildUrl(SortOrder order) {

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

    public static String getResponseFromHttpUrl(URL url) throws IOException {
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

}