package com.boostedpenguin.clipboardmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boostedpenguin.clipboardmanager.databinding.ActivityMainBinding
import com.boostedpenguin.clipboardmanager.room.Note

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val model: MainActivityViewModel by viewModels { MainActivityViewModelFactory((applicationContext as NoteApplication).repository) }
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))


        val recyclerView = binding.recyclerView
        adapter = NoteAdapter()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)


        setListeners()
    }


    private fun setCurrentTheme() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPref?.let { pref ->
            val darkModeString = getString(R.string.dark_mode)
            val darkModeValues = resources.getStringArray(R.array.dark_mode_values)
            when (pref.getString(darkModeString, darkModeValues[0])) {
                darkModeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                darkModeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                darkModeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setListeners() {
        //TODO Configure listeners
        model.allNotes.observe(this, Observer { it ->
            it.let {adapter.setNotes(it)}
        })

        // Toggle edit mode and etc.
        adapter.setOnLongItemClickListener(object : NoteAdapter.OnLongItemClickListener {
            override fun onLongClick(position: Int, note: Note) {
                model.isItemSelected.value = model.isItemSelected.value?.not()

                if(model.isItemSelected.value == true) {
                    model.addPosition(position)
                    model.selectedNote.add(note)

                    adapter.notifyDataSetChanged()
                }
            }
        })

        adapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onClick(position: Int, note: Note) {
                handleContent(position, note)
                adapter.notifyDataSetChanged()
            }
        })


        adapter.setOnCheckboxClickListener(object : NoteAdapter.OnCheckboxClickListener {
            override fun onItemClick(position: Int, note: Note) {
                handleContent(position, note)
                adapter.notifyDataSetChanged()
            }
        })

        model.isItemSelected.observe(this, Observer { it ->
            adapter.updateVisibility(it)
            if(!it) {
                model.selectedNote.clear()
                model.clearPositions()
            }
            adapter.notifyDataSetChanged();

            // Invalidate tells it to redraw itself -> toggles onprepare
            this.invalidateOptionsMenu()
        })

        model.selectedCheckboxes.observe(this, Observer { it ->
            adapter.selectedPositions = it.toMutableList();
        })
    }

    fun handleContent(position: Int, note: Note) {
        if(model.isItemSelected.value == true) {
            if(model.selectedCheckboxes.value?.contains(position)!!) {
                model.removePosition(position)
                model.selectedNote.removeAll {
                    it.id == note.id
                }
            }
            else {
                model.addPosition(position)
                model.selectedNote.add(note)
            }
        }
        else {
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onBackPressed() {
        if(model.isItemSelected.value == true) {
            exitSelectMode()
            return
        }
        super.onBackPressed()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        if(model.isItemSelected.value == true) {
            menu.clear()
            menuInflater.inflate(R.menu.item_selected_menu , menu)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        else {
            menu.clear()
            menuInflater.inflate(R.menu.main_menu, menu)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }


        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {
            R.id.main_menu_1 -> {
                Toast.makeText(applicationContext, "Toasty1", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.main_menu_2 -> {
                Toast.makeText(applicationContext, "Toasty2", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.main_menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.main_menu_add -> {
                // Fixme Fill with actual content

                model.insert(Note("New title", "Desc cool description RNG:"))
                model.selectedNote.clear()
                model.clearPositions()
                true
            }
            R.id.action_delete -> {
                model.deleteNotes()
                model.clearPositions()
                model.isItemSelected.value = false;
                true
            }
            android.R.id.home -> {
                exitSelectMode()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun exitSelectMode() {
        model.isItemSelected.value = false
        model.selectedNote.clear()
        model.clearPositions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}