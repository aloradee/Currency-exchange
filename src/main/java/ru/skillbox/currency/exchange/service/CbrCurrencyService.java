package ru.skillbox.currency.exchange.service;

public interface CbrCurrencyService {
    void updateCurrenciesFromCbr();
    void scheduledCurrencyUpdate();
}
