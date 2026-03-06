package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.entity.pojo.Word;
import com.afeng.plagchenckpro.mapper.WordMapper;
import com.afeng.plagchenckpro.service.WordSerivce;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordSerivce {
}
