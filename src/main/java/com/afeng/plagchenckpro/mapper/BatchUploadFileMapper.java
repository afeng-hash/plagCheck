package com.afeng.plagchenckpro.mapper;

import com.afeng.plagchenckpro.entity.pojo.BatchUploadFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BatchUploadFileMapper extends BaseMapper<BatchUploadFile> {
    @Insert("INSERT INTO batch_upload_file(task_id, file_name, file_path, file_size, status, message) " +
            "VALUES(#{taskId}, #{fileName}, #{filePath}, #{fileSize}, #{status}, #{message})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BatchUploadFile file);

    @Update("UPDATE batch_upload_file SET status=#{status}, message=#{message}, result_data=#{resultData}, " +
            "file_path=#{filePath}, updated_at=NOW() WHERE id=#{id}")
    int update(BatchUploadFile file);


    @Select("SELECT COUNT(*) FROM batch_upload_file WHERE task_id=#{taskId} AND status='success'")
    int countSuccessByTaskId(String taskId);

    int batchInsert(@Param("list") List<BatchUploadFile> fileList);


    @Select("SELECT COUNT(*) FROM batch_upload_file WHERE task_id=#{taskId} AND status='failed'")
    int countFailedByTaskId(String taskId);


}
