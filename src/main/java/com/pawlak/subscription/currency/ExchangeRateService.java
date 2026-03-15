package com.pawlak.subscription.currency;

import com.pawlak.subscription.currency.dto.NbpRateResponse;
import com.pawlak.subscription.exception.domain.ExchangeRateUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String CACHE_KEY_PREFIX = "exchange_rate:";

    private final RestClient nbpRestClient;
    private final StringRedisTemplate redisTemplate;

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        if (to == Currency.PLN) {
            BigDecimal rateToPln = getRateToPln(from);
            return amount.multiply(rateToPln)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (from == Currency.PLN) {
            BigDecimal rateToPln = getRateToPln(to);
            return amount.divide(rateToPln, 2, RoundingMode.HALF_UP);
        }

        BigDecimal fromToPln = getRateToPln(from);
        BigDecimal toToPln = getRateToPln(to);
        return amount
                .multiply(fromToPln)
                .divide(toToPln, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRateToPln(Currency currency) {
        String cacheKey = CACHE_KEY_PREFIX + currency.name();
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.debug("Cache exchange rate: {}", currency);
            return new BigDecimal(cached);
        }

        BigDecimal rate = fetchRateFromNbp(currency);
        redisTemplate.opsForValue().set(cacheKey, rate.toString(), CACHE_TTL);
        log.debug("Fetch exchange rate from NBP: {} = {} PLN", currency, rate);
        return rate;
    }

    private BigDecimal fetchRateFromNbp(Currency currency) {
        try {
            NbpRateResponse response = nbpRestClient.get()
                    .uri("/exchangerates/rates/A/{code}/", currency.name())
                    .retrieve()
                    .body(NbpRateResponse.class);

            if (response == null || response.getRates() == null) {
                throw new ExchangeRateUnavailableException();
            }

            return response.getRates();
        } catch (ExchangeRateUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exchange rate error {} from NBP: {}", currency, e.getMessage());
            throw new ExchangeRateUnavailableException();
        }
    }
}

