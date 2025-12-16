package com.yxh.fangs.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yxh.fangs.bean.Message;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface MessageDao {
    @Insert
    Completable insertMessage(Message message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessages(Message... messages);

    @Delete
    void deleteStudent(Message message);

    @Update
    void updateStudent(Message... messages);

    @Query("SELECT * FROM message")
    List<Message> queryAllMessage();

    @Query("SELECT * FROM message")
    Flowable<List<Message>> queryRxAllMessage();

    @Query("SELECT * FROM message WHERE message_type = :type")
    List<Message> queryAllByType(int type);

    @Query("SELECT * FROM message WHERE message_type = :type")
    Flowable<List<Message>> queryRxAllByType(int type);
}
