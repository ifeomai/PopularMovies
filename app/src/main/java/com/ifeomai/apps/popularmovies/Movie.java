package com.ifeomai.apps.popularmovies;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
public class Movie implements Serializable {

    @PrimaryKey @NonNull
    public String mMovieId;
    public String mTitle;
    public String mPosterURL;
    public String mOverview;
    public String mUserRating;
    public String mReleaseDate;

    public Movie(){}

    @Ignore
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