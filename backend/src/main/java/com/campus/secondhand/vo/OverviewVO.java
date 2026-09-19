package com.campus.secondhand.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OverviewVO {
    private Long productTotal;
    private Long onSaleTotal;
    private Long userTotal;
    private Long orderTotal;
    private Long favoriteTotal;
    private Long todayNewProduct;
    private BigDecimal tradeAmount;
    private Long hotKeywordTotal;
}
