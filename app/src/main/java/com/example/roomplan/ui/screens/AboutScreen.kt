package com.example.roomplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.ui.theme.AccentEmerald
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.BlueprintDark
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About RoomPlan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueprintDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "RoomPlan",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROOMPLAN — MEASURE YOUR SPACE. PLAN YOUR ROOM.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "An offline room dimension and proportional floor-plan generator with automatic scaling, architectural wall thickness, doors, windows, structural columns, furniture layout blocks, and multi-format export.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }

            // ARCHITECTURAL DISCLAIMER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARCHITECTURAL DISCLAIMER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RoomPlan is a planning and visualization utility. It is not a replacement for architectural drawings, professional site measurement, structural design, building codes, or construction documentation.\n\nDimensions should always be verified on site before construction or furniture fabrication.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 17.sp
                    )
                }
            }

            // PRIVACY & OFFLINE COMMITMENT
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AccentEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVACY & 100% OFFLINE OPERATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AccentEmerald
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All calculations, geometry engines, and layouts are created locally on your device.\n\nRoomPlan requires zero internet permission. No accounts, no backend, no cloud synchronization, no ads, no trackers, and no AI APIs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 17.sp
                    )
                }
            }

            // KEY FEATURES
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FEATURES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val features = listOf(
                        "Precise Room Dimensions (mm, cm, m, in, ft)",
                        "Inside vs Outside wall-to-wall measurements",
                        "Live Area (m², ft²) & Perimeter (m, ft) calculations",
                        "Gross and Net Wall Surface Area calculations",
                        "Shapes: Rectangle, Square, and L-Shaped rooms",
                        "Architectural Wall Thickness (double-line representation)",
                        "Hinged & Sliding Doors with opening arcs and swing clearance",
                        "Break-in-wall Window symbols with height & sill levels",
                        "Structural Column blocks with cross-hatching",
                        "23 Furniture presets + custom proportional blocks",
                        "Deterministic Auto-Layout engine respecting clearances",
                        "Collision detection: overlap, boundary, column & door zone",
                        "Jetpack Compose Canvas with smooth pan and zoom (0.5×–8×)",
                        "Custom Grid intervals & Snap to Grid",
                        "Architectural dimension lines with 45° extension ticks",
                        "Up to 20 local saved plans with open, duplicate, rename & delete",
                        "High-res PNG image export",
                        "Printable PDF floor plan document export",
                        "Full plan JSON export & Furniture CSV export",
                        "Standard Android Share summary intent"
                    )

                    features.forEach { feat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                                tint = PrimaryBlue
                            )
                            Text(text = feat, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
