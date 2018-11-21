package com.ifeomai.apps.popularmovies.Utils;

public class Trailer {
    public String[] mYoutube_ids;
    public int mTrailer_count;
    public Trailer (String[] youtube_ids, int trailer_count){
        mTrailer_count = trailer_count;
        mYoutube_ids = youtube_ids;
    }
}
