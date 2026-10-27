package com.example.roomplan.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.roomplan.engine.RoomCalculator
import com.example.roomplan.engine.RoomPlanGenerator
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.WallSide
import java.io.OutputStream
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object ExportManager {

    /**
     * Generates a high-resolution Bitmap of the floor plan.
     */
    fun renderPlanToBitmap(
        plan: RoomPlan,
        width: Int = 1800,
        height: Int = 1400
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 32f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 44f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 26f
        }

        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        val wallFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val furnitureStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235) // Architectural blue
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val furnitureFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 59, 130, 246)
            style = Paint.Style.FILL
        }

        val dimensionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val dimTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 185, 129) // Emerald
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val doorArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }

        val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(14, 165, 233) // Sky blue
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        val columnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 3f
        }

        // Header drawing
        val margin = 80f
        canvas.drawText("ROOMPLAN — ${plan.name.uppercase(Locale.US)}", margin, margin + 30f, titlePaint)

        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val areaFt2 = RoomCalculator.mm2ToFt2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val perimM = RoomCalculator.calculatePerimeter(plan.room, plan.shape) / 1000.0

        val dimHeader = String.format(
            Locale.US,
            "Dimensions: %s × %s | Area: %.2f m² (%.1f ft²) | Perimeter: %.2f m | Unit: %s",
            plan.unit.format(plan.room.lengthMm),
            plan.unit.format(plan.room.widthMm),
            areaM2,
            areaFt2,
            perimM,
            plan.unit.displayName
        )
        canvas.drawText(dimHeader, margin, margin + 70f, subtitlePaint)

        // Plan drawing region
        val planTop = margin + 140f
        val planBottom = height - margin - 80f
        val planWidth = width - 2 * margin
        val planHeight = planBottom - planTop

        val totalMmX = plan.room.lengthMm + (plan.wallThicknessMm * 2)
        val totalMmY = plan.room.widthMm + (plan.wallThicknessMm * 2)

        val scale = min(planWidth / totalMmX.toFloat(), planHeight / totalMmY.toFloat()) * 0.85f
        val drawnW = totalMmX.toFloat() * scale
        val drawnH = totalMmY.toFloat() * scale
        val originX = margin + (planWidth - drawnW) / 2f + (plan.wallThicknessMm.toFloat() * scale)
        val originY = planTop + (planHeight - drawnH) / 2f + (plan.wallThicknessMm.toFloat() * scale)

        fun toX(xMm: Double) = originX + (xMm.toFloat() * scale)
        fun toY(yMm: Double) = originY + (yMm.toFloat() * scale)

        // Draw Room Floor Fill & Boundary
        val rLeft = toX(0.0)
        val rTop = toY(0.0)
        val rRight = toX(plan.room.lengthMm)
        val rBottom = toY(plan.room.widthMm)

        if (plan.shape == RoomShape.L_SHAPE && plan.room.cutoutLengthMm > 0 && plan.room.cutoutWidthMm > 0) {
            val cutX = toX(plan.room.lengthMm - plan.room.cutoutLengthMm)
            val cutY = toY(plan.room.cutoutWidthMm)
            val path = Path().apply {
                moveTo(rLeft, rTop)
                lineTo(cutX, rTop)
                lineTo(cutX, cutY)
                lineTo(rRight, cutY)
                lineTo(rRight, rBottom)
                lineTo(rLeft, rBottom)
                close()
            }
            canvas.drawPath(path, wallFillPaint)
            canvas.drawPath(path, wallPaint)
        } else {
            val rect = RectF(rLeft, rTop, rRight, rBottom)
            canvas.drawRect(rect, wallFillPaint)
            canvas.drawRect(rect, wallPaint)
        }

        // Draw Windows
        for (win in plan.windows) {
            val wStart = win.offsetMm
            val wEnd = win.offsetMm + win.widthMm
            when (win.wall) {
                WallSide.TOP -> {
                    val y = toY(0.0)
                    canvas.drawLine(toX(wStart), y, toX(wEnd), y, windowPaint)
                    canvas.drawLine(toX(wStart), y - 10f, toX(wEnd), y - 10f, windowPaint)
                }
                WallSide.BOTTOM -> {
                    val y = toY(plan.room.widthMm)
                    canvas.drawLine(toX(wStart), y, toX(wEnd), y, windowPaint)
                    canvas.drawLine(toX(wStart), y + 10f, toX(wEnd), y + 10f, windowPaint)
                }
                WallSide.LEFT -> {
                    val x = toX(0.0)
                    canvas.drawLine(x, toY(wStart), x, toY(wEnd), windowPaint)
                    canvas.drawLine(x - 10f, toY(wStart), x - 10f, toY(wEnd), windowPaint)
                }
                WallSide.RIGHT -> {
                    val x = toX(plan.room.lengthMm)
                    canvas.drawLine(x, toY(wStart), x, toY(wEnd), windowPaint)
                    canvas.drawLine(x + 10f, toY(wStart), x + 10f, toY(wEnd), windowPaint)
                }
            }
        }

        // Draw Doors
        for (door in plan.doors) {
            val dWidthPx = (door.widthMm.toFloat() * scale)
            when (door.wall) {
                WallSide.BOTTOM -> {
                    val dx = toX(door.offsetMm)
                    val dy = toY(plan.room.widthMm)
                    // Door opening line
                    canvas.drawLine(dx, dy - dWidthPx, dx, dy, doorPaint)
                    // Swing arc
                    val arcRect = RectF(dx - dWidthPx, dy - dWidthPx, dx + dWidthPx, dy + dWidthPx)
                    canvas.drawArc(arcRect, 270f, 90f, false, doorArcPaint)
                }
                WallSide.TOP -> {
                    val dx = toX(door.offsetMm)
                    val dy = toY(0.0)
                    canvas.drawLine(dx, dy, dx, dy + dWidthPx, doorPaint)
                    val arcRect = RectF(dx - dWidthPx, dy - dWidthPx, dx + dWidthPx, dy + dWidthPx)
                    canvas.drawArc(arcRect, 0f, 90f, false, doorArcPaint)
                }
                WallSide.LEFT -> {
                    val dx = toX(0.0)
                    val dy = toY(door.offsetMm)
                    canvas.drawLine(dx, dy, dx + dWidthPx, dy, doorPaint)
                    val arcRect = RectF(dx - dWidthPx, dy - dWidthPx, dx + dWidthPx, dy + dWidthPx)
                    canvas.drawArc(arcRect, 0f, 90f, false, doorArcPaint)
                }
                WallSide.RIGHT -> {
                    val dx = toX(plan.room.lengthMm)
                    val dy = toY(door.offsetMm)
                    canvas.drawLine(dx - dWidthPx, dy, dx, dy, doorPaint)
                    val arcRect = RectF(dx - dWidthPx, dy - dWidthPx, dx + dWidthPx, dy + dWidthPx)
                    canvas.drawArc(arcRect, 90f, 90f, false, doorArcPaint)
                }
            }
        }

        // Draw Columns
        for (col in plan.columns) {
            val cx = toX(col.xMm)
            val cy = toY(col.yMm)
            val cw = col.widthMm.toFloat() * scale
            val cd = col.depthMm.toFloat() * scale
            canvas.drawRect(cx, cy, cx + cw, cy + cd, columnPaint)
        }

        // Draw Furniture
        val fLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val fSubLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }

        for (item in plan.furniture) {
            val fx = toX(item.xMm)
            val fy = toY(item.yMm)
            val fw = item.effectiveWidthMm.toFloat() * scale
            val fd = item.effectiveDepthMm.toFloat() * scale

            val fRect = RectF(fx, fy, fx + fw, fy + fd)
            canvas.drawRoundRect(fRect, 8f, 8f, furnitureFill)
            canvas.drawRoundRect(fRect, 8f, 8f, furnitureStroke)

            // Label
            if (fw > 40f && fd > 30f) {
                val cx = fx + fw / 2f
                val cy = fy + fd / 2f
                canvas.drawText(item.name, cx, cy - 4f, fLabelPaint)
                val dimStr = "${plan.unit.formatPlain(item.effectiveWidthMm)} × ${plan.unit.formatPlain(item.effectiveDepthMm)}"
                canvas.drawText(dimStr, cx, cy + 18f, fSubLabelPaint)
            }
        }

        // Draw Architectural Dimension Lines
        // Top dimension (Length)
        val dimOffset = 40f
        val topDimY = rTop - dimOffset
        canvas.drawLine(rLeft, topDimY, rRight, topDimY, dimensionPaint)
        // Extension lines
        canvas.drawLine(rLeft, rTop - 10f, rLeft, topDimY - 15f, dimensionPaint)
        canvas.drawLine(rRight, rTop - 10f, rRight, topDimY - 15f, dimensionPaint)
        // End ticks
        canvas.drawLine(rLeft - 10f, topDimY + 10f, rLeft + 10f, topDimY - 10f, dimensionPaint)
        canvas.drawLine(rRight - 10f, topDimY + 10f, rRight + 10f, topDimY - 10f, dimensionPaint)
        // Label
        canvas.drawText(plan.unit.format(plan.room.lengthMm), (rLeft + rRight) / 2f, topDimY - 10f, dimTextPaint)

        // Left dimension (Width)
        val leftDimX = rLeft - dimOffset
        canvas.drawLine(leftDimX, rTop, leftDimX, rBottom, dimensionPaint)
        canvas.drawLine(rLeft - 10f, rTop, leftDimX - 15f, rTop, dimensionPaint)
        canvas.drawLine(rLeft - 10f, rBottom, leftDimX - 15f, rBottom, dimensionPaint)
        canvas.drawLine(leftDimX - 10f, rTop + 10f, leftDimX + 10f, rTop - 10f, dimensionPaint)
        canvas.drawLine(leftDimX - 10f, rBottom + 10f, leftDimX + 10f, rBottom - 10f, dimensionPaint)

        canvas.save()
        canvas.rotate(-90f, leftDimX - 15f, (rTop + rBottom) / 2f)
        canvas.drawText(plan.unit.format(plan.room.widthMm), leftDimX - 15f, (rTop + rBottom) / 2f, dimTextPaint)
        canvas.restore()

        // Footer note
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 20f
        }
        canvas.drawText(
            "RoomPlan — Proportional Floor Plan (Not to physical scale) | Offline Utility",
            margin,
            height - margin + 20f,
            footerPaint
        )

        return bitmap
    }

    /**
     * Exports a clean printable PDF document.
     */
    fun exportToPdf(plan: RoomPlan, outputStream: OutputStream) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Header
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        canvas.drawText("ROOMPLAN — FLOOR PLAN SPECIFICATION", 40f, 50f, titlePaint)
        canvas.drawText("Room Name: ${plan.name}", 40f, 75f, boldBodyPaint)

        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val perimM = RoomCalculator.calculatePerimeter(plan.room, plan.shape) / 1000.0
        val furnFootprint = RoomCalculator.mm2ToM2(RoomCalculator.calculateFurnitureFootprint(plan.furniture))
        val roomM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val utilPct = RoomCalculator.calculateRoomUtilization(furnFootprint, roomM2)

        val stats = listOf(
            "Dimensions: ${plan.unit.format(plan.room.lengthMm)} × ${plan.unit.format(plan.room.widthMm)}",
            "Floor Area: ${String.format(Locale.US, "%.2f m²", areaM2)}",
            "Perimeter: ${String.format(Locale.US, "%.2f m", perimM)}",
            "Furniture Items: ${plan.furniture.size} (Area: ${String.format(Locale.US, "%.2f m²", furnFootprint)})",
            "Room Utilization: ${String.format(Locale.US, "%.1f%%", utilPct)}",
            "Doors: ${plan.doors.size} | Windows: ${plan.windows.size} | Columns: ${plan.columns.size}"
        )

        var y = 95f
        stats.forEach { line ->
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 18f
        }

        // Render floor plan bitmap onto PDF page
        val planBitmap = renderPlanToBitmap(plan, 1400, 1000)
        val destRect = RectF(40f, y + 10f, 555f, y + 10f + (515f * (1000f / 1400f)))
        canvas.drawBitmap(planBitmap, null, destRect, null)
        planBitmap.recycle()

        // Notes and Disclaimer
        val footerY = 780f
        val noteText = if (plan.notes.isNotBlank()) "Notes: ${plan.notes}" else "Notes: None"
        canvas.drawText(noteText, 40f, footerY - 20f, bodyPaint)

        val disclaimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 9f
        }
        canvas.drawText(
            "Disclaimer: RoomPlan is a planning and visualization utility. Not a replacement for architectural drawings.",
            40f,
            footerY,
            disclaimerPaint
        )
        canvas.drawText("Scale: Proportional (Not to physical scale unless calibrated)", 40f, footerY + 14f, disclaimerPaint)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    /**
     * Generates CSV format for furniture list.
     */
    fun exportToCsv(plan: RoomPlan): String {
        val sb = StringBuilder()
        sb.append("Name,Type,Width(${plan.unit.symbol}),Depth(${plan.unit.symbol}),Height(${plan.unit.symbol}),X(${plan.unit.symbol}),Y(${plan.unit.symbol}),Rotation(deg),Width(mm),Depth(mm),X(mm),Y(mm)\n")
        for (f in plan.furniture) {
            sb.append("\"${f.name}\",")
            sb.append("${f.type.name},")
            sb.append("${plan.unit.formatPlain(f.widthMm)},")
            sb.append("${plan.unit.formatPlain(f.depthMm)},")
            sb.append("${f.heightMm?.let { plan.unit.formatPlain(it) } ?: "N/A"},")
            sb.append("${plan.unit.formatPlain(f.xMm)},")
            sb.append("${plan.unit.formatPlain(f.yMm)},")
            sb.append("${f.rotationDegrees},")
            sb.append("${f.widthMm},")
            sb.append("${f.depthMm},")
            sb.append("${f.xMm},")
            sb.append("${f.yMm}\n")
        }
        return sb.toString()
    }

    /**
     * Generates text share summary.
     */
    fun createShareSummary(plan: RoomPlan): String {
        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val areaFt2 = RoomCalculator.mm2ToFt2(RoomCalculator.calculateArea(plan.room, plan.shape))
        val perimM = RoomCalculator.calculatePerimeter(plan.room, plan.shape) / 1000.0

        return buildString {
            append("RoomPlan — ${plan.name}\n\n")
            append("Dimensions: ${plan.unit.format(plan.room.lengthMm)} × ${plan.unit.format(plan.room.widthMm)}\n")
            append("Shape: ${plan.shape.displayName}\n")
            append(String.format(Locale.US, "Area: %.2f m² (%.1f ft²)\n", areaM2, areaFt2))
            append(String.format(Locale.US, "Perimeter: %.2f m\n", perimM))
            append("Furniture Items: ${plan.furniture.size}\n")
            append("Doors: ${plan.doors.size}\n")
            append("Windows: ${plan.windows.size}\n")
            if (plan.notes.isNotBlank()) {
                append("\nNotes:\n${plan.notes}\n")
            }
            append("\nCreated with RoomPlan — Offline Room Dimension & Floor Plan Generator")
        }
    }
}
