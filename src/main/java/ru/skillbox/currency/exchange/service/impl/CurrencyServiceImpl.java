package ru.skillbox.currency.exchange.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyResponse;
import ru.skillbox.currency.exchange.dto.CurrencyData;
import ru.skillbox.currency.exchange.dto.ShortCurrency;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.mapper.CurrencyMapper;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;
import ru.skillbox.currency.exchange.service.CurrencyService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyMapper mapper;
    private final CurrencyRepository repository;

    public CurrencyResponse getAllCurrencies() {
        List<ShortCurrency> detail = new ArrayList<>();
        List<Currency> currencies = repository.findAll();

        for(Currency currency : currencies) {
            ShortCurrency detailedCurrency = new ShortCurrency();
            detailedCurrency.setName(currency.getName());
            detailedCurrency.setValue(currency.getValue());
            detail.add(detailedCurrency);
        }
        CurrencyData currencyData = new CurrencyData();
        currencyData.setDetailed(detail);

        CurrencyResponse response = new CurrencyResponse();
        response.setResult(true);
        response.setData(currencyData);
        return null;
    }

    @Override
    public CurrencyDto getById(Long id) {
        log.info("CurrencyService method getById executed");
        Currency currency = repository.findById(id).orElseThrow(() -> new RuntimeException("Currency not found with id: " + id));
        return mapper.convertToDto(currency);
    }

    @Override
    public Double convertValue(Long value, Long numCode) {
        log.info("CurrencyService method convertValue executed");
        Currency currency = repository.findByIsoNumCode(numCode);
        return value * currency.getValue();
    }

    @Override
    public CurrencyDto create(CurrencyDto dto) {
        log.info("CurrencyService method create executed");
        return  mapper.convertToDto(repository.save(mapper.convertToEntity(dto)));
    }
}
