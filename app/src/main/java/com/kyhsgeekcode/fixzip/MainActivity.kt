package com.kyhsgeekcode.fixzip

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.assetpacks.*
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.google.android.play.core.tasks.RuntimeExecutionException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, IConsole {
    private lateinit var assetPackManager: AssetPackManager
    private var waitForWifiConfirmationShown = false
    private val packName: String by lazy {
        packNameByDensity()
    }

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_download -> {
                downloadResources()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun downloadResources() {
        Timber.d("Downloading resources")
        initAssetPackManager()
        loadAssets()
    }

    private fun initAssetPackManager() {
        assetPackManager = AssetPackManagerFactory.getInstance(applicationContext)
    }

    private fun loadAssets() {
        val onDemandAssetPackPath: String? =
            assetPackManager.getAbsoluteAssetPath(packName, "test.txt")
        if (onDemandAssetPackPath == null) {
            Timber.d(" OnDemand AssetPath : Null; fetching packname: $packName")
            assetPackManager.registerListener(assetPackStateUpdateListener)
            assetPackManager.fetch(listOf(packName)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Timber.d("Oncomplete Success" + it.result.packStates().keys)
                } else {
                    Timber.d("OnComplete faild fetch " + it.exception)
                }
            }
            Timber.d(" Called fetch")
        } else {
            // ready. load
            Timber.d(" Ready. load")
            initWithLoadedAssets(onDemandAssetPackPath)
        }
    }

    private fun initWithLoadedAssets(path: String) {
        val file = File(path)
        Timber.d("Init with loaded assets. Path: $path, file: $file")
        val helpText = file.readText(Charsets.UTF_8)
        print(helpText)
    }

    private var assetPackStateUpdateListener =
        AssetPackStateUpdateListener { state ->
            when (state.status()) {
                AssetPackStatus.PENDING -> Timber.i("Pending")
                AssetPackStatus.DOWNLOADING -> {
                    val downloaded = state.bytesDownloaded()
                    val totalSize = state.totalBytesToDownload()
                    val percent = 100.0 * downloaded / totalSize
                    Timber.i("PercentDone=" + String.format("%.2f", percent))
                }
                AssetPackStatus.TRANSFERRING -> {
                    Timber.d("Transferrring")
                }
                AssetPackStatus.COMPLETED -> {
                    Timber.d("Completed")
                    loadAssets()

                }                    // Asset pack is ready to use. Start the Game/App.
                AssetPackStatus.FAILED -> {                  // Request failed. Notify user.
                    Timber.d("Failed" + state.errorCode().toString())

                }
                AssetPackStatus.CANCELED -> {
                    Timber.d("Canceled")
                }
                AssetPackStatus.WAITING_FOR_WIFI -> showWifiConfirmationDialog()
                AssetPackStatus.NOT_INSTALLED -> {
                    Timber.d("Not installed")
                }
                AssetPackStatus.UNKNOWN -> {
                    Timber.d("Unknown")
                }
            }
        }

    private fun AssetPackManager.loadAssetByName(assetPackName: String) {
        getPackStates(Collections.singletonList(assetPackName))
            .addOnCompleteListener {
                val assetPackStates: AssetPackStates
                try {
                    assetPackStates = it.result
                    val assetPackState: AssetPackState? =
                        assetPackStates.packStates()[assetPackName]
                } catch (e: RuntimeExecutionException) {
                    Timber.e(e, "Failed to get pack states")
                }
            }
    }

    private fun showWifiConfirmationDialog() {
        if (!waitForWifiConfirmationShown) {
            assetPackManager.showCellularDataConfirmation(this@MainActivity)
                .addOnSuccessListener { resultCode ->
                    if (resultCode == RESULT_OK) {
                        Timber.d("Confirmation dialog has been accepted.")
                        loadAssets()
                    } else if (resultCode == RESULT_CANCELED) {
                        Timber.d("Confirmation dialog has been denied by the user.")
                        Toast.makeText(this,
                            "Please Connect to Wifi to begin app files to download",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            waitForWifiConfirmationShown = true
        }
    }

    private fun AssetPackManager.getAbsoluteAssetPath(
        assetPack: String,
        relativeAssetPath: String,
    ): String? {
        val assetPackPath: AssetPackLocation = getPackLocation(assetPack)
            ?: // asset pack is not ready
            return null
        val assetsFolderPath: String? = assetPackPath.assetsPath()
        // equivalent to: FilenameUtils.concat(assetPackPath.path(), "assets");
        return assetsFolderPath + relativeAssetPath
    }

    lateinit var btGo: Button
    lateinit var lvScreen: ListView
    lateinit var etCommand: EditText
    private var contents: ArrayList<String> = ArrayList()
    private var isInputMode = false
    private var input = ""
    private var adapter: ArrayAdapter<String>? = null
    var lock = CompletableDeferred<Unit>()

    inner class LogTree : Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            print("$priority:$tag:$message:$t")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(LogTree())
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

    private fun packNameByDensity() = "drawable_${getDensity()}"

    private fun getDensity(): String {
        return "hdpi"
        val d = resources.displayMetrics.density

        return when {
            d < 1.0f -> "ldpi"
            d < 1.5f -> "mdpi"
            d < 2.0f -> "hdpi"
            d < 3.0f -> "xhdpi"
            d < 4.0f -> "xxhdpi"
            else -> "xxxhdpi"
        }
    }
}