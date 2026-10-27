package com.example.roomplan.model

enum class FurnitureType(
    val displayName: String,
    val defaultWidthMm: Double,
    val defaultDepthMm: Double,
    val defaultHeightMm: Double? = null,
    val isWallPreferred: Boolean = false,
    val category: String = "General"
) {
    BED("Bed", 2000.0, 1600.0, 600.0, true, "Bedroom"),
    SINGLE_BED("Single Bed", 2000.0, 900.0, 550.0, true, "Bedroom"),
    DOUBLE_BED("Double Bed", 2000.0, 1400.0, 600.0, true, "Bedroom"),
    QUEEN_BED("Queen Bed", 2000.0, 1600.0, 650.0, true, "Bedroom"),
    KING_BED("King Bed", 2000.0, 1900.0, 650.0, true, "Bedroom"),
    SOFA("Sofa", 2000.0, 900.0, 850.0, true, "Living"),
    TWO_SEATER_SOFA("2-Seater Sofa", 1600.0, 850.0, 850.0, true, "Living"),
    THREE_SEATER_SOFA("3-Seater Sofa", 2200.0, 900.0, 850.0, true, "Living"),
    SECTIONAL_SOFA("Sectional Sofa", 2600.0, 1600.0, 850.0, true, "Living"),
    ARMCHAIR("Armchair", 900.0, 850.0, 800.0, false, "Living"),
    CENTER_TABLE("Center Table", 1200.0, 600.0, 450.0, false, "Living"),
    DINING_TABLE("Dining Table", 1600.0, 900.0, 750.0, false, "Dining"),
    DINING_CHAIR("Dining Chair", 500.0, 500.0, 900.0, false, "Dining"),
    WARDROBE("Wardrobe", 1800.0, 600.0, 2100.0, true, "Storage"),
    DRESSER("Dresser", 1200.0, 500.0, 900.0, true, "Storage"),
    DESK("Desk", 1400.0, 700.0, 750.0, true, "Office"),
    OFFICE_CHAIR("Office Chair", 650.0, 650.0, 950.0, false, "Office"),
    TV_UNIT("TV Unit", 1800.0, 450.0, 500.0, true, "Living"),
    CABINET("Cabinet", 1000.0, 450.0, 1200.0, true, "Storage"),
    BOOKSHELF("Bookshelf", 1000.0, 350.0, 1800.0, true, "Storage"),
    REFRIGERATOR("Refrigerator", 800.0, 750.0, 1800.0, true, "Kitchen"),
    WASHING_MACHINE("Washing Machine", 600.0, 600.0, 850.0, true, "Utility"),
    CUSTOM("Custom", 1000.0, 1000.0, null, false, "Custom")
}
