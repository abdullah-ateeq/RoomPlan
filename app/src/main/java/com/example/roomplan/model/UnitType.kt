package com.example.roomplan.model

import java.util.Locale

enum class UnitType(
    val symbol: String,
    val displayName: String,
    val mmFactor: Double
) {
    MM("mm", "Millimeters", 1.0),
    CM("cm", "Centimeters", 10.0),
    M("m", "Meters", 1000.0),
    INCH("in", "Inches", 25.4),
    FEET("ft", "Feet", 304.8);

    fun fromMm(mm: Double): Double = mm / mmFactor

    fun toMm(value: Double): Double = value * mmFactor

    fun format(mm: Double, decimals: Int = 1): String {
        val converted = fromMm(mm)
        return String.format(Locale.US, "%.${decimals}f %s", converted, symbol)
    }

    fun formatPlain(mm: Double, decimals: Int = 1): String {
        val converted = fromMm(mm)
        return if (converted % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", converted)
        } else {
            String.format(Locale.US, "%.${decimals}f", converted)
        }
    }
}
