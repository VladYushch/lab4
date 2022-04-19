@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.*

//import android.app.Fragment
var questionlist: ArrayList<String> = ArrayList()

class MainActivity : AppCompatActivity() {

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        val contentResolver = applicationContext.contentResolver
        if (uri != null) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        questionlist += line
                        line = reader.readLine()
                    }
                }
            }
        }
        val SecondActivity = Intent(this, Pooling::class.java)
        startActivity(SecondActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            getContent.launch("text/plain")
        }


    }
}

class Pooling : AppCompatActivity() {
    var q: Int = 0

    var answerarr: ArrayList<String> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pooling)
        val nextbtn: Button = findViewById(R.id.button2)
        val previous: Button = findViewById(R.id.button3)

        nextbtn.setOnClickListener {
            btns(1)

        }
        previous.setOnClickListener {
            btns(0)
        }
        update()

    }


    private fun update() {
        var quest: TextView = findViewById(R.id.questionView)
        var steps: ProgressBar = findViewById(R.id.progressBar)
        steps.max = questionlist.size
        steps.progress = q
        val previous: Button = findViewById(R.id.button3)
        if (q == 0) {
            previous.isEnabled = false
            quest.text = questionlist[q]
            return
        } else if (q != 0 && q < questionlist.size) {
            quest.text = questionlist[q]
            previous.isEnabled = true
            return
        } else {
            createFile()
        }
    }

    private fun btns(code: Int) {
        //

        var answer: TextInputEditText = findViewById(R.id.answerfield_text)
        if (code == 1) {
            if (!answer.text.toString().trim().isEmpty()) {
                q++
                answerarr.add(answer.text.toString())
                answer.text = null

                update()
            } else {
                answer.error = "Is required"
                answer.requestFocus()
                update()
            }

        } else {
            q--
            val editans: CharArray = answerarr[q].toCharArray()
            answer.setText(editans, 0, editans.size)
            update()

        }
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "invoice.txt")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, "/Download")
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            val docuri: Uri? = data?.data
            var ii: Int = 1
            if (docuri != null) {
                try {
                    contentResolver.openFileDescriptor(docuri, "w")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            for (ans in answerarr) {
                                var tans: String = "${ii} ${ans} \n"
                                it.write(tans.toByteArray())
                                ii++
                            }
                        }
                    }
                    questionlist.removeAll(questionlist)
                    startActivity(Intent(this, MainActivity::class.java))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    companion object {
        // Request code for creating a PDF document.
        const val CREATE_FILE = 1
    }
}
