package com.joengelke.shoppinglistapp.frontend.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

object FileUtils {

    fun openFile(context: Context, file: File) {
        val fileUri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val mimeType = getMimeType(file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(intent, "Open with")
        try {
            context.startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "*/*"
        }
    }

    fun createRecipePdf(context: Context, recipe: Recipe, download: Boolean): File {
        val pageWidth = 595
        val pageHeight = 842
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // --- Margins ---
        val marginLeft = 30f
        val marginRight = 30f
        val marginTop = 50f
        val marginBottom = 40f

        // --- Paints ---
        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val smallPaint = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }
        val descPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.LEFT
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }
        val footerPaint = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }

        // --- Step 1: Measure instructions ---
        val lineHeight = bodyPaint.textSize + 5f // ~17
        val instructionsLines = mutableListOf<String>()
        recipe.instructions.forEachIndexed { i, step ->
            val stepLines = splitTextIntoLines(
                "${i + 1}. $step",
                bodyPaint,
                pageWidth - marginLeft.toInt() - marginRight.toInt()
            )
            instructionsLines.addAll(stepLines)
            instructionsLines.add("") // gap
        }
        val instructionsHeight = instructionsLines.size * lineHeight + 22f // include header
        val instructionsTop = pageHeight - instructionsHeight - marginBottom

        // --- Step 2: Title ---
        var y = marginTop
        canvas.drawText(recipe.name, (pageWidth / 2).toFloat(), y, titlePaint)
        y += 25f  // a little less space than before

        // --- Step 3: Categories (as subtitle) ---
        if (recipe.categories.isNotEmpty()) {
            val categoriesText = recipe.categories.joinToString(", ")
            canvas.drawText(categoriesText, (pageWidth / 2).toFloat(), y, smallPaint)
            y += 20f // spacing after subtitle
        }

        // --- Step 4: Description ---

        var descLines = splitTextIntoLines(
            recipe.description,
            descPaint,
            pageWidth - marginLeft.toInt() - marginRight.toInt()
        )
        var descHeight = descLines.size * (descPaint.textSize + 4f)

        val availableAboveIngredients = instructionsTop - y - 25f

        // shrink description font size if needed
        while (descHeight > availableAboveIngredients && descPaint.textSize > 8f) {
            descPaint.textSize -= 1f
            descLines = splitTextIntoLines(
                recipe.description,
                descPaint,
                pageWidth - marginLeft.toInt() - marginRight.toInt()
            )
            descHeight = descLines.size * (descPaint.textSize + 4f)
        }

        descLines.forEach {
            canvas.drawText(it, marginLeft, y, descPaint)
            y += descPaint.textSize + 4f
        }
        y += 8f

        // --- Step 5: Ingredients (multi-column) ---
        canvas.drawText(context.getString(R.string.ingredients), marginLeft, y, bodyPaint)
        y += 20f

        val ingredientTexts = recipe.itemSet.itemList.map { item ->
            val amountStr = when {
                item.amount == 0.0 -> ""                  // skip if 0
                item.amount % 1.0 == 0.0 -> item.amount.toInt().toString()
                else -> item.amount.toString()
            }
            val space = if (amountStr.isNotEmpty()) " " else ""
            "• $amountStr$space${item.unit} ${item.name}"
        }

        val availableForIngredients = instructionsTop - y - 15f
        val linesPerColumn = (availableForIngredients / lineHeight).toInt().coerceAtLeast(1)
        val columnCount = if (ingredientTexts.size > linesPerColumn) 2 else 1
        val columnWidth = (pageWidth - marginLeft - marginRight) / columnCount

        // Define columns outside the block for later reference
        val leftColumn: List<String>
        val rightColumn: List<String>

        if (columnCount == 1) {
            leftColumn = ingredientTexts
            rightColumn = emptyList()

            leftColumn.forEachIndexed { index, text ->
                val lineY = y + index * lineHeight
                canvas.drawText(text, marginLeft + 20f, lineY, bodyPaint)
            }
        } else {
            val half = (ingredientTexts.size + 1) / 2
            leftColumn = ingredientTexts.subList(0, half)
            rightColumn = ingredientTexts.subList(half, ingredientTexts.size)

            leftColumn.forEachIndexed { index, text ->
                val lineY = y + index * lineHeight
                canvas.drawText(text, marginLeft + 20f, lineY, bodyPaint)
            }

            rightColumn.forEachIndexed { index, text ->
                val lineY = y + index * lineHeight
                canvas.drawText(text, marginLeft + 20f + columnWidth, lineY, bodyPaint)
            }
        }

        // --- Step 6: Instructions ---
        // Determine bottom of ingredients
        val lastIngredientRow = if (columnCount == 1) {
            leftColumn.size - 1
        } else {
            max(leftColumn.size, rightColumn.size) - 1
        }
        val ingredientsBottom = y + lastIngredientRow * lineHeight

        // Instructions start below ingredients
        var instY = ingredientsBottom + 20f
        canvas.drawText(context.getString(R.string.instructions), marginLeft, instY, bodyPaint)
        instY += 20f

        val numberX = marginLeft
        val textX = numberX + 22f
        val maxWidth = pageWidth - marginLeft.toInt() - marginRight.toInt()

        recipe.instructions.forEachIndexed { i, step ->
            val number = "${i + 1}."
            val stepLines =
                splitTextIntoLines(step, bodyPaint, (maxWidth - (textX - numberX)).toInt())

            // Draw number
            canvas.drawText(number, numberX, instY, bodyPaint)

            // First line next to number
            if (stepLines.isNotEmpty()) {
                canvas.drawText(stepLines[0], textX, instY, bodyPaint)
            }
            instY += lineHeight

            // Remaining lines indented
            for (j in 1 until stepLines.size) {
                canvas.drawText(stepLines[j], textX, instY, bodyPaint)
                instY += lineHeight
            }

            instY += 6f // small gap between steps
        }

        // --- Footer: Created with ShopIt ---
        val footerText = "Created with ShopIt"

        // Position footer above bottom margin
        val footerY = pageHeight - 20f

        // Draw centered text
        canvas.drawText(footerText, (pageWidth  / 2).toFloat(), footerY, footerPaint)


        pdfDocument.finishPage(page)

        // --- Decide file location based on `download` ---
        val file: File = if (download) {
            // Use MediaStore for public Downloads folder (modern Android)
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "${recipe.name.replace(" ", "_")}.pdf")
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create file in Downloads")

            resolver.openOutputStream(uri)?.use { out ->
                pdfDocument.writeTo(out)
            }

            // Return a temporary File object pointing to cache for sharing purposes
            File(context.cacheDir, "${recipe.name.replace(" ", "_")}.pdf").apply {
                pdfDocument.writeTo(FileOutputStream(this))
            }
        } else {
            // Save to cache folder
            File(context.cacheDir, "${recipe.name.replace(" ", "_")}.pdf").apply {
                FileOutputStream(this).use { out ->
                    pdfDocument.writeTo(out)
                }
            }
        }

        pdfDocument.close()
        return file
    }


    /**
     * Helper: split long text into multiple lines that fit the page width
     */
    fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    fun sharePdf(context: Context, file: File) {
        // 1. Get a content Uri for the file using FileProvider
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Make sure your provider is declared in AndroidManifest
            file
        )

        // 2. Predefined text
        val message =
            "Hey! Ich möchte ein Rezept mit dir teilen. Schau es dir doch gleich mal an: ${file.name}"

        // 3. Create the share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Allow the receiving app to read the file
            //`package` = "com.whatsapp"
        }

        // 4. Launch the chooser
        try {
            context.startActivity(shareIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, e.printStackTrace().toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareRecipeAsText(context: Context, recipe: Recipe) {
        val builder = StringBuilder()

        // Title
        builder.append("🍴 ${recipe.name}\n\n")

        // Ingredients
        builder.append("📝 ${context.getString(R.string.ingredients)}:\n")
        recipe.itemSet.itemList.forEach { item ->
            val amountStr = when {
                item.amount == 0.0 -> "" // skip if 0
                item.amount % 1.0 == 0.0 -> item.amount.toInt().toString()
                else -> item.amount.toString()
            }
            val space = if (amountStr.isNotEmpty()) " " else ""
            builder.append("• $amountStr$space${item.unit} ${item.name}\n")
        }
        builder.append("\n")

        // Instructions
        builder.append("📖 ${context.getString(R.string.instructions)}:\n")
        recipe.instructions.forEachIndexed { i, step ->
            builder.append("${i + 1}. $step\n")
        }

        val shareText = builder.toString()

        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            // If you want to force WhatsApp only:
            // `setPackage("com.whatsapp")`
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share recipe via")
        )
    }
}