package com.afeng.plagchenckpro.mapper;

import com.afeng.plagchenckpro.entity.pojo.BatchUploadTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BatchUploadTaskMapper extends BaseMapper<BatchUploadTask> {
    @Insert("INSERT INTO batch_upload_task(id, total_files, status, progress, message) " +
            "VALUES(#{id}, #{totalFiles}, #{status}, #{progress}, #{message})")
    int insert(BatchUploadTask task);

    @Update("UPDATE batch_upload_task SET success_count=#{successCount}, failed_count=#{failedCount}, " +
            "status=#{status}, progress=#{progress}, message=#{message}, updated_at=NOW() WHERE id=#{id}")
    int update(BatchUploadTask task);

    @Select("SELECT * FROM batch_upload_task WHERE id=#{id}")
    BatchUploadTask selectById(String id);

    @Select("SELECT * FROM batch_upload_task ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<BatchUploadTask> selectPage(@Param("offset") int offset, @Param("limit") int limit);
}
