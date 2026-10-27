package com.example.roomplan

import com.example.roomplan.data.PlanStorage
import com.example.roomplan.engine.AutoLayoutEngine
import com.example.roomplan.engine.RoomCalculator
import com.example.roomplan.engine.RoomCollisionEngine
import com.example.roomplan.engine.RoomPlanGenerator
import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.DoorType
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.FurnitureType
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.WallSide
import com.example.roomplan.model.Window
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomPlanUnitTest {

    @Test
    fun testRectangleArea() {
        // Room: 500 × 400 cm = 5000 × 4000 mm
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)
        val areaMm2 = RoomCalculator.calculateArea(room, RoomShape.RECTANGLE)
        val areaM2 = RoomCalculator.mm2ToM2(areaMm2)

        // Expected: 20 m²
        assertEquals(20.0, areaM2, 0.001)
    }

    @Test
    fun testRectanglePerimeter() {
        // Room: 500 × 400 cm = 5000 × 4000 mm
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)
        val perimeterMm = RoomCalculator.calculatePerimeter(room, RoomShape.RECTANGLE)
        val perimeterM = perimeterMm / 1000.0

        // Expected: 2 * (5 + 4) = 18 m
        assertEquals(18.0, perimeterM, 0.001)
    }

    @Test
    fun testUnitConversion() {
        // 1 m = 1000 mm
        assertEquals(1000.0, UnitType.M.toMm(1.0), 0.001)
        assertEquals(1.0, UnitType.M.fromMm(1000.0), 0.001)

        // 1 cm = 10 mm
        assertEquals(10.0, UnitType.CM.toMm(1.0), 0.001)
        assertEquals(1.0, UnitType.CM.fromMm(10.0), 0.001)

        // 1 inch = 25.4 mm
        assertEquals(25.4, UnitType.INCH.toMm(1.0), 0.001)
        assertEquals(1.0, UnitType.INCH.fromMm(25.4), 0.001)

        // 1 foot = 304.8 mm
        assertEquals(304.8, UnitType.FEET.toMm(1.0), 0.001)
        assertEquals(1.0, UnitType.FEET.fromMm(304.8), 0.001)
    }

    @Test
    fun testSquareRoom() {
        // Room: 400 × 400 cm = 4000 × 4000 mm
        val square = RoomDimensions(lengthMm = 4000.0, widthMm = 4000.0)
        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(square, RoomShape.SQUARE))
        val perimeterM = RoomCalculator.calculatePerimeter(square, RoomShape.SQUARE) / 1000.0

        // Expected: 16 m² area, 16 m perimeter
        assertEquals(16.0, areaM2, 0.001)
        assertEquals(16.0, perimeterM, 0.001)
    }

    @Test
    fun testWallArea() {
        // Room: 500 × 400 cm (5000 × 4000 mm), Height: 300 cm (3000 mm)
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0, heightMm = 3000.0)
        val wallAreaMm2 = RoomCalculator.calculateWallArea(room, 3000.0, RoomShape.RECTANGLE)
        val wallAreaM2 = RoomCalculator.mm2ToM2(wallAreaMm2)

        // Perimeter = 18 m. Height = 3 m. Wall area = 18 × 3 = 54 m²
        assertEquals(54.0, wallAreaM2, 0.001)
    }

    @Test
    fun testDoorValidation() {
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)

        // Valid door: 90 cm at 50 cm offset on 500 cm bottom wall
        val validDoor = Door(
            id = "d1",
            widthMm = 900.0,
            wall = WallSide.BOTTOM,
            offsetMm = 500.0
        )
        assertTrue(RoomCollisionEngine.validateDoor(validDoor, room))

        // Invalid door: 90 cm at 450 cm offset on 500 cm bottom wall (450 + 90 = 540 > 500)
        val invalidDoor = Door(
            id = "d2",
            widthMm = 900.0,
            wall = WallSide.BOTTOM,
            offsetMm = 4500.0
        )
        assertFalse(RoomCollisionEngine.validateDoor(invalidDoor, room))
    }

    @Test
    fun testWindowValidation() {
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)

        // Valid window: 150 cm at 100 cm offset on 500 cm top wall
        val validWin = Window(
            id = "w1",
            widthMm = 1500.0,
            wall = WallSide.TOP,
            offsetMm = 1000.0
        )
        assertTrue(RoomCollisionEngine.validateWindow(validWin, room))

        // Invalid window: 150 cm at 400 cm offset on 500 cm wall (400 + 150 = 550 > 500)
        val invalidWin = Window(
            id = "w2",
            widthMm = 1500.0,
            wall = WallSide.TOP,
            offsetMm = 4000.0
        )
        assertFalse(RoomCollisionEngine.validateWindow(invalidWin, room))
    }

    @Test
    fun testFurnitureBoundary() {
        // Room 500 × 400 cm
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)

        // Furniture: 220 × 90 cm (2200 × 900 mm)
        // Position X=100 cm, Y=100 cm (1000, 1000 mm) -> Inside
        val itemInside = FurnitureItem(
            id = "f1",
            type = FurnitureType.THREE_SEATER_SOFA,
            name = "Sofa",
            widthMm = 2200.0,
            depthMm = 900.0,
            xMm = 1000.0,
            yMm = 1000.0
        )
        val rectInside = RoomCollisionEngine.getFurnitureRect(itemInside)
        assertTrue(RoomCollisionEngine.isInsideRoom(rectInside, room, RoomShape.RECTANGLE))

        // Move to X=400 cm (4000 mm) -> 4000 + 2200 = 6200 > 5000 -> Outside
        val itemOutside = itemInside.copy(xMm = 4000.0)
        val rectOutside = RoomCollisionEngine.getFurnitureRect(itemOutside)
        assertFalse(RoomCollisionEngine.isInsideRoom(rectOutside, room, RoomShape.RECTANGLE))
    }

    @Test
    fun testRotationFootprint() {
        // Furniture: 220 × 90 cm
        val sofa = FurnitureItem(
            id = "f1",
            type = FurnitureType.THREE_SEATER_SOFA,
            name = "Sofa",
            widthMm = 2200.0,
            depthMm = 900.0,
            xMm = 0.0,
            yMm = 0.0,
            rotationDegrees = 90
        )

        // At 90°, effective width = 900 mm, effective depth = 2200 mm
        assertEquals(900.0, sofa.effectiveWidthMm, 0.001)
        assertEquals(2200.0, sofa.effectiveDepthMm, 0.001)

        // At 180°, effective width = 2200 mm, effective depth = 900 mm
        val sofa180 = sofa.copy(rotationDegrees = 180)
        assertEquals(2200.0, sofa180.effectiveWidthMm, 0.001)
        assertEquals(900.0, sofa180.effectiveDepthMm, 0.001)
    }

    @Test
    fun testCollisionOverlap() {
        // Furniture A: 220 × 90 cm at (100, 100)
        val itemA = FurnitureItem(
            id = "a",
            type = FurnitureType.THREE_SEATER_SOFA,
            name = "Sofa",
            widthMm = 2200.0,
            depthMm = 900.0,
            xMm = 1000.0,
            yMm = 1000.0
        )

        // Furniture B: 120 × 60 cm at (150, 120) -> Overlaps A
        val itemBOverlapping = FurnitureItem(
            id = "b",
            type = FurnitureType.CENTER_TABLE,
            name = "Table",
            widthMm = 1200.0,
            depthMm = 600.0,
            xMm = 1500.0,
            yMm = 1200.0
        )

        val rectA = RoomCollisionEngine.getFurnitureRect(itemA)
        val rectBOverlapping = RoomCollisionEngine.getFurnitureRect(itemBOverlapping)
        assertTrue(rectA.overlaps(rectBOverlapping))

        // Move B apart to (350, 250) -> No overlap
        val itemBApart = itemBOverlapping.copy(xMm = 3500.0, yMm = 2500.0)
        val rectBApart = RoomCollisionEngine.getFurnitureRect(itemBApart)
        assertFalse(rectA.overlaps(rectBApart))
    }

    @Test
    fun testGridSnap() {
        val gridSpacingMm = 250.0 // 25 cm
        val posMm = 1130.0 // 113 cm

        val snapped = RoomPlanGenerator.snapToGrid(posMm, gridSpacingMm)
        // 1130 / 250 = 4.52 -> rounds to 5 * 250 = 1250 mm (125 cm)
        assertEquals(1250.0, snapped, 0.001)

        val pos2Mm = 1120.0 // 1120 / 250 = 4.48 -> rounds to 4 * 250 = 1000 mm (100 cm)
        assertEquals(1000.0, RoomPlanGenerator.snapToGrid(pos2Mm, gridSpacingMm), 0.001)
    }

    @Test
    fun testLShapedRoom() {
        // Overall: 600 × 500 cm = 6000 × 5000 mm
        // Cutout: 200 × 150 cm = 2000 × 1500 mm
        val lRoom = RoomDimensions(
            lengthMm = 6000.0,
            widthMm = 5000.0,
            cutoutLengthMm = 2000.0,
            cutoutWidthMm = 1500.0
        )

        // Area = (6 × 5) - (2 × 1.5) = 30 - 3 = 27 m²
        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(lRoom, RoomShape.L_SHAPE))
        assertEquals(27.0, areaM2, 0.001)

        // Perimeter = 2 * (6 + 5) = 22 m
        val perimM = RoomCalculator.calculatePerimeter(lRoom, RoomShape.L_SHAPE) / 1000.0
        assertEquals(22.0, perimM, 0.001)

        // Test boundary check in cutout zone:
        // Cutout is X in [4000, 6000], Y in [0, 1500]
        val itemInCutout = FurnitureItem(
            id = "c1",
            type = FurnitureType.ARMCHAIR,
            name = "Chair",
            widthMm = 800.0,
            depthMm = 800.0,
            xMm = 4500.0,
            yMm = 500.0
        )
        val rectInCutout = RoomCollisionEngine.getFurnitureRect(itemInCutout)
        assertFalse(RoomCollisionEngine.isInsideRoom(rectInCutout, lRoom, RoomShape.L_SHAPE))

        // Item in usable zone: X in [1000, 2000], Y in [2000, 3000]
        val itemInUsable = itemInCutout.copy(xMm = 1000.0, yMm = 2000.0)
        val rectInUsable = RoomCollisionEngine.getFurnitureRect(itemInUsable)
        assertTrue(RoomCollisionEngine.isInsideRoom(rectInUsable, lRoom, RoomShape.L_SHAPE))
    }

    @Test
    fun testAutoLayout() {
        // Room 500 × 400 cm
        val room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0)
        val doors = listOf(
            Door(
                id = "d1",
                widthMm = 900.0,
                wall = WallSide.BOTTOM,
                offsetMm = 2050.0
            )
        )
        val furniture = listOf(
            FurnitureItem(
                id = "f1",
                type = FurnitureType.THREE_SEATER_SOFA,
                name = "Sofa",
                widthMm = 2200.0,
                depthMm = 900.0,
                xMm = 0.0,
                yMm = 0.0
            ),
            FurnitureItem(
                id = "f2",
                type = FurnitureType.CENTER_TABLE,
                name = "Table",
                widthMm = 1200.0,
                depthMm = 600.0,
                xMm = 0.0,
                yMm = 0.0
            ),
            FurnitureItem(
                id = "f3",
                type = FurnitureType.TV_UNIT,
                name = "TV Unit",
                widthMm = 1800.0,
                depthMm = 450.0,
                xMm = 0.0,
                yMm = 0.0
            )
        )

        val result = AutoLayoutEngine.arrange(
            room = room,
            shape = RoomShape.RECTANGLE,
            furniture = furniture,
            doors = doors,
            windows = emptyList(),
            columns = emptyList()
        )

        assertTrue(result.success)
        assertEquals(3, result.items.size)

        // Verify no overlaps
        val rects = result.items.map { RoomCollisionEngine.getFurnitureRect(it) }
        for (i in 0 until rects.size) {
            for (j in i + 1 until rects.size) {
                assertFalse("Furniture items should not overlap", rects[i].overlaps(rects[j]))
            }
            assertTrue("Furniture item must be inside room", RoomCollisionEngine.isInsideRoom(rects[i], room, RoomShape.RECTANGLE))
        }

        // Verify door clearance zone is preserved
        val doorZone = RoomCollisionEngine.getDoorClearanceZone(doors[0], room)
        for (rect in rects) {
            assertFalse("Door zone must remain accessible", rect.overlaps(doorZone))
        }
    }

    @Test
    fun testSerializationSaveLoad() {
        val originalPlan = RoomPlan(
            id = UUID.randomUUID().toString(),
            name = "Living Room",
            room = RoomDimensions(lengthMm = 5000.0, widthMm = 4000.0, heightMm = 3000.0),
            unit = UnitType.CM,
            shape = RoomShape.RECTANGLE,
            dimensionType = DimensionType.INSIDE,
            wallThicknessMm = 150.0,
            doors = listOf(
                Door(
                    id = "door-1",
                    widthMm = 900.0,
                    wall = WallSide.BOTTOM,
                    offsetMm = 1000.0,
                    type = DoorType.HINGED,
                    swing = DoorSwing.INWARD
                )
            ),
            windows = listOf(
                Window(
                    id = "win-1",
                    widthMm = 1500.0,
                    wall = WallSide.TOP,
                    offsetMm = 1200.0,
                    heightMm = 1400.0
                )
            ),
            furniture = listOf(
                FurnitureItem(
                    id = "f-1",
                    type = FurnitureType.THREE_SEATER_SOFA,
                    name = "Sofa",
                    widthMm = 2200.0,
                    depthMm = 900.0,
                    xMm = 1000.0,
                    yMm = 500.0,
                    rotationDegrees = 90
                )
            ),
            notes = "Spacious living room plan"
        )

        // Test serialization to JSONObject and parsing back
        val json = PlanStorage.serializePlan(originalPlan)
        val loadedPlan = PlanStorage.parsePlan(json)

        assertNotNull(loadedPlan)
        assertEquals(originalPlan.id, loadedPlan!!.id)
        assertEquals(originalPlan.name, loadedPlan.name)
        assertEquals(originalPlan.room.lengthMm, loadedPlan.room.lengthMm, 0.001)
        assertEquals(originalPlan.room.widthMm, loadedPlan.room.widthMm, 0.001)
        assertEquals(originalPlan.unit, loadedPlan.unit)
        assertEquals(originalPlan.doors.size, loadedPlan.doors.size)
        assertEquals(originalPlan.windows.size, loadedPlan.windows.size)
        assertEquals(originalPlan.furniture.size, loadedPlan.furniture.size)
        assertEquals(originalPlan.furniture[0].rotationDegrees, loadedPlan.furniture[0].rotationDegrees)
        assertEquals(originalPlan.notes, loadedPlan.notes)
    }
}
