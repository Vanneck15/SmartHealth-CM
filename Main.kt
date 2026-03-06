fun main() {
    val grades = mutableListOf<Double>()
    val totalNotes = 3

    while (grades.size < totalNotes) {
        print("Entrez la note n°${grades.size + 1} : ")
        val input = readLine() ?: ""

        val grade = input.toDoubleOrNull()
        if (grade == null || grade < 0 || grade > 20) {
            println("Veuillez entrer un nombre valide entre 0 et 20.")
            continue
        }
        grades.add(grade)
    }

    val average = grades.average()
    println("\nRésultats :")
    println("Notes : $grades")
    println("Moyenne : %.2f".format(average))
    println("Mention : ${getMention(average)}")
}

fun getMention(average: Double): String = when {
    average >= 19 -> "Excellent"
    average >= 17 -> "Très bien"
    average >= 14 -> "Bien"
    average >= 10 -> "Passable"
    else -> "Insuffisant"
}
