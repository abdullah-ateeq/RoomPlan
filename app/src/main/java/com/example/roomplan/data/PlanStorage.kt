package com.example.roomplan.data

import android.content.Context
import android.content.SharedPreferences
import com.example.roomplan.model.Column
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PlanStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("room_plan_storage", Context.MODE_PRIVATE)

    companion object {
        const val MAX_PLANS = 20
        private const val KEY_PLANS_JSON = "key_plans_json"
        private const val KEY_LAST_OPENED_ID = "key_last_opened_id"

        fun serializePlan(plan: RoomPlan): JSONObject {
            val obj = JSONObject()
            obj.put("id", plan.id)
            obj.put("name", plan.name)
            obj.put("unit", plan.unit.name)
            obj.put("shape", plan.shape.name)
            obj.put("dimensionType", plan.dimensionType.name)
            obj.put("wallThicknessMm", plan.wallThicknessMm)
            obj.put("notes", plan.notes)
            obj.put("createdAt", plan.createdAt)
            obj.put("updatedAt", plan.updatedAt)

            // Room
            val roomObj = JSONObject()
            roomObj.put("lengthMm", plan.room.lengthMm)
            roomObj.put("widthMm", plan.room.widthMm)
            plan.room.heightMm?.let { roomObj.put("heightMm", it) }
            roomObj.put("cutoutLengthMm", plan.room.cutoutLengthMm)
            roomObj.put("cutoutWidthMm", plan.room.cutoutWidthMm)
            obj.put("room", roomObj)

            // Doors
            val doorsArr = JSONArray()
            for (door in plan.doors) {
                val dObj = JSONObject()
                dObj.put("id", door.id)
                dObj.put("widthMm", door.widthMm)
                dObj.put("wall", door.wall.name)
                dObj.put("offsetMm", door.offsetMm)
                dObj.put("type", door.type.name)
                dObj.put("swing", door.swing.name)
                doorsArr.put(dObj)
            }
            obj.put("doors", doorsArr)

            // Windows
            val windowsArr = JSONArray()
            for (win in plan.windows) {
                val wObj = JSONObject()
                wObj.put("id", win.id)
                wObj.put("widthMm", win.widthMm)
                wObj.put("wall", win.wall.name)
                wObj.put("offsetMm", win.offsetMm)
                win.sillHeightMm?.let { wObj.put("sillHeightMm", it) }
                win.heightMm?.let { wObj.put("heightMm", it) }
                windowsArr.put(wObj)
            }
            obj.put("windows", windowsArr)

            // Columns
            val colsArr = JSONArray()
            for (col in plan.columns) {
                val cObj = JSONObject()
                cObj.put("id", col.id)
                cObj.put("widthMm", col.widthMm)
                cObj.put("depthMm", col.depthMm)
                cObj.put("xMm", col.xMm)
                cObj.put("yMm", col.yMm)
                colsArr.put(cObj)
            }
            obj.put("columns", colsArr)

            // Furniture
            val furnArr = JSONArray()
            for (item in plan.furniture) {
                val fObj = JSONObject()
                fObj.put("id", item.id)
                fObj.put("type", item.type.name)
                fObj.put("name", item.name)
                fObj.put("widthMm", item.widthMm)
                fObj.put("depthMm", item.depthMm)
                item.heightMm?.let { fObj.put("heightMm", it) }
                fObj.put("xMm", item.xMm)
                fObj.put("yMm", item.yMm)
                fObj.put("rotationDegrees", item.rotationDegrees)
                furnArr.put(fObj)
            }
            obj.put("furniture", furnArr)

            return obj
        }

        fun parsePlan(obj: JSONObject): RoomPlan? {
            return try {
                val id = obj.getString("id")
                val name = obj.optString("name", "Untitled Plan")
                val unit = try {
                    UnitType.valueOf(obj.optString("unit", "CM"))
                } catch (e: Exception) {
                    UnitType.CM
                }
                val shape = try {
                    RoomShape.valueOf(obj.optString("shape", "RECTANGLE"))
                } catch (e: Exception) {
                    RoomShape.RECTANGLE
                }
                val dimensionType = try {
                    DimensionType.valueOf(obj.optString("dimensionType", "INSIDE"))
                } catch (e: Exception) {
                    DimensionType.INSIDE
                }
                val wallThicknessMm = obj.optDouble("wallThicknessMm", 150.0)
                val notes = obj.optString("notes", "")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

                val roomObj = obj.getJSONObject("room")
                val room = RoomDimensions(
                    lengthMm = roomObj.getDouble("lengthMm"),
                    widthMm = roomObj.getDouble("widthMm"),
                    heightMm = if (roomObj.has("heightMm")) roomObj.getDouble("heightMm") else null,
                    cutoutLengthMm = roomObj.optDouble("cutoutLengthMm", 0.0),
                    cutoutWidthMm = roomObj.optDouble("cutoutWidthMm", 0.0)
                )

                val doors = mutableListOf<Door>()
                val doorsArr = obj.optJSONArray("doors")
                if (doorsArr != null) {
                    for (i in 0 until doorsArr.length()) {
                        val d = doorsArr.getJSONObject(i)
                        doors.add(
                            Door(
                                id = d.getString("id"),
                                widthMm = d.getDouble("widthMm"),
                                wall = WallSide.valueOf(d.getString("wall")),
                                offsetMm = d.getDouble("offsetMm"),
                                type = DoorType.valueOf(d.optString("type", "HINGED")),
                                swing = DoorSwing.valueOf(d.optString("swing", "INWARD"))
                            )
                        )
                    }
                }

                val windows = mutableListOf<com.example.roomplan.model.Window>()
                val windowsArr = obj.optJSONArray("windows")
                if (windowsArr != null) {
                    for (i in 0 until windowsArr.length()) {
                        val w = windowsArr.getJSONObject(i)
                        windows.add(
                            com.example.roomplan.model.Window(
                                id = w.getString("id"),
                                widthMm = w.getDouble("widthMm"),
                                wall = WallSide.valueOf(w.getString("wall")),
                                offsetMm = w.getDouble("offsetMm"),
                                sillHeightMm = if (w.has("sillHeightMm")) w.getDouble("sillHeightMm") else null,
                                heightMm = if (w.has("heightMm")) w.getDouble("heightMm") else null
                            )
                        )
                    }
                }

                val columns = mutableListOf<Column>()
                val colsArr = obj.optJSONArray("columns")
                if (colsArr != null) {
                    for (i in 0 until colsArr.length()) {
                        val c = colsArr.getJSONObject(i)
                        columns.add(
                            Column(
                                id = c.getString("id"),
                                widthMm = c.getDouble("widthMm"),
                                depthMm = c.getDouble("depthMm"),
                                xMm = c.getDouble("xMm"),
                                yMm = c.getDouble("yMm")
                            )
                        )
                    }
                }

                val furniture = mutableListOf<FurnitureItem>()
                val furnArr = obj.optJSONArray("furniture")
                if (furnArr != null) {
                    for (i in 0 until furnArr.length()) {
                        val f = furnArr.getJSONObject(i)
                        furniture.add(
                            FurnitureItem(
                                id = f.getString("id"),
                                type = try {
                                    FurnitureType.valueOf(f.getString("type"))
                                } catch (e: Exception) {
                                    FurnitureType.CUSTOM
                                },
                                name = f.optString("name", "Furniture"),
                                widthMm = f.getDouble("widthMm"),
                                depthMm = f.getDouble("depthMm"),
                                heightMm = if (f.has("heightMm")) f.getDouble("heightMm") else null,
                                xMm = f.getDouble("xMm"),
                                yMm = f.getDouble("yMm"),
                                rotationDegrees = f.optInt("rotationDegrees", 0)
                            )
                        )
                    }
                }

                RoomPlan(
                    id = id,
                    name = name,
                    room = room,
                    unit = unit,
                    shape = shape,
                    dimensionType = dimensionType,
                    wallThicknessMm = wallThicknessMm,
                    doors = doors,
                    windows = windows,
                    columns = columns,
                    furniture = furniture,
                    notes = notes,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Synchronized
    fun getAllPlans(): List<RoomPlan> {
        val jsonStr = prefs.getString(KEY_PLANS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<RoomPlan>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsePlan(obj)?.let { list.add(it) }
            }
            list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Synchronized
    fun getPlan(id: String): RoomPlan? {
        return getAllPlans().firstOrNull { it.id == id }
    }

    @Synchronized
    fun savePlan(plan: RoomPlan): Result<RoomPlan> {
        val currentPlans = getAllPlans().toMutableList()
        val index = currentPlans.indexOfFirst { it.id == plan.id }

        if (index == -1 && currentPlans.size >= MAX_PLANS) {
            return Result.failure(
                IllegalStateException("Maximum plan limit reached ($MAX_PLANS). Please delete an existing plan.")
            )
        }

        val updatedPlan = plan.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            currentPlans[index] = updatedPlan
        } else {
            currentPlans.add(0, updatedPlan)
        }

        saveAll(currentPlans)
        prefs.edit().putString(KEY_LAST_OPENED_ID, updatedPlan.id).apply()
        return Result.success(updatedPlan)
    }

    @Synchronized
    fun deletePlan(id: String): Boolean {
        val currentPlans = getAllPlans().toMutableList()
        val removed = currentPlans.removeAll { it.id == id }
        if (removed) {
            saveAll(currentPlans)
        }
        return removed
    }

    @Synchronized
    fun duplicatePlan(id: String): RoomPlan? {
        val currentPlans = getAllPlans().toMutableList()
        if (currentPlans.size >= MAX_PLANS) {
            return null
        }
        val target = currentPlans.firstOrNull { it.id == id } ?: return null
        val newId = UUID.randomUUID().toString()
        val duplicated = target.copy(
            id = newId,
            name = "${target.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        currentPlans.add(0, duplicated)
        saveAll(currentPlans)
        return duplicated
    }

    @Synchronized
    fun renamePlan(id: String, newName: String): RoomPlan? {
        val currentPlans = getAllPlans().toMutableList()
        val index = currentPlans.indexOfFirst { it.id == id }
        if (index < 0) return null
        val updated = currentPlans[index].copy(
            name = newName.trim(),
            updatedAt = System.currentTimeMillis()
        )
        currentPlans[index] = updated
        saveAll(currentPlans)
        return updated
    }

    fun getLastOpenedPlanId(): String? {
        return prefs.getString(KEY_LAST_OPENED_ID, null)
    }

    private fun saveAll(plans: List<RoomPlan>) {
        val jsonArray = JSONArray()
        for (plan in plans) {
            jsonArray.put(serializePlan(plan))
        }
        prefs.edit().putString(KEY_PLANS_JSON, jsonArray.toString()).apply()
    }
}
