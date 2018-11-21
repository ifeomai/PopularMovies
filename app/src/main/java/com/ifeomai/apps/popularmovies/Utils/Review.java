package com.ifeomai.apps.popularmovies.Utils;

import org.json.JSONArray;

public class Review {
    public JSONArray mReviewArray;
    public Integer mReviewCount;

    public Review( JSONArray reviews, int reviewCount){
        mReviewCount = reviewCount;
        mReviewArray = reviews;
    }
}
