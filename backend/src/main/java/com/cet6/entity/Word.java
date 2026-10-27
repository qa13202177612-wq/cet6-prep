package com.cet6.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** 六级核心词汇(真题高频 Top1500,分批导入) */
@TableName("t_word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String example;
    private String exampleCn;
    /** 真题出现频率排名(1 最核心) */
    private Integer freqRank;
    /** 阶段标签:词汇基础/专项突破 */
    private String stageTag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
    public String getExampleCn() { return exampleCn; }
    public void setExampleCn(String exampleCn) { this.exampleCn = exampleCn; }
    public Integer getFreqRank() { return freqRank; }
    public void setFreqRank(Integer freqRank) { this.freqRank = freqRank; }
    public String getStageTag() { return stageTag; }
    public void setStageTag(String stageTag) { this.stageTag = stageTag; }
}
