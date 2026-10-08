package com.example.util

object MathFormatter {

    private val supMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'x' to 'ˣ', 'y' to 'ʸ', 'p' to 'ᵖ', 'q' to 'q',
        't' to 'ᵗ', 'a' to 'ᵃ', 'b' to 'ᵇ', 'm' to 'ᵐ', 'k' to 'ᵏ',
        'i' to 'ⁱ', 'r' to 'ʳ', 's' to 'ˢ', 'u' to 'ᵘ', 'v' to 'ᵛ'
    )

    private val subMap = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'o' to 'ₒ', 'x' to 'ₓ', 'y' to 'ᵧ',
        'n' to 'ₙ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ', 'l' to 'ₗ',
        'm' to 'ₘ', 'p' to 'ₚ', 's' to 'ₛ', 't' to 'ₜ', 'r' to 'ᵣ'
    )

    private val htmlEntities = mapOf(
        "&plusmn;" to "±", "&times;" to "×", "&divide;" to "÷", "&le;" to "≤", "&ge;" to "≥",
        "&ne;" to "≠", "&approx;" to "≈", "&infin;" to "∞", "&int;" to "∫", "&sum;" to "∑",
        "&prod;" to "∏", "&radic;" to "√", "&alpha;" to "α", "&beta;" to "β", "&gamma;" to "γ",
        "&delta;" to "δ", "&epsilon;" to "ε", "&theta;" to "θ", "&lambda;" to "λ", "&mu;" to "μ",
        "&pi;" to "π", "&sigma;" to "σ", "&tau;" to "τ", "&phi;" to "φ", "&omega;" to "ω",
        "&Delta;" to "Δ", "&Omega;" to "Ω", "&Sigma;" to "Σ", "&Pi;" to "Π", "&deg;" to "°",
        "&minus;" to "−", "&rarr;" to "→", "&larr;" to "←", "&harr;" to "↔", "&rArr;" to "⇒",
        "&lArr;" to "⇐", "&hArr;" to "⇔", "&isin;" to "∈", "&notin;" to "∉", "&sub;" to "⊂",
        "&sup;" to "⊃", "&cap;" to "∩", "&cup;" to "∪", "&part;" to "∂", "&nabla;" to "∇",
        "&prime;" to "′", "&Prime;" to "″", "&hellip;" to "...", "&#8216;" to "'",
        "&#8217;" to "'", "&#8220;" to "\"", "&#8221;" to "\"", "&nbsp;" to " ",
        "&amp;" to "&", "&lt;" to "<", "&gt;" to ">", "&perp;" to "⊥", "&ang;" to "∠"
    )

    private val greekMap = listOf(
        Regex("""\\alpha\b""") to "α",
        Regex("""\\beta\b""") to "β",
        Regex("""\\gamma\b""") to "γ",
        Regex("""\\Gamma\b""") to "Γ",
        Regex("""\\delta\b""") to "δ",
        Regex("""\\Delta\b""") to "Δ",
        Regex("""\\epsilon\b|\\varepsilon\b""") to "ε",
        Regex("""\\zeta\b""") to "ζ",
        Regex("""\\eta\b""") to "η",
        Regex("""\\theta\b""") to "θ",
        Regex("""\\Theta\b""") to "Θ",
        Regex("""\\iota\b""") to "ι",
        Regex("""\\kappa\b""") to "κ",
        Regex("""\\lambda\b""") to "λ",
        Regex("""\\Lambda\b""") to "Λ",
        Regex("""\\mu\b""") to "μ",
        Regex("""\\nu\b""") to "ν",
        Regex("""\\xi\b""") to "ξ",
        Regex("""\\pi\b""") to "π",
        Regex("""\\Pi\b""") to "Π",
        Regex("""\\rho\b""") to "ρ",
        Regex("""\\sigma\b""") to "σ",
        Regex("""\\Sigma\b""") to "Σ",
        Regex("""\\tau\b""") to "τ",
        Regex("""\\upsilon\b""") to "υ",
        Regex("""\\phi\b|\\varphi\b""") to "φ",
        Regex("""\\Phi\b""") to "Φ",
        Regex("""\\chi\b""") to "χ",
        Regex("""\\psi\b""") to "ψ",
        Regex("""\\omega\b""") to "ω",
        Regex("""\\Omega\b""") to "Ω"
    )

    private val symbolMap = listOf(
        Regex("""\\pm\b""") to "±",
        Regex("""\\mp\b""") to "∓",
        Regex("""\\times\b""") to "×",
        Regex("""\\cdot\b""") to "·",
        Regex("""\\div\b""") to "÷",
        Regex("""\\leq\b|\\le\b""") to "≤",
        Regex("""\\geq\b|\\ge\b""") to "≥",
        Regex("""\\neq\b|\\ne\b""") to "≠",
        Regex("""\\approx\b""") to "≈",
        Regex("""\\equiv\b""") to "≡",
        Regex("""\\sim\b""") to "~",
        Regex("""\\propto\b""") to "∝",
        Regex("""\\infty\b""") to "∞",
        Regex("""\\to\b|\\rightarrow\b|\\longrightarrow\b""") to "→",
        Regex("""\\leftarrow\b|\\longleftarrow\b""") to "←",
        Regex("""\\Rightarrow\b|\\implies\b|\\Longrightarrow\b""") to "⇒",
        Regex("""\\Leftarrow\b""") to "⇐",
        Regex("""\\leftrightarrow\b""") to "↔",
        Regex("""\\Leftrightarrow\b|\\iff\b""") to "⇔",
        Regex("""\\degree\b|\^\\circ\b""") to "°",
        Regex("""\\in\b""") to "∈",
        Regex("""\\notin\b""") to "∉",
        Regex("""\\subset\b""") to "⊂",
        Regex("""\\supset\b""") to "⊃",
        Regex("""\\subseteq\b""") to "⊆",
        Regex("""\\supseteq\b""") to "⊇",
        Regex("""\\cup\b""") to "∪",
        Regex("""\\cap\b""") to "∩",
        Regex("""\\iint\b""") to "∬",
        Regex("""\\iiint\b""") to "∭",
        Regex("""\\oint\b""") to "∮",
        Regex("""\\partial\b""") to "∂",
        Regex("""\\nabla\b""") to "∇",
        Regex("""\\forall\b""") to "∀",
        Regex("""\\exists\b""") to "∃",
        Regex("""\\because\b""") to "∵",
        Regex("""\\therefore\b""") to "∴",
        Regex("""\\angle\b""") to "∠",
        Regex("""\\triangle\b""") to "△",
        Regex("""\\perp\b""") to "⊥",
        Regex("""\\parallel\b""") to "∥",
        Regex("""\\dots\b|\\ldots\b|\\cdots\b|\\vdots\b|\\ddots\b""") to "...",
        Regex("""\\prime\b""") to "′",
        Regex("""\\mid\b""") to "|",
        Regex("""\\langle\b""") to "⟨",
        Regex("""\\rangle\b""") to "⟩",
        Regex("""\\mathbb\{R\}""") to "ℝ",
        Regex("""\\mathbb\{C\}""") to "ℂ",
        Regex("""\\mathbb\{Z\}""") to "ℤ",
        Regex("""\\mathbb\{N\}""") to "ℕ",
        Regex("""\\mathbb\{Q\}""") to "ℚ",
        Regex("""\\quad\b""") to "   ",
        Regex("""\\qquad\b""") to "    ",
        Regex("""\\,\b|\\:\b|\\;\b|\\!\b""") to " "
    )

    private val standardFunctions = listOf(
        "sin", "cos", "tan", "cot", "sec", "csc",
        "arcsin", "arccos", "arctan",
        "sinh", "cosh", "tanh",
        "log", "ln", "exp", "max", "min", "det", "gcd", "deg"
    )

    /**
     * Converts MathML markup (<math>...</math>) into readable unicode mathematical text.
     * This preserves fractions, powers, roots, and symbols instead of stripping them.
     */
    fun convertMathML(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var s: String = raw

        // Decode HTML entities in MathML
        for ((ent, rep) in htmlEntities) {
            s = s.replace(ent, rep)
        }

        // Handle fractions: <mfrac> NUM DEN </mfrac>
        var prev = ""
        var iters = 0
        while (prev != s && iters < 5) {
            prev = s
            s = s.replace(Regex("""<mfrac[^>]*>(.*?)</mfrac>""", RegexOption.DOT_MATCHES_ALL)) { m ->
                val inner = m.groupValues[1].trim()
                val mrowMatches = Regex("""<mrow[^>]*>(.*?)</mrow>""", RegexOption.DOT_MATCHES_ALL).findAll(inner).map { it.groupValues[1] }.toList()
                val num: String
                val den: String
                if (mrowMatches.size >= 2) {
                    num = cleanInnerMath(mrowMatches[0]).trim()
                    den = cleanInnerMath(mrowMatches[1]).trim()
                } else {
                    val childTags = Regex("""<([a-zA-Z]+)[^>]*>.*?</\1>""", RegexOption.DOT_MATCHES_ALL).findAll(inner).map { it.value }.toList()
                    if (childTags.size >= 2) {
                        num = cleanInnerMath(childTags[0]).trim()
                        den = cleanInnerMath(childTags[1]).trim()
                    } else {
                        num = cleanInnerMath(inner).trim()
                        den = ""
                    }
                }
                formatFraction(num, den)
            }
            iters++
        }

        // Handle square roots: <msqrt> <mrow>CONTENT</mrow> </msqrt>
        s = s.replace(Regex("""<msqrt[^>]*>\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*</msqrt>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            "√(${cleanInnerMath(m.groupValues[1]).trim()})"
        }

        // Handle nth roots: <mroot> <mrow>BASE</mrow> <mrow>ROOT</mrow> </mroot>
        s = s.replace(Regex("""<mroot[^>]*>\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*</mroot>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            val base = cleanInnerMath(m.groupValues[1]).trim()
            val root = cleanInnerMath(m.groupValues[2]).trim()
            "($root)√($base)"
        }

        // Handle sub+sup: <msubsup> BASE SUB SUP </msubsup>
        s = s.replace(Regex("""<msubsup[^>]*>\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*</msubsup>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            val base = cleanInnerMath(m.groupValues[1]).trim()
            val sub = toSubscript(cleanInnerMath(m.groupValues[2]).trim())
            val sup = toSuperscript(cleanInnerMath(m.groupValues[3]).trim())
            "$base$sub$sup"
        }

        // Handle superscripts: <msup> BASE EXP </msup>
        s = s.replace(Regex("""<msup[^>]*>\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*</msup>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            val base = cleanInnerMath(m.groupValues[1]).trim()
            val exp = toSuperscript(cleanInnerMath(m.groupValues[2]).trim())
            "$base$exp"
        }

        // Handle subscripts: <msub> BASE SUB </msub>
        s = s.replace(Regex("""<msub[^>]*>\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*(?:<mrow[^>]*>)?(.*?)(?:</mrow>)?\s*</msub>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            val base = cleanInnerMath(m.groupValues[1]).trim()
            val sub = toSubscript(cleanInnerMath(m.groupValues[2]).trim())
            "$base$sub"
        }

        // Handle tables/matrices: <mtable> <mtr> <mtd>A</mtd> <mtd>B</mtd> </mtr> </mtable>
        s = s.replace(Regex("""<mtable[^>]*>(.*?)</mtable>""", RegexOption.DOT_MATCHES_ALL)) { m ->
            val rows = Regex("""<mtr[^>]*>(.*?)</mtr>""", RegexOption.DOT_MATCHES_ALL).findAll(m.groupValues[1]).map { rowMatch ->
                val cols = Regex("""<mtd[^>]*>(.*?)</mtd>""", RegexOption.DOT_MATCHES_ALL).findAll(rowMatch.groupValues[1]).map { colMatch ->
                    cleanInnerMath(colMatch.groupValues[1]).trim()
                }.joinToString("  ")
                "| $cols |"
            }.joinToString("\n")
            "\n$rows\n"
        }

        // Remove <math>, </math>, <mrow>, </mrow>, <mi>, <mo>, <mn>, <mtext> tags
        s = cleanInnerMath(s)
        return s
    }

    private fun cleanInnerMath(str: String): String {
        return str.replace(Regex("""</?(?:math|mrow|mi|mo|mn|mtext|mstyle|mspace)[^>]*>""", RegexOption.IGNORE_CASE), " ")
    }

    /**
     * Converts a raw exponent string to clean unicode superscripts if possible.
     */
    fun toSuperscript(content: String): String {
        val trimmed = content.trim()
        val mapped = trimmed.map { c -> supMap[c] ?: c }.joinToString("")
        return if (mapped.any { it in supMap.values }) mapped else "^($trimmed)"
    }

    /**
     * Converts a raw subscript string to clean unicode subscripts if possible.
     */
    fun toSubscript(content: String): String {
        val trimmed = content.trim()
        val mapped = trimmed.map { c -> subMap[c] ?: c }.joinToString("")
        return if (mapped.any { it in subMap.values }) mapped else "_($trimmed)"
    }

    private fun formatFraction(num: String, den: String): String {
        if (num == "1" && den == "2") return "½"
        if (num == "1" && den == "4") return "¼"
        if (num == "3" && den == "4") return "¾"
        if (num == "1" && den == "3") return "⅓"
        if (num == "2" && den == "3") return "⅔"
        if (num.length <= 4 && den.length <= 4 && !num.contains(" ") && !den.contains(" ")) {
            return "$num/$den"
        }
        return "($num / $den)"
    }

    /**
     * Formats raw math, LaTeX strings, formulas, and MathML into clean, readable mathematical typography.
     */
    fun formatMath(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        // 1. Convert MathML and HTML entities if present
        var s = convertMathML(raw).trim()

        // 2. Decode any residual HTML entities
        for ((ent, rep) in htmlEntities) {
            s = s.replace(ent, rep)
        }

        // 3. LaTeX environments
        s = s.replace(Regex("""\\begin\{aligned\}|\\end\{aligned\}"""), "")
        s = s.replace(Regex("""\\begin\{vmatrix\}"""), "|\n| ").replace(Regex("""\\end\{vmatrix\}"""), " |\n")
        s = s.replace(Regex("""\\begin\{pmatrix\}"""), "(\n( ").replace(Regex("""\\end\{pmatrix\}"""), " )\n")
        s = s.replace(Regex("""\\begin\{bmatrix\}"""), "[\n[ ").replace(Regex("""\\end\{bmatrix\}"""), " ]\n")
        s = s.replace(Regex("""\\begin\{matrix\}"""), "[\n[ ").replace(Regex("""\\end\{matrix\}"""), " ]\n")
        s = s.replace(Regex("""\\begin\{cases\}|\\end\{cases\}"""), "")

        // Environment line breaks and alignment
        s = s.replace("\\\\", "\n")
        s = s.replace(Regex("""\s*&\s*"""), " ")

        // 4. Remove font & formatting wrappers
        s = s.replace(Regex("""\\(?:mathrm|mathbf|mathit|text|operatorname|mathbb|mathcal)\{([^}]*)\}""")) {
            it.groupValues[1]
        }
        s = s.replace(Regex("""\\operatorname\*?\s*\{?([a-zA-Z]+)\}?""")) { it.groupValues[1] }

        // 5. Greek letters & symbols
        for ((regex, replacement) in greekMap) {
            s = regex.replace(s, replacement)
        }
        for ((regex, replacement) in symbolMap) {
            s = regex.replace(s, replacement)
        }

        // 6. Vector and accent notations
        s = s.replace(Regex("""\\overline\{([^}]*)\}""")) { "${it.groupValues[1]}̄" }
        s = s.replace(Regex("""\\bar\{([^}]*)\}""")) { "${it.groupValues[1]}̄" }
        s = s.replace(Regex("""\\vec\{([^}]*)\}""")) { "${it.groupValues[1]}⃗" }
        s = s.replace(Regex("""\\hat\{i\}|\\hat\s*i\b"""), "î")
        s = s.replace(Regex("""\\hat\{j\}|\\hat\s*j\b"""), "ĵ")
        s = s.replace(Regex("""\\hat\{k\}|\\hat\s*k\b"""), "k̂")
        s = s.replace(Regex("""\\binom\{([^}]+)\}\{([^}]+)\}""")) {
            val n = toSuperscript(it.groupValues[1])
            val r = toSubscript(it.groupValues[2])
            "${n}C${r}"
        }

        // 7. Delimiters cleanup (\left( -> (, \right] -> ], \left. -> "")
        s = s.replace(Regex("""\\left\s*([(\[{|])""")) { it.groupValues[1] }
        s = s.replace(Regex("""\\right\s*([)\]}|])""")) { it.groupValues[1] }
        s = s.replace(Regex("""\\left\.|\right\."""), "")

        // 8. Standard mathematical functions
        for (fn in standardFunctions) {
            s = s.replace("\\$fn", fn)
        }
        s = s.replace(Regex("""\\(?:lim|Lim)\b"""), "lim")

        // 9. Integrals with bounds: \int_0^{10 \pi}, \int_a^b, \int_0^{\sqrt{2}}
        s = s.replace(Regex("""\\int_\{([^}]+)\}\^\{([^}]+)\}""")) {
            "∫" + toSubscript(formatMath(it.groupValues[1])) + toSuperscript(formatMath(it.groupValues[2]))
        }
        s = s.replace(Regex("""\\int_([0-9a-zA-Z\\π]+)\^\{([^}]+)\}""")) {
            "∫" + toSubscript(it.groupValues[1]) + toSuperscript(formatMath(it.groupValues[2]))
        }
        s = s.replace(Regex("""\\int_\{([^}]+)\}\^([0-9a-zA-Z\\π]+)""")) {
            "∫" + toSubscript(formatMath(it.groupValues[1])) + toSuperscript(it.groupValues[2])
        }
        s = s.replace(Regex("""\\int_([0-9a-zA-Z\\π]+)\^([0-9a-zA-Z\\π]+)""")) {
            "∫" + toSubscript(it.groupValues[1]) + toSuperscript(it.groupValues[2])
        }
        s = s.replace(Regex("""\\int\b"""), "∫")

        // 10. Limits: \lim_{x \to 0}, \lim_{n \rightarrow \infty}
        s = s.replace(Regex("""lim_\{([^}]+)\}""")) {
            val inside = formatMath(it.groupValues[1])
            "lim[$inside]"
        }

        // 11. Summations & Products with bounds: \sum_{k=1}^n
        s = s.replace(Regex("""\\sum_\{([^}]+)\}\^\{([^}]+)\}""")) {
            "∑[${formatMath(it.groupValues[1])} to ${formatMath(it.groupValues[2])}]"
        }
        s = s.replace(Regex("""\\sum_\{([^}]+)\}""")) {
            "∑[${formatMath(it.groupValues[1])}]"
        }
        s = s.replace(Regex("""\\sum\b"""), "∑")
        s = s.replace(Regex("""\\prod_\{([^}]+)\}\^\{([^}]+)\}""")) {
            "∏[${formatMath(it.groupValues[1])} to ${formatMath(it.groupValues[2])}]"
        }
        s = s.replace(Regex("""\\prod\b"""), "∏")

        // 12. Fractions: \frac{A}{B}
        var prevFrac = ""
        var fracIters = 0
        while (prevFrac != s && fracIters < 4) {
            prevFrac = s
            s = s.replace(Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")) { match ->
                val num = match.groupValues[1].trim()
                val den = match.groupValues[2].trim()
                formatFraction(num, den)
            }
            fracIters++
        }

        // 13. Square Roots: \sqrt[3]{X} -> ³√(X), \sqrt{X} -> √(X)
        s = s.replace(Regex("""\\sqrt\[([^\]]+)\]\{([^{}]+)\}""")) {
            "(${it.groupValues[1]})√(${it.groupValues[2].trim()})"
        }
        s = s.replace(Regex("""\\sqrt\{([^{}]+)\}""")) {
            "√(${it.groupValues[1].trim()})"
        }
        s = s.replace(Regex("""\\sqrt\s*([0-9a-zA-Zα-ωΑ-Ωπ])""")) {
            "√${it.groupValues[1]}"
        }

        // 14. Trigonometric powers: tan ^n x or tan^n x -> tanⁿ(x)
        s = s.replace(Regex("""\b(sin|cos|tan|cot|sec|csc)\s*\^\{?([0-9a-zA-Z+\-]+)\}?\s*([a-zA-Zθxyt])\b""")) { m ->
            val fn = m.groupValues[1]
            val exp = toSuperscript(m.groupValues[2])
            val arg = m.groupValues[3]
            "$fn$exp($arg)"
        }

        // 15. General superscripts: ^{...} or ^X
        s = s.replace(Regex("""\^\{([^{}]+)\}""")) { match ->
            toSuperscript(match.groupValues[1])
        }
        s = s.replace(Regex("""\^([0-9a-zA-Z+\-])""")) { match ->
            toSuperscript(match.groupValues[1])
        }

        // 16. General subscripts: _{...} or _X
        s = s.replace(Regex("""_\{([^{}]+)\}""")) { match ->
            toSubscript(match.groupValues[1])
        }
        s = s.replace(Regex("""_([0-9a-zA-Z+\-])""")) { match ->
            toSubscript(match.groupValues[1])
        }

        // 17. Differentials in integrals: d x -> dx
        s = s.replace(Regex("""\bd\s+([xyztrstθ])\b""")) { "d${it.groupValues[1]}" }

        // 18. Remove delimiters: $$, $, \(, \), \[, \]
        s = s.replace("$$", "").replace("$", "")
        s = s.replace("\\(", "").replace("\\)", "").replace("\\[", "").replace("\\]", "")

        // 19. Clean remaining unhandled LaTeX backslashes (\foo -> foo)
        s = s.replace(Regex("""\\([a-zA-Z]+)""")) { it.groupValues[1] }
        s = s.replace("\\{", "{").replace("\\}", "}")

        // 20. Clean spacing around mathematical operators
        s = s.replace(Regex("""\s*=\s*"""), " = ")
        s = s.replace(Regex("""\s*\+\s*"""), " + ")
        s = s.replace(Regex("""(?<=[0-9a-zA-Z\)])\s*-\s*(?=[0-9a-zA-Z\(])(?!\d*\^)"""), " − ")
        s = s.replace(Regex("""\s*±\s*"""), " ± ")
        s = s.replace(Regex("""\s*≤\s*"""), " ≤ ")
        s = s.replace(Regex("""\s*≥\s*"""), " ≥ ")
        s = s.replace(Regex("""\s*≠\s*"""), " ≠ ")
        s = s.replace(Regex("""\s*→\s*"""), " → ")
        s = s.replace(Regex("""\s*⇒\s*"""), " ⇒ ")

        // 21. Collapse redundant horizontal spaces while preserving lines
        s = s.lines().joinToString("\n") { line ->
            line.replace(Regex("""[ \t]+"""), " ").trim()
        }.trim()

        return s
    }
}
