package com.boostedpenguin.clipboardmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import com.boostedpenguin.clipboardmanager.databinding.ActivityCreateNoteBinding
import com.boostedpenguin.clipboardmanager.room.Note

class CreateNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNoteBinding
    private val model: CreateNoteActivityViewModel by viewModels { CreateNoteActivityViewModelFactory((applicationContext as NoteApplication).repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_create_note)

        setSupportActionBar(findViewById(R.id.createToolbar))
        supportActionBar?.title = "Add clipboard"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_menu, menu)
        val item = menu.findItem(R.id.action_save)
        item.actionView.setOnClickListener {
            val content = findViewById<EditText>(R.id.note_content).text.toString()
            model.insert(Note(content))
        }

        return true
    }
}