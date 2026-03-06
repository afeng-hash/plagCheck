package com.afeng.plagchenckpro.service;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.pojo.Feedback;
import com.baomidou.mybatisplus.extension.service.IService;


public interface FeedbackService extends IService<Feedback> {

    /**
     * 添加反馈
     * @param feedback
     * @return
     */
    Result addFeedback(Feedback feedback);

    /**
     * 获取反馈
     * @return
     */
    Result getFeedback();
}
