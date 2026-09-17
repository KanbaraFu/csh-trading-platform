package com.campus.secondhand.vo;

import lombok.Data;

@Data
public class HotWordVO {
    private String word;
    private Double score;
    private Long rank;
    private Integer percent;
    private Boolean is_hot;
}
