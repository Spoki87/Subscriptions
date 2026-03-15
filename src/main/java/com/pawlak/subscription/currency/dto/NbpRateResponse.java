package com.pawlak.subscription.currency.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class NbpRateResponse {
    private String code;
    private List<Rate> rates;

    public BigDecimal getRates() {
        return rates.getFirst().getRate();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rate {
        private BigDecimal rate;
    }
}
