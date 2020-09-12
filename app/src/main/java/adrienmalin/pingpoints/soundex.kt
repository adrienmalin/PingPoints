package adrienmalin.pingpoints

import java.util.*
import java.util.regex.Pattern

val yNotPrecededByA:Pattern = Pattern.compile("(?<!A)Y")
val hNotPrecededByCS: Pattern = Pattern.compile("(?<![CS])H")
val aInWord: Pattern = Pattern.compile("(?<!\\b)A")
val silentEndLetter = Pattern.compile("[ADST](?=\\b)")
val notALetter = Pattern.compile("\\W")
val doubleLetter = Pattern.compile("(\\w)\\1+(?!\\1)")

fun soundex(string: String): String {
    var s = string
        .trim()
        .toUpperCase(Locale.ROOT)
        .replace('Â', 'A')
        .replace('Ä', 'A')
        .replace('À', 'A')
        .replace('E', 'A')
        .replace('È', 'A')
        .replace('É', 'A')
        .replace('Ê', 'A')
        .replace('Ë', 'A')
        .replace('Œ', 'A')
        .replace('I', 'A')
        .replace('Î', 'A')
        .replace('Ï', 'A')
        .replace('O', 'A')
        .replace('Ô', 'A')
        .replace('Ö', 'A')
        .replace('U', 'A')
        .replace('Ù', 'A')
        .replace('Û', 'A')
        .replace('Ü', 'A')
        .replace('Ç', 'S')
        .replace("GUI", "KI")
        .replace("GUE", "KE")
        .replace("GA", "KA")
        .replace("GO", "KO")
        .replace("GU", "K")
        .replace("CA", "KA")
        .replace("CO", "KO")
        .replace("CU", "KU")
        .replace("Q", "K")
        .replace("CC", "K")
        .replace("CK", "K")
        .replace("MAC", "MCC")
        .replace("ASA", "AZA")
        .replace("KN", "NN")
        .replace("PF", "FF")
        .replace("SCH", "SSS")
        .replace("PH", "FF")
        .replace("H", "")

    s = yNotPrecededByA.matcher(s).replaceAll("")
    s = hNotPrecededByCS.matcher(s).replaceAll("")
    s = silentEndLetter.matcher(s).replaceAll("")
    s = silentEndLetter.matcher(s).replaceAll("")
    s = notALetter.matcher(s).replaceAll("")
    s = aInWord.matcher(s).replaceAll("")
    s = doubleLetter.matcher(s).replaceAll("$1")

    return s
}

