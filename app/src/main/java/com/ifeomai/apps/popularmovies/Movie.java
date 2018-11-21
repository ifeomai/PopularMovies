package com.ifeomai.apps.popularmovies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class Movie implements Serializable {

    public String mMovieId;
    public String mTitle;
    public String mPosterURL;
    public String mOverview;
    public String mUserRating;
    public String mReleaseDate;

    public Movie(Map<String,String> movieData){
        mMovieId = movieData.get("id");
        mTitle = movieData.get("title");
        mPosterURL = movieData.get("poster");
        mOverview = movieData.get("overview");
        mUserRating = movieData.get("rating");
        mReleaseDate = movieData.get("releaseDate");
    }
    public static List<Movie> createMovies(List<Map<String,String>> moviesCollection){

        List<Movie> movies = new ArrayList<>();

        for(Map<String,String> movieItem: moviesCollection){
            movies.add(new Movie(movieItem));
        }
        return movies;
    }

}