package com.kyhsgeekcode.fixzip

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, IConsole {
    override fun print(s: String?) {
        runOnUiThread {
            //avoid OutOfMemoryError
            if (adapter!!.count > 10000) adapter!!.remove(adapter!!.getItem(0))
            adapter!!.add(s)
            adapter!!.notifyDataSetChanged()
        }
    }

    override suspend fun readLine(): String {
        isInputMode = true
        lock.await()
        lock = CompletableDeferred()
        isInputMode = false
        return input
    }

    override fun onClick(p1: View) {
        if (!isInputMode) {
            return  //ignore!
        }
        val tmp = etCommand.text.toString()
        synchronized(lock) { input = tmp }
        etCommand.setText("")
        adapter!!.add("$$input")
        adapter!!.notifyDataSetChanged()
        lock.complete(Unit)
        return
    }

    lateinit var btGo: Button
    lateinit var lvScreen: ListView
    lateinit var etCommand: EditText
    private var contents: ArrayList<String> = ArrayList()
    private var isInputMode = false
    private var input = ""
    private var adapter: ArrayAdapter<String>? = null
    var lock = CompletableDeferred<Unit>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(DebugTree())
        setContentView(R.layout.main)
        btGo = findViewById(R.id.mainBTDo)
        etCommand = findViewById(R.id.mainETCommand)
        lvScreen = findViewById(R.id.mainListView)
        contents.add("Begin")
        adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, contents)


        // Assign adapter to ListView
        lvScreen.adapter = adapter
        btGo.setOnClickListener(this)
        isInputMode = false

        Timber.d("Oncreate finish.")
        lifecycleScope.launch {
            Timber.d("Launch.")
            FixZip.Run(this@MainActivity)
            Timber.d("Run finished")
            print("Program finished")
        }
    }
}