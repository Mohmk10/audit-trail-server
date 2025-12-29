package com.mohmk10.audittrail.core.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {

    @Test
    void shouldCreateValidRange() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        DateRange range = new DateRange(from, to);

        assertThat(range.from()).isEqualTo(from);
        assertThat(range.to()).isEqualTo(to);
    }

    @Test
    void shouldHandleSameDayRange() {
        Instant from = Instant.parse("2024-06-15T00:00:00Z");
        Instant to = Instant.parse("2024-06-15T23:59:59Z");

        DateRange range = new DateRange(from, to);

        assertThat(range.from()).isEqualTo(from);
        assertThat(range.to()).isEqualTo(to);
    }

    @Test
    void shouldHandleRangeSpanningMultipleMonths() {
        Instant from = Instant.parse("2024-01-15T10:30:00Z");
        Instant to = Instant.parse("2024-04-20T15:45:00Z");

        DateRange range = new DateRange(from, to);

        assertThat(range.from()).isBefore(range.to());
    }

    @Test
    void shouldCreateRangeWithCurrentTime() {
        Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = Instant.now();

        DateRange range = new DateRange(from, to);

        assertThat(range.from()).isBefore(range.to());
        assertThat(range.to()).isAfterOrEqualTo(range.from());
    }

    @Test
    void shouldSupportRecordEquality() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        DateRange range1 = new DateRange(from, to);
        DateRange range2 = new DateRange(from, to);

        assertThat(range1).isEqualTo(range2);
        assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualRanges() {
        Instant from1 = Instant.parse("2024-01-01T00:00:00Z");
        Instant from2 = Instant.parse("2024-02-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        DateRange range1 = new DateRange(from1, to);
        DateRange range2 = new DateRange(from2, to);

        assertThat(range1).isNotEqualTo(range2);
    }

    @Test
    void shouldPreserveTimezoneInfo() {
        Instant from = Instant.parse("2024-06-15T10:30:00Z");
        Instant to = Instant.parse("2024-06-15T18:45:30Z");

        DateRange range = new DateRange(from, to);

        assertThat(range.from().toString()).contains("2024-06-15T10:30:00Z");
        assertThat(range.to().toString()).contains("2024-06-15T18:45:30Z");
    }

    @Test
    void shouldHandleHourRange() {
        Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant to = Instant.now();

        DateRange range = new DateRange(from, to);

        long hoursBetween = ChronoUnit.HOURS.between(range.from(), range.to());
        assertThat(hoursBetween).isEqualTo(1);
    }

    @Test
    void shouldHandleMinuteRange() {
        Instant from = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant to = Instant.now();

        DateRange range = new DateRange(from, to);

        long minutesBetween = ChronoUnit.MINUTES.between(range.from(), range.to());
        assertThat(minutesBetween).isEqualTo(30);
    }
}
