package com.pw.edu.pl.master.thesis.ai.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NumberUtils {

    private NumberUtils() {}

    public static Double round2(Double value) {
        if (value == null) return null;

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
