//package com.hackspectra.jobit
//
//import android.os.Bundle
//import android.print.PrintAttributes
//import android.print.PrintManager
//import android.webkit.WebView
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import com.hackspectra.jobit.viewModel.ResumeViewModel
//
//class TailoredResumeActivity : AppCompatActivity() {
//
//    private val viewModel: ResumeViewModel by viewModels()
//
//    private lateinit var atsScoreTextView: TextView
//    private lateinit var resumeWebView: WebView
//    private lateinit var downloadButton: Button
//
//    private var tailoredResumeContent: String? = null
//    private var atsScore: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_tailored_resume)
//
//        atsScoreTextView = findViewById(R.id.ats_score_text)
//        resumeWebView = findViewById(R.id.resume_web_view)
//        downloadButton = findViewById(R.id.download_button)
//
//        tailoredResumeContent = intent.getStringExtra("resume_content")
//        atsScore = intent.getIntExtra("ats_score", 0)
//
//        if (tailoredResumeContent != null) {
//            atsScoreTextView.text = "ATS Score: $atsScore/100"
//            displayResumeInWebView(tailoredResumeContent!!)
//        }
//
//        downloadButton.setOnClickListener {
//            downloadResumeAsPDF()
//        }
//    }
//
//    private fun displayResumeInWebView(resumeContent: String) {
//        val htmlContent = formatToCompactHtml(resumeContent)
//        resumeWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
//    }
//
//    private fun downloadResumeAsPDF() {
//        val fileName = "Tailored_Resume_${System.currentTimeMillis()}"
//
//        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
//        val printAdapter = resumeWebView.createPrintDocumentAdapter(fileName)
//
//        val printAttributes = PrintAttributes.Builder()
//            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
//            .setResolution(PrintAttributes.Resolution("res1", PRINT_SERVICE, 300, 300))
//            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
//            .build()
//
//        printManager.print(fileName, printAdapter, printAttributes)
//        Toast.makeText(this, "Preparing resume PDF...", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun formatToCompactHtml(content: String): String {
//        return """
//            <html>
//            <head>
//                <style>
//                    body {
//                        font-family: Arial, sans-serif;
//                        font-size: 10px;
//                        line-height: 1.4;
//                        margin: 20px;
//                    }
//                    h1, h2 {
//                        font-size: 12px;
//                        margin-bottom: 4px;
//                    }
//                    .section {
//                        margin-bottom: 10px;
//                    }
//                    ul {
//                        padding-left: 16px;
//                        list-style-type: disc;
//                        margin: 4px 0;
//                    }
//                    ul ul {
//                        padding-left: 16px;
//                        list-style-type: circle;
//                    }
//                    li {
//                        margin-bottom: 4px;
//                    }
//                </style>
//            </head>
//            <body>
//                $content
//            </body>
//            </html>
//        """.trimIndent()
//    }
//}


package com.hackspectra.jobit

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TailoredResumeActivity : AppCompatActivity() {

    private lateinit var atsScoreTextView: TextView
    private lateinit var resumeWebView: WebView
    private lateinit var downloadButton: Button

    private var tailoredResumeContent: String? = null
    private var atsScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tailored_resume)

        // Initialize views
        atsScoreTextView = findViewById(R.id.ats_score_text)
        resumeWebView = findViewById(R.id.resume_web_view)
        downloadButton = findViewById(R.id.download_button)

        // Get data from intent
        tailoredResumeContent = intent.getStringExtra("resume_content")
        atsScore = intent.getIntExtra("ats_score", 0)

        // Update UI with data
        if (tailoredResumeContent != null) {
            // Format ATS score with color based on score value
            val scoreColor = when {
                atsScore >= 90 -> "#2E7D32" // Dark green
                atsScore >= 75 -> "#388E3C" // Green
                atsScore >= 60 -> "#FFA000" // Orange
                else -> "#D32F2F" // Red
            }

            atsScoreTextView.text = "ATS Score: $atsScore/100"
            atsScoreTextView.setTextColor(android.graphics.Color.parseColor(scoreColor))

            // Display resume in WebView
            displayResumeInWebView(tailoredResumeContent!!)
        } else {
            Toast.makeText(this, "Error: Resume content not available", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up button click listeners
        downloadButton.setOnClickListener {
            downloadResumeAsPDF()
        }
    }

    private fun displayResumeInWebView(resumeContent: String) {
        // Enable JavaScript (if needed for any interactive elements)
        resumeWebView.settings.javaScriptEnabled = true

        // If the content already contains <html> tags, use it directly, otherwise apply formatting
        val htmlContent = if (resumeContent.trim().startsWith("<html", ignoreCase = true)) {
            enhanceExistingHtml(resumeContent)
        } else {
            formatToStandardResume(resumeContent)
        }

        resumeWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun enhanceExistingHtml(htmlContent: String): String {
        // Add print-friendly styles while preserving original content
        return htmlContent.replace("</head>", """
            <style>
                @media print {
                    body {
                        font-size: 11pt;
                        line-height: 1.3;
                        margin: 0.5in;
                    }
                    .page-break {
                        page-break-before: always;
                    }
                }
                
                /* Enhance existing styling for better readability */
                body {
                    font-family: 'Arial', 'Helvetica', sans-serif;
                    line-height: 1.5;
                    margin: 20px;
                    color: #333;
                }
                h1 {
                    font-size: 24px;
                    color: #000;
                    margin-bottom: 5px;
                    border-bottom: 1px solid #ddd;
                    padding-bottom: 5px;
                }
                h2 {
                    font-size: 18px;
                    color: #1a73e8;
                    margin-top: 15px;
                    margin-bottom: 10px;
                    border-bottom: 1px solid #eee;
                    padding-bottom: 3px;
                }
                h3 {
                    font-size: 16px;
                    margin-bottom: 5px;
                    color: #333;
                }
                .skill-tag {
                    background-color: #e8f0fe;
                    padding: 2px 8px;
                    border-radius: 3px;
                    margin-right: 5px;
                    margin-bottom: 5px;
                    display: inline-block;
                    font-size: 13px;
                }
                ul {
                    padding-left: 20px;
                }
                li {
                    margin-bottom: 5px;
                }
                .company, .dates {
                    color: #555;
                }
            </style>
        </head>""")
    }

    private fun formatToStandardResume(content: String): String {
        // Transform plain content into properly formatted resume HTML
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Tailored Resume</title>
            <style>
                body {
                    font-family: 'Arial', 'Helvetica', sans-serif;
                    line-height: 1.5;
                    margin: 20px;
                    color: #333;
                }
                .header {
                    text-align: center;
                    margin-bottom: 20px;
                }
                h1 {
                    font-size: 24px;
                    color: #000;
                    margin-bottom: 5px;
                }
                .contact-info {
                    font-size: 14px;
                    margin-bottom: 5px;
                }
                h2 {
                    font-size: 18px;
                    color: #1a73e8;
                    margin-top: 15px;
                    margin-bottom: 10px;
                    border-bottom: 1px solid #eee;
                    padding-bottom: 3px;
                }
                .section {
                    margin-bottom: 15px;
                }
                .experience-item, .project-item, .education-item {
                    margin-bottom: 12px;
                }
                .job-title, .project-name, .degree {
                    font-weight: bold;
                    font-size: 16px;
                    margin-bottom: 3px;
                }
                .company, .institution {
                    font-weight: bold;
                    color: #555;
                }
                .dates {
                    font-style: italic;
                    color: #666;
                }
                ul {
                    margin-top: 5px;
                    padding-left: 25px;
                    list-style-type: disc;
                }
                li {
                    margin-bottom: 5px;
                }
                .skills-container {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 5px;
                }
                .skill-tag {
                    background-color: #e8f0fe;
                    padding: 2px 8px;
                    border-radius: 3px;
                    display: inline-block;
                    font-size: 13px;
                }
                
                /* Print-specific styles */
                @media print {
                    body {
                        font-size: 11pt;
                        line-height: 1.3;
                        margin: 0.5in;
                    }
                    .page-break {
                        page-break-before: always;
                    }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>SHUBHAM RAJURPALLE</h1>
                <div class="contact-info">
                    <a href="https://linkedin.com/in/shubham-rajurpalle">linkedin/shubham-rajurpalle</a> | 
                    rajurpalleshubham1802@gmail.com | +91-9834583910 | 
                    <a href="https://github.com/Shubham-Rajurpalle">github/Shubham-Rajurpalle</a>
                </div>
            </div>
            
            <div class="section">
                <h2>Skills</h2>
                <p><strong>Languages & Frameworks:</strong> Kotlin, Java, JavaScript, Python, C++, React Native, React, HTML, CSS</p>
                <p><strong>Android App Development:</strong> Android SDK, Jetpack Compose, Material Design, MVVM, Coroutines, LiveData, ViewModel</p>
                <p><strong>Cross-platform Tools:</strong> React Native, Redux (Basics), Figma (UI Prototyping), Expo</p>
                <p><strong>Web Development:</strong> React, JavaScript, HTML, CSS</p>
                <p><strong>Backend Development & Databases:</strong> Firebase, Room Database, SQLite, RESTful APIs, Retrofit</p>
                <p><strong>Development Tools:</strong> Android Studio, Git, GitHub, Postman, VS Code</p>
                <p><strong>Engineering Practices:</strong> Data Structures & Algorithms, Operating Systems, Database Management Systems, Software Engineering, Android Development, Web Development (Frontend)</p>
                <p><strong>Software Engineering Practices:</strong> Test-Driven Development (TDD), CI/CD, Performance Optimization, Agile & Scrum</p>
            </div>
            
            <div class="section">
                <h2>Experience</h2>
                <div class="experience-item">
                    <div class="job-title">React Native Developer</div>
                    <div><span class="company">OrderHub | Drive</span> <span class="dates">Dec'24 - Feb'25</span></div>
                    <ul>
                        <li>Collaborated with stakeholders to define app flows and create user journeys through detailed Figma wireframes</li>
                        <li>Designed intuitive UI screens following Google's Material Design system for cross-platform consistency</li>
                        <li>Outlined Redux-based state management structure and Firebase integration plan for future implementation</li>
                        <li>Development currently on hold; contributed fully to planning, UI design, and architecture decisions</li>
                    </ul>
                </div>
            </div>
            
            <div class="section">
                <h2>Projects</h2>
                <div class="project-item">
                    <div class="job-title">Android Developer</div>
                    <div><span class="company">CricXone | GitHub</span> <span class="dates">May'24 - Sep'24</span></div>
                    <ul>
                        <li>Built high-performance Android application handling real-time data synchronization for thousands of concurrent users</li>
                        <li>Engineered caching mechanisms reducing API calls by 60% and improving application response time by 45%</li>
                        <li>Implemented advanced RecyclerView patterns with DiffUtil for smooth user experience and memory optimization</li>
                        <li>Utilized Kotlin Coroutines for asynchronous operations, ensuring responsive UI during network operations</li>
                    </ul>
                </div>
                
                <div class="project-item">
                    <div class="job-title">Android Developer</div>
                    <div><span class="company">CampusCore | GitHub</span> <span class="dates">Feb'25 - Mar'25</span></div>
                    <ul>
                        <li>Developed a role-based college management app with QR/email login and secure identity verification</li>
                        <li>Built modules for live elections, complaint filing, resource booking, and application requests</li>
                        <li>Implemented user roles including student, faculty, dean, doctor, and security using MVVM and Firebase</li>
                        <li>Utilized Kotlin Coroutines and Firestore listeners for real-time updates and smooth user experience</li>
                    </ul>
                </div>
                
                <div class="project-item">
                    <div class="job-title">Web Developer</div>
                    <div><span class="company">Devfolio | GitHub</span> <span class="dates">Apr'25 - Apr'24</span></div>
                    <ul>
                        <li>Designed and developed a personal portfolio website showcasing projects, academics, and professional background</li>
                        <li>Built responsive UI using React, JavaScript, HTML, and CSS ensuring optimal viewing across all device sizes</li>
                        <li>Implemented smooth animations and transitions for enhanced user experience and engagement</li>
                        <li>Created an intuitive project showcase section and detailed project descriptions</li>
                    </ul>
                </div>
            </div>
            
            <div class="section">
                <h2>Education</h2>
                <div class="education-item">
                    <div class="job-title">Shri Guru Gobind Singh Institute of Engineering and Technology, Nanded</div>
                    <div><span class="company">CSE</span> <span class="dates">2022 - 2026</span></div>
                    <p>CGPA: 8.7 (expected)</p>
                </div>
            </div>
            
            <div class="section">
                <h2>Technical Problem Solving</h2>
                <ul>
                    <li>Solved 350+ algorithmic challenges on GeeksforGeeks and 50+ on LeetCode, focusing on optimization and efficiency</li>
                    <li>Implemented complex data structures and algorithms in Java and Kotlin for competitive programming competitions</li>
                    <li>1st Runner Up at National Level "Hack Fusion" Hackathon - Engineered Automated College Management System</li>
                </ul>
            </div>
            
            <div class="section">
                <h2>Leadership & Entrepreneurship</h2>
                <ul>
                    <li>Built and scaled YouTube channel "Story Network" to 250,000+ subscribers through content optimization</li>
                    <li>Managed cross-functional team of content creators, ensuring consistent delivery meeting platform requirements</li>
                    <li>Analyzed user engagement metrics to optimize content strategy, resulting in 40% growth in viewership</li>
                </ul>
            </div>
        </body>
        </html>
        """
    }

    private fun downloadResumeAsPDF() {
        try {
            val fileName = "Tailored_Resume_${System.currentTimeMillis()}"

            val printManager = getSystemService(PRINT_SERVICE) as PrintManager
            val printAdapter = resumeWebView.createPrintDocumentAdapter(fileName)

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "PDF", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            printManager.print(fileName, printAdapter, printAttributes)
            Toast.makeText(this, "Preparing your tailored resume PDF...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}