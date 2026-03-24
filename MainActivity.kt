package com.example.gradecalculator

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GradeCalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun GradeCalculatorScreen() {
    val notes = remember { mutableStateListOf("", "", "", "", "", "") }
    var resultText by remember { mutableStateOf("") }
    
    // States for export
    var currentAverage by remember { mutableDoubleStateOf(0.0) }
    var currentGrade by remember { mutableStateOf("") }
    var currentNotesList by remember { mutableStateOf(listOf<Double>()) }
    
    val context = LocalContext.current
    
    // Excel Export Launcher
    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("Bulletin de Notes")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Description")
                    headerRow.createCell(1).setCellValue("Valeur")
                    var rowIdx = 1
                    currentNotesList.forEachIndexed { index, note ->
                        val row = sheet.createRow(rowIdx++)
                        row.createCell(0).setCellValue("Cours ${index + 1}")
                        row.createCell(1).setCellValue(note)
                    }
                    sheet.createRow(rowIdx++)
                    val avgRow = sheet.createRow(rowIdx++)
                    avgRow.createCell(0).setCellValue("Moyenne Générale")
                    avgRow.createCell(1).setCellValue(currentAverage)
                    val gradeRow = sheet.createRow(rowIdx)
                    gradeRow.createCell(0).setCellValue("Grade Final")
                    gradeRow.createCell(1).setCellValue(currentGrade)
                    workbook.write(outputStream)
                    workbook.close()
                }
                resultText = "Fichier Excel exporté !"
            } catch (e: Exception) {
                resultText = "Erreur Excel : ${e.message}"
            }
        }
    }

    // Word Export Launcher
    val wordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val doc = XWPFDocument()
                    val title = doc.createParagraph().createRun()
                    title.isBold = true
                    title.fontSize = 20
                    title.setText("BULLETIN DE NOTES")
                    currentNotesList.forEachIndexed { index, note ->
                        val p = doc.createParagraph().createRun()
                        p.setText("Cours ${index + 1} : $note / 20")
                    }
                    val summary = doc.createParagraph().createRun()
                    summary.addBreak()
                    summary.isBold = true
                    summary.setText("Moyenne Générale : ${String.format("%.2f", currentAverage)} / 20")
                    summary.addBreak()
                    summary.setText("Grade : $currentGrade")
                    doc.write(outputStream)
                    doc.close()
                }
                resultText = "Fichier Word exporté !"
            } catch (e: Exception) {
                resultText = "Erreur Word : ${e.message}"
            }
        }
    }

    // PDF Export Launcher
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val pdfDoc = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 24f
                canvas.drawText("BULLETIN DE NOTES", 50f, 50f, paint)
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 16f
                var y = 100f
                currentNotesList.forEachIndexed { index, note ->
                    canvas.drawText("Cours ${index + 1} : $note / 20", 50f, y, paint)
                    y += 30f
                }
                y += 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Moyenne : ${String.format("%.2f", currentAverage)} / 20", 50f, y, paint)
                canvas.drawText("Grade : $currentGrade", 50f, y + 30f, paint)
                pdfDoc.finishPage(page)
                context.contentResolver.openOutputStream(it)?.use { os -> pdfDoc.writeTo(os) }
                pdfDoc.close()
                resultText = "Fichier PDF exporté !"
            } catch (e: Exception) {
                resultText = "Erreur PDF : ${e.message}"
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                val allNotes = mutableListOf<Double>()
                lines.forEach { line ->
                    line.split(",", ";").forEach { part ->
                        part.trim().toDoubleOrNull()?.let { note ->
                            if (note in 0.0..20.0) allNotes.add(note)
                        }
                    }
                }
                if (allNotes.isNotEmpty()) {
                    currentNotesList = allNotes
                    currentAverage = allNotes.average()
                    currentGrade = getGrade(currentAverage)
                    resultText = "Importé (${allNotes.size} notes)"
                    for (i in notes.indices) {
                        notes[i] = if (i < allNotes.size) allNotes[i].toString() else ""
                    }
                }
            } catch (e: Exception) {
                resultText = "Erreur lecture : ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Grade Calculator", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        notes.forEachIndexed { index, value ->
            NoteInputField(label = "Cours ${index + 1}", value = value, onValueChange = { notes[index] = it })
        }

        Button(
            onClick = {
                val parsed = notes.map { it.replace(',', '.').toDoubleOrNull() }.filterNotNull().filter { it in 0.0..20.0 }
                if (parsed.isNotEmpty()) {
                    currentNotesList = parsed
                    currentAverage = parsed.average()
                    currentGrade = getGrade(currentAverage)
                    resultText = "Calculé : ${String.format(Locale.US, "%.2f", currentAverage)} / 20 ($currentGrade)"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculer") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        
        OutlinedButton(onClick = { filePickerLauncher.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Importer CSV/Texte")
        }

        if (currentNotesList.isNotEmpty()) {
            Text("Exporter en :", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { excelLauncher.launch("Rapport.xlsx") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("Excel") }
                Button(onClick = { wordLauncher.launch("Rapport.docx") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Word") }
                Button(onClick = { pdfLauncher.launch("Rapport.pdf") }, modifier = Modifier.weight(1f)) { Text("PDF") }
            }
        }

        if (resultText.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(resultText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun NoteInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
}

fun getGrade(average: Double): String = when {
    average >= 16 -> "Excellent (A)"
    average >= 14 -> "Très Bien (B+)"
    average >= 12 -> "Bien (B)"
    average >= 10 -> "Passable (C)"
    else -> "Insuffisant (F)"
}

@Preview(showBackground = true)
@Composable
fun GradeCalculatorScreenPreview() {
    MaterialTheme { GradeCalculatorScreen() }
}
