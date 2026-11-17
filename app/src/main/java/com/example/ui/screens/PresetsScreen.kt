package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.entity.PresetEntity
import com.example.model.LogoPlacement
import com.example.ui.components.GlassCard
import com.example.ui.components.NubccBrandHeader
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.studioAuraBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PresetsScreen(
    onNavigateBack: () -> Unit,
    onSelectPreset: (LogoPlacement) -> Unit,
    currentPlacement: LogoPlacement? = null
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val presets by db.presetDao().getAllPresets().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NubccBrandHeader(
                title = "WATERMARK PRESETS",
                subtitle = "Reusable logo placement configurations",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x337C3AED))
                            .testTag("add_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Preset",
                            tint = NubccCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = NubccCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Presets Saved",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NubccTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to save a custom watermark configuration",
                            fontSize = 12.sp,
                            color = NubccTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NubccTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Position: ${preset.presetName.replace("_", " ")} • Size: ${(preset.normalizedWidth * 100).roundToInt()}% • Opacity: ${(preset.opacity * 100).roundToInt()}%",
                                        fontSize = 11.sp,
                                        color = NubccTextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PrimaryButton(
                                        text = "USE",
                                        onClick = {
                                            onSelectPreset(preset.toPlacement())
                                            onNavigateBack()
                                        },
                                        modifier = Modifier.height(36.dp),
                                        testTag = "use_preset_${preset.id}"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                db.presetDao().deletePresetById(preset.id)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_preset_${preset.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete preset",
                                            tint = Color(0x99FF5555),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Preset Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF0F1B30),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        text = "Save As Preset",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NubccTextPrimary
                    )
                },
                text = {
                    Column {
                        val placementToSave = currentPlacement ?: LogoPlacement.defaultPlacement()
                        Text(
                            text = if (currentPlacement != null) {
                                "Save your active setup (${placementToSave.preset.displayName}, ${(placementToSave.normalizedWidth * 100).roundToInt()}% size) as a template:"
                            } else {
                                "Save the default bottom-right 18% configuration as a named template:"
                            },
                            fontSize = 12.sp,
                            color = NubccTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Preset Name", color = NubccCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NubccTextPrimary,
                                unfocusedTextColor = NubccTextPrimary,
                                focusedBorderColor = NubccCyan,
                                unfocusedBorderColor = NubccGlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                        )
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "SAVE",
                        onClick = {
                            if (newPresetName.isNotBlank()) {
                                coroutineScope.launch {
                                    db.presetDao().insertPreset(
                                        PresetEntity.fromPlacement(
                                            name = newPresetName.trim(),
                                            placement = currentPlacement ?: LogoPlacement.defaultPlacement()
                                        )
                                    )
                                    newPresetName = ""
                                    showAddDialog = false
                                }
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        testTag = "dialog_save_preset_confirm"
                    )
                },
                dismissButton = {
                    SecondaryButton(
                        text = "CANCEL",
                        onClick = { showAddDialog = false },
                        modifier = Modifier.height(40.dp)
                    )
                }
            )
        }
    }
}
