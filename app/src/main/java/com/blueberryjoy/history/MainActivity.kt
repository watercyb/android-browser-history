package com.blueberryjoy.history

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueberryjoy.history.history.BrowsingHistory
import com.blueberryjoy.history.history.BrowsingHistory.debouncedInsert
import com.blueberryjoy.history.history.HistorySuggestionAdapter
import com.blueberryjoy.history.history.HistoryURL
import com.blueberryjoy.history.history.historypage.HistoryPage
import com.blueberryjoy.history.history.utils.Favicon
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Favicon.initialize()
        BrowsingHistory.initialize()

        val webUrl = findViewById<EditText>(R.id.url)
        val webView = findViewById<WebView>(R.id.webView)
        val recyclerViewHistory = findViewById<RecyclerView>(R.id.recyclerView)
        val button = findViewById<Button>(R.id.button)

        val startActivityIntent = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val url = result.data?.getStringExtra("link")
                if (!url.isNullOrEmpty()) {
                    webUrl.setText(url)
                    webView.loadUrl(url)
                }
            }
        }

        recyclerViewHistory.setLayoutManager(LinearLayoutManager(this@MainActivity))
        val historySuggestionAdapter =
            HistorySuggestionAdapter(java.util.ArrayList<HistoryURL>()) { item: HistoryURL ->
                val url: String = item.url
                webUrl.setText(url)
                webView.loadUrl(url)
                webUrl.clearFocus()
            }
        recyclerViewHistory.setAdapter(historySuggestionAdapter)

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.getBindingAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) historySuggestionAdapter.remove(
                        position
                    )
                }
            })
        itemTouchHelper.attachToRecyclerView(recyclerViewHistory)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                super.onReceivedIcon(view, icon)
                if (view.url != null) Favicon.saveFavicon(icon, view.url!!)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    if (view == null || view.url == null || title == null) return@Runnable
                    debouncedInsert(view.url!!, title)
                    webUrl.setText(view.url)
                }, 1000)
            }
        }

        webUrl.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                for (i in s.length - 1 downTo 0) {
                    if (s[i] == '\n') {
                        s.delete(i, i + 1)
                        val url: String = try {
                            URLDecoder.decode(webUrl.text.toString(), "UTF-8")
                        } catch (_: UnsupportedEncodingException) {
                            webUrl.text.toString()
                        }
                        webView.loadUrl(url)
                        webUrl.clearFocus()
                        return
                    }
                }

                if (webUrl.isFocused) {
                    val list: ArrayList<HistoryURL> = BrowsingHistory.search(webUrl.text.toString())
                    historySuggestionAdapter.addList(list)
                }
            }
        })

        webUrl.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                recyclerViewHistory.visibility = View.VISIBLE
                val list: ArrayList<HistoryURL> = BrowsingHistory.search(webUrl.text.toString())
                historySuggestionAdapter.addList(list)
                webUrl.isSingleLine = false
            } else {
                recyclerViewHistory.visibility = View.GONE
                webUrl.isSingleLine = true
            }
        }

        button.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryPage::class.java)
            startActivityIntent.launch(intent)
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webUrl.hasFocus()) webUrl.clearFocus()
            }
        })
    }
}