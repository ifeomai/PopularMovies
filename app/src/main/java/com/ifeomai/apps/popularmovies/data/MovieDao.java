package com.ifeomai.apps.popularmovies.data;
import android.arch.lifecycle.LiveData;
import com.ifeomai.apps.popularmovies.Movie;
import java.util.List;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    LiveData<List<Movie>> loadFavorites();

    @Insert
    void insertFavorite(Movie favoriteMovie);

    @Delete
    void deleteFavorite(Movie favoriteMovie);

    @Query("SELECT * FROM movie WHERE mMovieId = :id")
    Movie loadFavoriteById(String id);

}

