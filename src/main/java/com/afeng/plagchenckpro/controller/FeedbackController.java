package com.afeng.plagchenckpro.controller;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.pojo.Feedback;
import com.afeng.plagchenckpro.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@Slf4j
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;


    /**
     * 添加反馈
     * @param feedback
     * @return
     */
    @PostMapping("/add")
    public Result addFeedback(@RequestBody Feedback feedback) {
        log.info("添加反馈：{}",feedback.getName());
        return feedbackService.addFeedback(feedback);
    }

    /**
     * 获取反馈
     * @return
     */
    @GetMapping("/get")
    public Result getFeedback() {
        log.info("获取反馈");
        return feedbackService.getFeedback();
    }
}
