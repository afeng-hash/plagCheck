package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.utils.ThreadLocalUtil;
import com.afeng.plagchenckpro.entity.pojo.Feedback;
import com.afeng.plagchenckpro.mapper.FeedbackMapper;
import com.afeng.plagchenckpro.service.FeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    /**
     * 添加反馈
     * @param feedback
     * @return
     */
    @Override
    public Result addFeedback(Feedback feedback) {
        Long id = ThreadLocalUtil.getId();
        feedback.setUserId(id);
        feedback.setDate(LocalDate.now());
        save(feedback);
        return Result.ok();
    }


    /**
     * 获取反馈
     * @return
     */
    @Override
    public Result getFeedback() {
        //只获取最新的10条反馈，根据时间倒序
        List<Feedback> feedbacks = list(new QueryWrapper<Feedback>().orderByDesc("date").last("limit 10"));
        return Result.ok(feedbacks);
    }
}
