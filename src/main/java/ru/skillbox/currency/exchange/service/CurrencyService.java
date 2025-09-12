package ru.skillbox.currency.exchange.service;

import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyResponse;

public interface CurrencyService {

    CurrencyDto getById(Long id);
    Double convertValue(Long value, Long numCode);
    CurrencyDto create(CurrencyDto dto);
    CurrencyResponse getAllCurrencies();
}
