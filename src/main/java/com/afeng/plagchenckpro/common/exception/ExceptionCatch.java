package com.afeng.plagchenckpro.common.exception;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.reuslt.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class ExceptionCatch {

    /**
     * 处理不可控异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exception(Exception e){
        e.printStackTrace();
        log.error("catch exception:{}",e.getMessage());

        return Result.build(null, ResultCodeEnum.SERVICE_ERROR);
    }

}
