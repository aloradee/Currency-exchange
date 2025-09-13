package ru.skillbox.currency.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyResponse;
import ru.skillbox.currency.exchange.service.CurrencyService;
import ru.skillbox.currency.exchange.service.impl.CbrCurrencyServiceImpl;
import ru.skillbox.currency.exchange.service.impl.CurrencyServiceImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/currency")
public class CurrencyController {
    private final CbrCurrencyServiceImpl cbrCurrencyService;
    private final CurrencyService service;

    @PostMapping("/update")
    public ResponseEntity<String> updateCurrencies() {
        try {
            cbrCurrencyService.updateCurrenciesFromCbr();
            return ResponseEntity.ok("Данные о валютах успешно обновлены");
        } catch (Exception exception) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при обновлении данных: " + exception.getMessage());
        }
    }

    @GetMapping(value = "/")
    ResponseEntity<CurrencyResponse> getAllCurrency() {
        return ResponseEntity.ok(service.getAllCurrencies());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<CurrencyDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping(value = "/convert")
    ResponseEntity<Double> convertValue(@RequestParam("value") Long value, @RequestParam("numCode") Long numCode) {
        return ResponseEntity.ok(service.convertValue(value, numCode));
    }

    @PostMapping("/create")
    ResponseEntity<CurrencyDto> create(@RequestBody CurrencyDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }
}
