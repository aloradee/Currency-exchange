package ru.skillbox.currency.exchange.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.model.cbr.ValCurs;
import ru.skillbox.currency.exchange.model.cbr.Valute;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;
import ru.skillbox.currency.exchange.service.CbrCurrencyService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Optional;

@Slf4j
public class CbrCurrencyServiceImpl implements CbrCurrencyService {

    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final long ONE_HOUR_IN_MILLIS = 3600000L;

    private final CurrencyRepository repository;
    private final RestTemplate restTemplate;

    @Value("${cbr.currency.url}")
    private String cbrApiUrl;

    public CbrCurrencyServiceImpl(CurrencyRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void updateCurrenciesFromCbr() {
        try {
            ValCurs valCurs = fetchCurrenciesFromCbr();
            Optional.ofNullable(valCurs)
                    .map(ValCurs::getValutes)
                    .ifPresent(this::processCurrencies);
        } catch (Exception exception) {
            log.error("Ошибка при обновлении данных о валютах из ЦБ РФ", exception);
            throw new RuntimeException("Ошибка при обновлении данных о валютах", exception);
        }
    }

    private void processCurrencies(java.util.List<Valute> cbrValutes) {
        cbrValutes.stream()
                .forEach(cbrValute -> processSingleCurrency(cbrValute));
    }

    private void processSingleCurrency(Valute cbrValute) {
        try {
            Currency currency = convertToCurrencyEntity(cbrValute);
            saveOrUpdateCurrency(currency);
        } catch (Exception exception) {
            log.warn("Ошибка при обработке валюты: {}", cbrValute.getCharCode(), exception);
        }
    }

    private ValCurs fetchCurrenciesFromCbr() {
        try {
            String xmlResponse = restTemplate.getForObject(cbrApiUrl, String.class);
            JAXBContext jaxbContext = JAXBContext.newInstance(ValCurs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            return (ValCurs) unmarshaller.unmarshal(new StringReader(xmlResponse));
        } catch (Exception exception) {
            log.error("Ошибка при получении данных из ЦБ РФ", exception);
            throw new RuntimeException("Ошибка при получении данных из ЦБ РФ", exception);
        }
    }

    private Currency convertToCurrencyEntity(Valute cbrValute) {
        Currency currency = new Currency();
        currency.setIsoCharCode(cbrValute.getCharCode());
        currency.setName(cbrValute.getName());
        currency.setIsoNumCode(cbrValute.getNumCode());
        currency.setId(cbrValute.getId());
        currency.setNominal(cbrValute.getNominal());
        currency.setValue(cbrValute.getValue());

        return currency;
    }

    private void saveOrUpdateCurrency(Currency currency) {
        repository.findByIsoCharCode(currency.getIsoCharCode())
                .ifPresentOrElse(
                        existingCurrency -> updateExistingCurrency(existingCurrency, currency),
                        () -> createNewCurrency(currency)
                );
    }
    private void updateExistingCurrency(Currency existing, Currency updated) {
        existing.setName(updated.getName());
        existing.setIsoNumCode(updated.getIsoNumCode());
        existing.setNominal(updated.getNominal());
        existing.setValue(updated.getValue());
        existing.setId(updated.getId());

        repository.save(existing);
        log.debug("Обновлена валюта: {}", updated.getIsoCharCode());
    }

    private void createNewCurrency(Currency currency) {
        repository.save(currency);
        log.debug("Создана новая валюта: {}", currency.getIsoCharCode());
    }

    @Scheduled(fixedRate = ONE_HOUR_IN_MILLIS)
    @Override
    public void scheduledCurrencyUpdate() {
        log.info("Запуск запланированного обновления валют...");
        updateCurrenciesFromCbr();
        log.info("Обновление валют завершено.");
    }


}
