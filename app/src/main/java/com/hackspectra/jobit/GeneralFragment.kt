package com.hackspectra.jobit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.*

class GeneralFragment : Fragment() {

    companion object {
        private const val RESUME_PICK_CODE = 1001
    }

    private lateinit var scoreTextView: TextView
    private lateinit var missingKeywordsTextView: TextView
    private lateinit var jdEditText: EditText
    private var resumeText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_general, container, false)

        // Initialize UI components
        scoreTextView = rootView.findViewById(R.id.scoreTextView)
        missingKeywordsTextView = rootView.findViewById(R.id.missingKeywordsTextView)
        jdEditText = rootView.findViewById(R.id.jdEditText)

        val uploadResumeBtn = rootView.findViewById<Button>(R.id.uploadResumeBtn)
        val calculateBtn = rootView.findViewById<Button>(R.id.calculateBtn)

        // Set click listeners
        uploadResumeBtn.setOnClickListener {
            pickResumeFile()
        }

        calculateBtn.setOnClickListener {
            val jobDescription = jdEditText.text.toString().trim()
            if (resumeText.isEmpty()) {
                Toast.makeText(requireContext(), "Please upload your resume first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jobDescription.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter job description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            calculateAtsScore(resumeText, jobDescription)
        }

        return rootView
    }

    private fun pickResumeFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), RESUME_PICK_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESUME_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val resumeUri = data?.data
            if (resumeUri != null) {
                extractResumeText(resumeUri)
            } else {
                Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractResumeText(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    resumeText = extractTextFromPdf(inputStream)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Resume uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Unable to read PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to extract text: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun extractTextFromPdf(inputStream: InputStream): String {
        return try {
            val reader = PdfReader(inputStream)
            val pdfDoc = PdfDocument(reader)
            val pageCount = pdfDoc.numberOfPages
            val extractedText = StringBuilder()

            // Extracting text from each page
            for (i in 1..pageCount) {
                val page = pdfDoc.getPage(i)
                val text = PdfTextExtractor.getTextFromPage(page)
                extractedText.append(text)
                extractedText.append("\n")
            }

            pdfDoc.close()
            reader.close()
            inputStream.close()
            extractedText.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Error extracting text: ${e.message}", e)
        }
    }

    private fun calculateAtsScore(resumeText: String, jobDescription: String) {
        // Extract keywords from job description
        val jobKeywords = extractKeywords(jobDescription)

        // Add industry-specific keywords
        val industryKeywords = listOf(
            "Android", "Kotlin", "Java", "MVVM", "Architecture", "UI/UX", "Material Design",
            "Jetpack", "Compose", "Firebase", "API", "Mobile", "App", "Testing", "Git",
            "Agile", "CI/CD", "Performance", "Optimization", "React Native", "JavaScript",
            "Python", "C++", "HTML", "CSS", "Web Development", "Frontend", "Backend",
            "SQLite", "Room", "Database", "RESTful", "Retrofit", "Coroutines", "LiveData",
            "ViewModel", "TDD", "Redux", "React", "Figma", "Expo"
            )
            // Create a combined set of all relevant keywords
        val allKeywords = (jobKeywords + industryKeywords).distinct()

        // Convert resume text to lowercase for case-insensitive matching
        val resumeTextLower = resumeText.lowercase(Locale.ROOT)

        // Find matching and missing keywords
        val matchedKeywords = mutableListOf<String>()
        val missingKeywords = mutableListOf<String>()

        for (keyword in allKeywords) {
            if (resumeTextLower.contains(keyword.lowercase(Locale.ROOT))) {
                matchedKeywords.add(keyword)
            } else {
                missingKeywords.add(keyword)
            }
        }

        // Calculate score
        val score = if (allKeywords.isEmpty()) 0
        else (matchedKeywords.size.toFloat() / allKeywords.size * 100).toInt()

        // Update UI
        scoreTextView.text = "ATS Score: $score%"

        if (missingKeywords.isNotEmpty()) {
            missingKeywordsTextView.visibility = View.VISIBLE
        } else {
            missingKeywordsTextView.visibility = View.VISIBLE
            missingKeywordsTextView.text = "Great job! Your resume contains all the important keywords."
        }
    }

    private fun extractKeywords(text: String): List<String> {
        // Simple keyword extraction from job description
        // This is a basic implementation - could be improved with NLP techniques
        val stopWords = setOf("a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "with", "by", "of")

        // Split text into words, remove punctuation, and filter out stop words and short words
        return text.split("\\s+".toRegex())
            .map { it.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "") }
            .filter { it.length > 3 && it !in stopWords }
            .distinct()
            .filter { it.isNotEmpty() }
    }
}