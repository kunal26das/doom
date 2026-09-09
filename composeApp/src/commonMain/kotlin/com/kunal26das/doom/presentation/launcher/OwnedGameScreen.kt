package com.kunal26das.doom.presentation.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunal26das.doom.presentation.GameHostState

@Composable
fun OwnedGameScreen(
    state: GameHostState,
    onSelected: (String, ByteArray) -> Unit,
    onError: (String) -> Unit,
    onPlayWithoutSaving: () -> Unit,
    onPlayDemo: () -> Unit,
) {
    val pickFile = rememberWadPicker(onSelected, onError)
    val drop = rememberWadDropState(!state.isBusy, onSelected, onError)
    Box(
        Modifier.fillMaxSize().background(Color(0xFF0D0B0A)),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isBusy) {
            Text("Opening your game…", color = Color(0xFFE8DDD2))
        } else {
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("DOOM", color = Color(0xFFD6A15A), fontSize = 64.sp, fontWeight = FontWeight.Black)
                Text("Play the original DOOM", color = Color(0xFFE8DDD2), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Play the free first episode, or load DOOM.WAD from your full game. Your imported game is remembered for future visits.",
                    color = Color(0xFFB9AAA0), fontSize = 16.sp, lineHeight = 24.sp,
                )
                Button(
                    onClick = onPlayDemo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD6A15A), contentColor = Color(0xFF1B1108)),
                ) {
                    Text("PLAY DEMO", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                }
                Text(
                    "Knee-Deep in the Dead · 9 levels · No game file needed",
                    color = Color(0xFFB9AAA0), fontSize = 13.sp, lineHeight = 20.sp,
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (drop.isDragging) Color(0xFF3B2C19) else Color(0xFF17120E), RoundedCornerShape(8.dp))
                        .border(if (drop.isDragging) 2.dp else 1.dp, if (drop.isDragging) Color(0xFFD6A15A) else Color(0xFF59432D), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (drop.supported) {
                        Text(
                            if (drop.isDragging) "Release to load your game" else "Drag & drop your WAD here",
                            color = Color(0xFFE8DDD2), fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        )
                        Text("DOOM.WAD or DOOM2.WAD · Up to 64 MB", color = Color(0xFFB9AAA0), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = pickFile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color(0xFF211A13), contentColor = Color(0xFFD6A15A)),
                    ) {
                        Text("LOAD DOOM.WAD", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                    }
                }
                if (state.error != null) {
                    Text(state.error, color = Color(0xFFFFB29B), fontSize = 14.sp, lineHeight = 21.sp)
                }
                if (state.canPlayWithoutSaving) {
                    TextButton(onClick = onPlayWithoutSaving) { Text("PLAY FOR THIS VISIT", color = Color(0xFFD6A15A)) }
                }
                Text(
                    "In Steam, open DOOM + DOOM II → Properties → Installed Files → Browse. Look for DOOM.WAD in the game folders. DOOM2.WAD also works.",
                    color = Color(0xFFB9AAA0), fontSize = 13.sp, lineHeight = 20.sp,
                )
                Text("Your file stays on this device. Nothing is uploaded.", color = Color(0xFFB9AAA0), fontSize = 12.sp)
            }
        }
    }
}
