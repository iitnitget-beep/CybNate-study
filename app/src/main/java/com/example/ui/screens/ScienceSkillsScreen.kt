package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BumblebeeYellow
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSecondary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScienceSkillsScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Science Skills Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Experimental Physics, Salt Analysis & Lab PYQs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("science_skills_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MarksPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🔬 Virtual Lab", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🧪 Salt Analysis", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📐 Constants & Dimensions", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("📝 Lab PYQs", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> VirtualLabSection()
                1 -> SaltAnalysisSection()
                2 -> PhysicalConstantsSection()
                3 -> LabPyqSection()
            }
        }
    }
}

// =============================================================================
// TAB 0: VIRTUAL LAB (Vernier Calipers & Micrometer Screw Gauge Simulator)
// =============================================================================
@Composable
private fun VirtualLabSection() {
    var toolType by remember { mutableIntStateOf(0) } // 0: Vernier, 1: Screw Gauge

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { toolType = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (toolType == 0) MarksPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (toolType == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vernier Calipers", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { toolType = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (toolType == 1) MarksPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (toolType == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Screw Gauge", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (toolType == 0) {
            item { VernierCalipersSimulator() }
        } else {
            item { ScrewGaugeSimulator() }
        }
    }
}

@Composable
private fun VernierCalipersSimulator() {
    // Distance in mm: from 0.0 to 50.0 mm
    var sliderValue by remember { mutableFloatStateOf(16.4f) }
    var zeroErrorOption by remember { mutableIntStateOf(0) } // 0: Nil, 1: Positive (+0.02 cm), 2: Negative (-0.03 cm)

    val zeroErrorCm = when (zeroErrorOption) {
        1 -> 0.02
        2 -> -0.03
        else -> 0.0
    }

    val msrMm = sliderValue.toInt()
    val remainderFraction = sliderValue - msrMm
    val vsrDiv = (remainderFraction * 10).roundToInt().coerceIn(0, 9)
    val leastCountCm = 0.01 // 0.1 mm = 0.01 cm

    val msrCm = msrMm / 10.0
    val vsrContributionCm = vsrDiv * leastCountCm
    val measuredReadingCm = msrCm + vsrContributionCm
    val correctedReadingCm = measuredReadingCm - zeroErrorCm

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vernier Calipers Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarksPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Least Count = 0.01 cm (0.1 mm)",
                        color = MarksPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jaw Position Slider
            Text(
                text = "Adjust Jaw Opening: ${String.format(Locale.US, "%.1f mm (%.2f cm)", sliderValue, sliderValue / 10f)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..50f,
                steps = 500,
                colors = SliderDefaults.colors(thumbColor = BumblebeeYellow, activeTrackColor = MarksPrimary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Zero Error Selection
            Text(
                text = "Zero Error Setting:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = zeroErrorOption == 0,
                    onClick = { zeroErrorOption = 0 },
                    label = { Text("None (0.00 cm)") }
                )
                FilterChip(
                    selected = zeroErrorOption == 1,
                    onClick = { zeroErrorOption = 1 },
                    label = { Text("+0.02 cm (Pos)") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MarksError.copy(alpha = 0.2f))
                )
                FilterChip(
                    selected = zeroErrorOption == 2,
                    onClick = { zeroErrorOption = 2 },
                    label = { Text("-0.03 cm (Neg)") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MarksWarning.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculation Breakdown Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Formula: Corrected = MSR + (VSR × LC) - (±Zero Error)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Main Scale Reading (MSR): ${String.format(Locale.US, "%.1f cm (%d mm)", msrCm, msrMm)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Vernier Coinciding Div (VSR): $vsrDiv division",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Zero Error: ${String.format(Locale.US, "%+.2f cm", zeroErrorCm)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Final Corrected Length = ${String.format(Locale.US, "%.2f cm", correctedReadingCm)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarksSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrewGaugeSimulator() {
    var spindleValue by remember { mutableFloatStateOf(4.38f) }
    var zeroErrorOption by remember { mutableIntStateOf(0) } // 0: Nil, 1: +0.03 mm, 2: -0.04 mm

    val zeroErrorMm = when (zeroErrorOption) {
        1 -> 0.03
        2 -> -0.04
        else -> 0.0
    }

    val psrMm = spindleValue.toInt()
    val remainderFraction = spindleValue - psrMm
    val csdDiv = (remainderFraction * 100).roundToInt().coerceIn(0, 99)
    val leastCountMm = 0.01 // 1 mm / 100 divisions

    val measuredReadingMm = psrMm + (csdDiv * leastCountMm)
    val correctedReadingMm = measuredReadingMm - zeroErrorMm

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Micrometer Screw Gauge Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarksSecondary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Least Count = 0.01 mm",
                        color = MarksSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Adjust Spindle: ${String.format(Locale.US, "%.2f mm", spindleValue)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = spindleValue,
                onValueChange = { spindleValue = it },
                valueRange = 0f..15f,
                steps = 1500,
                colors = SliderDefaults.colors(thumbColor = MarksSecondary, activeTrackColor = MarksSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Zero Error Setting:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = zeroErrorOption == 0,
                    onClick = { zeroErrorOption = 0 },
                    label = { Text("None") }
                )
                FilterChip(
                    selected = zeroErrorOption == 1,
                    onClick = { zeroErrorOption = 1 },
                    label = { Text("+0.03 mm (Pos)") }
                )
                FilterChip(
                    selected = zeroErrorOption == 2,
                    onClick = { zeroErrorOption = 2 },
                    label = { Text("-0.04 mm (Neg)") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Formula: Corrected = PSR + (CSD × LC) - (±Zero Error)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• Pitch Scale Reading (PSR): $psrMm mm", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Circular Scale Div (CSD): $csdDiv div", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Zero Error: ${String.format(Locale.US, "%+.2f mm", zeroErrorMm)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Final Corrected Diameter = ${String.format(Locale.US, "%.3f mm", correctedReadingMm)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MarksSuccess
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: SALT ANALYSIS (Qualitative Cation/Anion Detection Tree)
// =============================================================================
@Composable
private fun SaltAnalysisSection() {
    var selectedGroup by remember { mutableStateOf("Group 0") }

    val saltGroups = listOf("Group 0", "Group I", "Group II", "Group III", "Group IV", "Group V", "Anions")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Qualitative Salt Analysis Flowchart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Select an analytical group to inspect group reagents, precipitations & confirmatory tests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(saltGroups) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MarksPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            when (selectedGroup) {
                "Group 0" -> SaltCard(
                    title = "Group 0: Ammonium (NH₄⁺)",
                    reagent = "Dilute NaOH Solution (Heat gently)",
                    observation = "Pungent smelling gas evolves; turns red litmus blue.",
                    confirmatory = "Pass gas through Nessler's Reagent (K₂[HgI₄]) -> Produces brown precipitate (Iodide of Millon's base)."
                )
                "Group I" -> SaltCard(
                    title = "Group I: Lead (Pb²⁺)",
                    reagent = "Dilute HCl",
                    observation = "White precipitate of Lead Chloride (PbCl₂) soluble in boiling water.",
                    confirmatory = "Add Potassium Iodide (KI) to hot filtrate -> Golden yellow spangles of PbI₂ appear on cooling."
                )
                "Group II" -> SaltCard(
                    title = "Group II: Copper (Cu²⁺), Arsenic (As³⁺)",
                    reagent = "H₂S gas in acidic medium (dil. HCl)",
                    observation = "Black precipitate of CuS (Copper) or Yellow precipitate of As₂S₃.",
                    confirmatory = "Dissolve CuS in dil. HNO₃ and add excess NH₄OH -> Deep royal blue solution of [Cu(NH₃)₄]²⁺."
                )
                "Group III" -> SaltCard(
                    title = "Group III: Iron (Fe³⁺), Aluminum (Al³⁺)",
                    reagent = "NH₄OH in presence of excess solid NH₄Cl",
                    observation = "Reddish-brown precipitate of Fe(OH)₃ or gelatinous white precipitate of Al(OH)₃.",
                    confirmatory = "For Fe³⁺: Add Potassium Ferrocyanide -> Prussian Blue precipitate Fe₄[Fe(CN)₆]₃."
                )
                "Group IV" -> SaltCard(
                    title = "Group IV: Zinc (Zn²⁺), Manganese (Mn²⁺), Nickel (Ni²⁺)",
                    reagent = "H₂S gas in alkaline medium (NH₄OH + NH₄Cl)",
                    observation = "White/dirty white ZnS, buff colored MnS, or black NiS.",
                    confirmatory = "For Ni²⁺: Add Dimethylglyoxime (DMG) in ammoniacal solution -> Rosy red precipitate."
                )
                "Group V" -> SaltCard(
                    title = "Group V: Barium (Ba²⁺), Strontium (Sr²⁺), Calcium (Ca²⁺)",
                    reagent = "Ammonium Carbonate (NH₄)₂CO₃ in presence of NH₄Cl + NH₄OH",
                    observation = "White precipitate of carbonates (BaCO₃, SrCO₃, CaCO₃).",
                    confirmatory = "Flame Test:\n• Ba²⁺: Persistent Apple Green flame\n• Sr²⁺: Crimson Red flame\n• Ca²⁺: Brick Red flame"
                )
                else -> SaltCard(
                    title = "Acidic Radicals / Anions",
                    reagent = "Dilute H₂SO₄ & Concentrated H₂SO₄",
                    observation = "CO₃²⁻: Brisk effervescence of CO₂ (turns lime water milky)\nCl⁻: Colorless pungent gas HCl (white fumes with NH₄OH)",
                    confirmatory = "Brown Ring Test for Nitrate (NO₃⁻):\nAdd freshly prepared FeSO₄ solution and conc. H₂SO₄ down the side of test tube -> Dark brown ring of [Fe(H₂O)₅(NO)]SO₄ at the junction."
                )
            }
        }
    }
}

@Composable
private fun SaltCard(
    title: String,
    reagent: String,
    observation: String,
    confirmatory: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MarksPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Group Reagent:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = reagent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Initial Observation:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = observation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MarksSuccess.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Confirmatory Test (High-Yield):", fontWeight = FontWeight.Bold, color = MarksSuccess, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = confirmatory, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: PHYSICAL CONSTANTS & DIMENSIONAL FORMULAS
// =============================================================================
@Composable
private fun PhysicalConstantsSection() {
    var searchQuery by remember { mutableStateOf("") }

    val constants = listOf(
        ConstantItem("Planck's Constant (h)", "6.626 × 10⁻³⁴ J·s", "[M L² T⁻¹]", "Photoelectric effect, de Broglie wavelength"),
        ConstantItem("Permittivity of Free Space (ε₀)", "8.854 × 10⁻¹² F/m", "[M⁻¹ L⁻³ T⁴ I²]", "Coulomb's Law, Capacitance"),
        ConstantItem("Permeability of Free Space (μ₀)", "4π × 10⁻⁷ T·m/A", "[M L T⁻² I⁻²]", "Biot-Savart Law, Ampere's Law"),
        ConstantItem("Universal Gas Constant (R)", "8.314 J/(mol·K)", "[M L² T⁻² K⁻¹ mol⁻¹]", "Ideal Gas Equation PV = nRT"),
        ConstantItem("Boltzmann Constant (kB)", "1.380 × 10⁻²³ J/K", "[M L² T⁻² K⁻¹]", "Kinetic Theory of Gases"),
        ConstantItem("Gravitational Constant (G)", "6.674 × 10⁻¹¹ N·m²/kg²", "[M⁻¹ L³ T⁻²]", "Newton's Universal Gravitation"),
        ConstantItem("Stefan-Boltzmann Constant (σ)", "5.670 × 10⁻⁸ W/(m²·K⁴)", "[M T⁻³ K⁻⁴]", "Blackbody Radiation Law"),
        ConstantItem("Rydberg Constant (R_H)", "1.097 × 10⁷ m⁻¹", "[L⁻¹]", "Hydrogen Spectral Series"),
        ConstantItem("Speed of Light (c)", "3.00 × 10⁸ m/s", "[L T⁻¹]", "Electromagnetic wave propagation")
    )

    val filtered = constants.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.dimensions.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Constant or Dimension (e.g. h, ε₀, [M L²])") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        items(filtered) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MarksPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.dimensions,
                                color = MarksPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Value: ${item.value}", style = MaterialTheme.typography.bodyMedium, color = MarksSuccess, fontWeight = FontWeight.SemiBold)
                    Text(text = "Appears in: ${item.usage}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class ConstantItem(val name: String, val value: String, val dimensions: String, val usage: String)

// =============================================================================
// TAB 3: LAB EXPERIMENTAL PYQ PRACTICE SET
// =============================================================================
@Composable
private fun LabPyqSection() {
    var selectedQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showSolution by remember { mutableStateOf(false) }

    val labQuestions = listOf(
        LabQuestion(
            exam = "JEE Main 2024",
            text = "In a Vernier Calipers, 10 vernier scale divisions coincide with 9 main scale divisions each of 1 mm. If the zero of vernier lies to the right of zero of main scale and 3rd division coincides, the zero error is:",
            options = listOf("+0.3 mm", "-0.3 mm", "+0.03 mm", "-0.03 mm"),
            correctIndex = 0,
            explanation = "Least Count (LC) = 1 MSD - 1 VSD = 1 mm - 0.9 mm = 0.1 mm.\nWhen the vernier zero lies to the RIGHT of the main scale zero, the error is POSITIVE.\nZero Error = + (Coinciding division × LC) = + (3 × 0.1 mm) = +0.3 mm."
        ),
        LabQuestion(
            exam = "NEET 2023",
            text = "During the qualitative analysis of a salt, an aqueous solution gives a white precipitate with dil. HCl which dissolves on boiling and reappears on cooling. The cation present is:",
            options = listOf("Ag⁺", "Pb²⁺", "Hg₂²⁺", "Cu²⁺"),
            correctIndex = 1,
            explanation = "Pb²⁺ belongs to Group I. It precipitates as PbCl₂ upon addition of dil. HCl. Lead chloride (PbCl₂) is uniquely soluble in hot water and crystallizes out as white needle-shaped crystals on cooling."
        ),
        LabQuestion(
            exam = "JEE Main 2023",
            text = "The pitch of a screw gauge is 1 mm and there are 100 divisions on its circular scale. When measured, main scale reading is 3 mm and circular scale reading is 45. If the screw gauge has a negative zero error of -0.05 mm, the true diameter is:",
            options = listOf("3.40 mm", "3.45 mm", "3.50 mm", "3.55 mm"),
            correctIndex = 2,
            explanation = "Least Count (LC) = 1 mm / 100 = 0.01 mm.\nObserved Reading = PSR + (CSD × LC) = 3 mm + (45 × 0.01 mm) = 3.45 mm.\nTrue Reading = Observed - (Zero Error) = 3.45 mm - (-0.05 mm) = 3.45 + 0.05 = 3.50 mm."
        )
    )

    val currentQ = labQuestions[selectedQuestionIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarksPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = currentQ.exam,
                        color = MarksPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Question ${selectedQuestionIndex + 1} of ${labQuestions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = currentQ.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))

                    currentQ.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOption == idx
                        val isCorrect = idx == currentQ.correctIndex
                        val btnColor = when {
                            showSolution && isCorrect -> MarksSuccess.copy(alpha = 0.2f)
                            showSolution && isSelected && !isCorrect -> MarksError.copy(alpha = 0.2f)
                            isSelected -> MarksPrimary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!showSolution) selectedOption = idx
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = btnColor,
                            border = if (isSelected || (showSolution && isCorrect)) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, if (showSolution && isCorrect) MarksSuccess else MarksPrimary)
                            } else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + idx)}. ",
                                    fontWeight = FontWeight.Bold,
                                    color = if (showSolution && isCorrect) MarksSuccess else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showSolution = true },
                            enabled = selectedOption != null && !showSolution,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary)
                        ) {
                            Text("Check Solution")
                        }

                        Button(
                            onClick = {
                                selectedOption = null
                                showSolution = false
                                selectedQuestionIndex = (selectedQuestionIndex + 1) % labQuestions.size
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Next Question")
                        }
                    }

                    AnimatedVisibility(visible = showSolution) {
                        Column(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .background(MarksSuccess.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(text = "Detailed Lab Solution:", fontWeight = FontWeight.Bold, color = MarksSuccess)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = currentQ.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private data class LabQuestion(val exam: String, val text: String, val options: List<String>, val correctIndex: Int, val explanation: String)
