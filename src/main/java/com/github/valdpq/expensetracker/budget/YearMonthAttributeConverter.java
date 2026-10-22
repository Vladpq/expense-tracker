package com.github.valdpq.expensetracker.budget;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.YearMonth;

@Converter
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth == null ? null : yearMonth.atDay(1);
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate date) {
        return date == null ? null : YearMonth.from(date);
    }
}
