package com.baekgol.reactnativealarmmanager.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.sql.Time;

@Dao
public interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long add(AlarmDto dto);

    @Query("select * from alarm where alarm_id=:alarmId")
    AlarmDto search(int alarmId);

    @Query("select * from alarm where alarm_time=:alarmTime")
    AlarmDto search(Time alarmTime);

    @Query("select * from alarm order by alarm_time")
    AlarmDto[] searchAll();

    @Update
    int modifyAlarm(AlarmDto dto);

    @Query("delete from alarm where alarm_id=:alarmId")
    int deleteAlarm(int alarmId);
}
