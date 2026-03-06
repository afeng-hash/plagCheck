package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.entity.pojo.Sentence;
import com.afeng.plagchenckpro.mapper.SentenceMapper;
import com.afeng.plagchenckpro.service.SentenceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SentenceServiceImpl extends ServiceImpl<SentenceMapper, Sentence> implements SentenceService {
}
