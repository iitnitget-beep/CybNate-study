package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// BUMBLEBEE CYBERTRON CORE PALETTE
// Authentic Cybertronian Energon, Racing Yellow, Carbon Fiber & Gunmetal
// =========================================================================

// Bumblebee Primary Yellows (Electrifying Autobot Gold & Cybertron Yellow)
val BumblebeeYellow = Color(0xFFFFB800)        // Vibrant Autobot Bumblebee Gold
val BumblebeeYellowBright = Color(0xFFFFCC00)  // Radiant High-voltage Yellow
val BumblebeeYellowDark = Color(0xFFD97706)    // Deep Amber Gold
val BumblebeeYellowMuted = Color(0xFFB45309)   // Burnt Gold Accent
val BumblebeeGoldLight = Color(0xFFFEF3C7)    // Soft Yellow Container Tint

// Cybertronian Energon Blues & Cyberspace Neon
val EnergonBlue = Color(0xFF00E5FF)           // Glowing Energon Blue Optics / Core
val EnergonBlueBright = Color(0xFF38BDF8)     // Pulse Cyan
val EnergonBlueDark = Color(0xFF0284C7)       // Deep Energon Marine
val EnergonGlow = Color(0x6600E5FF)           // Semi-transparent Energon aura

// Cybertron Armor, Carbon Fiber, Obsidian & Steel Chassis
val CybertronObsidian = Color(0xFF0A0C10)     // Ultra-deep space Cybertron black
val CybertronArmor = Color(0xFF12151C)        // Cybernetic Chassis Dark surface
val CybertronCard = Color(0xFF1B202B)         // Raised Armor plating card
val CybertronCardElevated = Color(0xFF242A38) // Secondary modular armor panel
val CybertronSteel = Color(0xFF333B4F)        // Gunmetal chassis joints & borders
val CybertronSteelLight = Color(0xFF475569)   // Brushed Alloy
val CybertronBorder = Color(0xFF2E384D)       // High-tech outline wireframe

// Racing Stinger Stripes & Carbon Highlights
val BumblebeeStingerBlack = Color(0xFF0F1117) // Racing Stinger stripe black
val CybertronGrid = Color(0x22FFB800)         // Subtle yellow cyber grid

// Light Theme (Clean Bumblebee Mech White & Cyber Alloys)
val BumblebeeLightBg = Color(0xFFF9FAFB)      // Clean chassis ivory
val BumblebeeLightSurface = Color(0xFFFFFFFF) // Polished Titanium White
val BumblebeeLightSurfaceVariant = Color(0xFFF1F5F9)
val BumblebeeLightOutline = Color(0xFFE2E8F0)
val BumblebeeLightBorder = Color(0xFFCBD5E1)

// Status & Exam Feedback Colors (Cybertron HUD style)
val MarksSuccess = Color(0xFF10B981)          // Emerald HUD (Correct Answer)
val MarksError = Color(0xFFEF4444)            // Laser Red (Wrong Answer)
val MarksWarning = Color(0xFFF59E0B)          // Amber Hazard Alert (Timer Warning)
val MarksPurple = Color(0xFFA855F7)           // Energon Violet (Marked for Review)
val MarksNotVisited = Color(0xFF94A3B8)       // Steel Gray (Unvisited HUD)

// Legacy compatibility aliases pointing directly to Bumblebee Cybertron palette
val MarksPrimary = BumblebeeYellowDark        // High-contrast primary for accessible text
val MarksPrimaryDark = BumblebeeYellowMuted
val MarksPrimaryLight = BumblebeeYellow
val MarksSecondary = EnergonBlue
val MarksTertiary = Color(0xFFA855F7)

// Neutral Slates
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val CardDark = CybertronCard
val SurfaceDark = CybertronArmor
