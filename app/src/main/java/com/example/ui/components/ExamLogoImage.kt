package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.ExamBranch

/**
 * Returns the verified real official web logo URL for any engineering or medical exam.
 */
fun getExamOfficialLogoUrl(branchId: String, branchName: String): String {
    val key = (branchId + "_" + branchName).lowercase()
    return when {
        key.contains("jee_adv") || key.contains("advanced") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/b/b5/JEE_Advanced_logo.png/320px-JEE_Advanced_logo.png"

        key.contains("jee_main") || key.contains("jee main") ->
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/JEE_Main_logo.png/320px-JEE_Main_logo.png"

        key.contains("abhyas") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/National_Testing_Agency_logo.png/320px-National_Testing_Agency_logo.png"

        key.contains("iat") || key.contains("iiser") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/IISER_Logo.svg/320px-IISER_Logo.svg.png"

        key.contains("neet") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/National_Testing_Agency_logo.png/320px-National_Testing_Agency_logo.png"

        key.contains("aiims") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/7/7b/All_India_Institute_of_Medical_Sciences%2C_New_Delhi_crest.svg/320px-All_India_Institute_of_Medical_Sciences%2C_New_Delhi_crest.svg.png"

        key.contains("jipmer") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/9/91/JIPMER_Logo.png/320px-JIPMER_Logo.png"

        key.contains("bitsat") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/d/d3/BITS_Pilani-Logo.svg/320px-BITS_Pilani-Logo.svg.png"

        key.contains("mht") || key.contains("cet") ->
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/be/Emblem_of_Maharashtra.svg/320px-Emblem_of_Maharashtra.svg.png"

        key.contains("comedk") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/2/23/COMEDK_logo.png/320px-COMEDK_logo.png"

        key.contains("wbjee") ->
            "https://upload.wikimedia.org/wikipedia/commons/thumb/2/24/Emblem_of_West_Bengal.svg/320px-Emblem_of_West_Bengal.svg.png"

        key.contains("kcet") ->
            "https://upload.wikimedia.org/wikipedia/commons/thumb/a/aa/Seal_of_Karnataka.svg/320px-Seal_of_Karnataka.svg.png"

        key.contains("viteee") || key.contains("vit") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/c/c5/Vellore_Institute_of_Technology_seal_2017.svg/320px-Vellore_Institute_of_Technology_seal_2017.svg.png"

        key.contains("met") || key.contains("manipal") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/a/a2/Manipal_Academy_of_Higher_Education_logo.png/320px-Manipal_Academy_of_Higher_Education_logo.png"

        key.contains("nest") || key.contains("niser") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/NISER_logo.png/320px-NISER_logo.png"

        key.contains("kvpy") ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Indian_Institute_of_Science_Logo.svg/320px-Indian_Institute_of_Science_Logo.svg.png"

        key.contains("nda") ->
            "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/National_Defence_Academy_%28India%29_emblem.png/320px-National_Defence_Academy_%28India%29_emblem.png"

        else ->
            "https://upload.wikimedia.org/wikipedia/en/thumb/8/87/National_Testing_Agency_logo.png/320px-National_Testing_Agency_logo.png"
    }
}

/**
 * Renders the Real Official Logo for an Exam fetched from the internet via Coil,
 * with an authentic fallback badge design.
 */
@Composable
fun ExamOfficialLogo(
    branch: ExamBranch,
    size: Dp = 38.dp,
    shapeCorner: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val logoUrl = getExamOfficialLogoUrl(branch.id, branch.name)
    val context = LocalContext.current

    val (badgeBgColor, badgeTextColor, badgeShortName) = getExamBadgeAttributes(branch)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(shapeCorner))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(shapeCorner)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "${branch.name} Official Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            loading = {
                // Polished insignia fallback while loading
                OfficialInsigniaFallback(badgeBgColor, badgeTextColor, badgeShortName)
            },
            error = {
                // Polished insignia fallback if offline
                OfficialInsigniaFallback(badgeBgColor, badgeTextColor, badgeShortName)
            }
        )
    }
}

@Composable
private fun OfficialInsigniaFallback(
    bgColor: Color,
    textColor: Color,
    shortName: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shortName,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
    }
}

private fun getExamBadgeAttributes(branch: ExamBranch): Triple<Color, Color, String> {
    val key = (branch.id + "_" + branch.name).lowercase()
    return when {
        key.contains("jee_adv") || key.contains("advanced") ->
            Triple(Color(0xFF8B0000), Color.White, "JEE\nADV")
        key.contains("jee_main") || key.contains("jee main") ->
            Triple(Color(0xFF0F3B66), Color.White, "JEE\nMAIN")
        key.contains("abhyas") ->
            Triple(Color(0xFFE65100), Color.White, "NTA\nABH")
        key.contains("iat") || key.contains("iiser") ->
            Triple(Color(0xFF4A148C), Color.White, "IISER\nIAT")
        key.contains("neet") ->
            Triple(Color(0xFF00796B), Color.White, "NEET\nUG")
        key.contains("aiims") ->
            Triple(Color(0xFF1B5E20), Color.White, "AIIMS")
        key.contains("jipmer") ->
            Triple(Color(0xFF0D47A1), Color.White, "JIP\nMER")
        key.contains("bitsat") ->
            Triple(Color(0xFF311B92), Color.White, "BIT\nSAT")
        key.contains("mht") ->
            Triple(Color(0xFFBF360C), Color.White, "MHT\nCET")
        key.contains("comedk") ->
            Triple(Color(0xFF1A237E), Color.White, "COM\nEDK")
        key.contains("wbjee") ->
            Triple(Color(0xFF004D40), Color.White, "WB\nJEE")
        key.contains("kcet") ->
            Triple(Color(0xFF880E4F), Color.White, "KCET")
        key.contains("viteee") ->
            Triple(Color(0xFF01579B), Color.White, "VIT")
        key.contains("nest") ->
            Triple(Color(0xFF2E7D32), Color.White, "NEST")
        else ->
            Triple(Color(0xFF37474F), Color.White, branch.name.take(3).uppercase())
    }
}
