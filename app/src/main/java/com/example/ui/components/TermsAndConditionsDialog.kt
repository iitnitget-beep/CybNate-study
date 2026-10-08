package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MarksPrimary

/**
 * Comprehensive Terms & Conditions and Privacy Policy Dialog for CybNate.
 */
@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Terms & Conditions & Privacy Policy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CybNate Exam Preparation & Analytics Platform",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TermsSection(
                    title = "1. Acceptance of Terms",
                    content = "By creating an account, logging in, or using CybNate, you explicitly agree to these Terms and Conditions. If you do not agree with any provision, you must not access or use the application."
                )

                TermsSection(
                    title = "2. Educational Purpose & Fair Use",
                    content = "CybNate provides mock test series, practice questions, detailed solutions, and performance analytics specifically engineered for competitive examinations (including JEE, NEET, and foundation exams). All mock tests and analytical benchmarks are intended solely for personal, non-commercial self-study and learning assessment."
                )

                TermsSection(
                    title = "3. User Accounts & Security Lockout Policy",
                    content = "You are responsible for maintaining the confidentiality of your account credentials (email, password, and OTP). Sharing accounts or automated scraping of question repositories is strictly forbidden. To safeguard against brute-force attacks and unauthorized access, CybNate enforces a strict 5-attempt limit: 5 consecutive failed sign-in attempts result in an automatic 24-hour security lockout."
                )

                TermsSection(
                    title = "4. Data Collection, Privacy & Cloud Sync",
                    content = "When you register or log in, CybNate securely stores your profile identifier, test attempts, question palette status, scores, bookmarks, and timestamps in Google Cloud Firestore. We do not sell or lease your personal identity or exam performance metrics to third parties. Data is used exclusively to generate your personalized learning analytics, accuracy graphs, and error reviews."
                )

                TermsSection(
                    title = "5. Intellectual Property & Question Content",
                    content = "All proprietary software code, UI designs, brand assets (including the CybNate brand and Bumblebee face emblem), and curated explanatory frameworks belong to CybNate. Examination questions adapted from public previous years' papers (PYQs) belong to their respective examination bodies and are displayed under fair dealing for educational guidance."
                )

                TermsSection(
                    title = "6. Limitation of Liability & Exam Guarantees",
                    content = "While CybNate employs exam-accurate syllabus structures, scoring algorithms (-1 negative marking), and verified solutions, CybNate does not guarantee specific exam percentiles, ranks, or admissions. Test scores represent indicative practice performance under mock conditions."
                )

                TermsSection(
                    title = "7. Modifications & Termination",
                    content = "CybNate reserves the right to update curriculum question banks, feature sets, or security policies. Violation of these terms may result in account suspension without prior notice."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("I Agree & Accept", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
