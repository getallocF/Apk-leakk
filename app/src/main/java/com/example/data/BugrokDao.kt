package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerProfileDao {
    @Query("SELECT * FROM server_profiles ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllProfiles(): Flow<List<ServerProfileEntity>>

    @Query("SELECT * FROM server_profiles ORDER BY isFavorite DESC, updatedAt DESC")
    suspend fun getAllProfilesSnapshot(): List<ServerProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ServerProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ServerProfileEntity>)

    @Update
    suspend fun updateProfile(profile: ServerProfileEntity)

    @Query("DELETE FROM server_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)

    @Query("DELETE FROM server_profiles")
    suspend fun clearAllProfiles()
}

@Dao
interface DownloadedPackDao {
    @Query("SELECT * FROM downloaded_packs ORDER BY downloadedAt DESC")
    fun getAllDownloadedPacks(): Flow<List<DownloadedPackEntity>>

    @Query("SELECT * FROM downloaded_packs ORDER BY downloadedAt DESC")
    suspend fun getAllDownloadedPacksSnapshot(): List<DownloadedPackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: DownloadedPackEntity): Long

    @Query("DELETE FROM downloaded_packs WHERE id = :id")
    suspend fun deletePackById(id: Int)

    @Query("DELETE FROM downloaded_packs")
    suspend fun clearAllPacks()
}
