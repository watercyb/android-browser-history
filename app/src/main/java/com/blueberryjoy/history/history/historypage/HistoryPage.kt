package com.blueberryjoy.history.history.historypage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueberryjoy.history.R
import com.blueberryjoy.history.history.BrowsingHistory
import com.blueberryjoy.history.history.HistoryUrl
import com.blueberryjoy.history.history.utils.Message

class HistoryPage : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var historyAdapter: HistoryAdapter? = null
    var popupWindow: PopupWindow? = null
    var deleted: HistoryUrl? = null
    var deletedPosition = -1
    var deletedActualIndex = -1
    var filter: EditText? = null
    var popupNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        filter = findViewById(R.id.filter)
        val searchButton = findViewById<ImageView?>(R.id.imageButtonSearch)
        val clearButton = findViewById<ImageView?>(R.id.imageButtonClear)

        initializeRecyclerView()

        searchButton!!.setOnClickListener { _: View? ->
            if (filter!!.isGone) {
                filterShow()
            } else {
                filterHide()
            }
        }

        filter!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                historyAdapter?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        clearButton!!.setOnClickListener { _: View? ->
            AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all history? This action cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    BrowsingHistory.clear()
                    initializeRecyclerView()
                    Message.toast("History cleared.")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && filter!!.isVisible) {
            filterHide()
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    private fun initializePopup() {
        if (deleted == null) return
        if (popupWindow != null && popupWindow!!.isShowing) popupWindow!!.dismiss()
        val inflater = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val popupView = inflater.inflate(R.layout.undo_popup, null)
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = false
        popupWindow?.elevation = 10f
        popupWindow?.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
        (popupView.findViewById<View?>(R.id.undo_notice) as TextView).text = "Deleted: ${deleted!!.title}"
        popupView.findViewById<View>(R.id.btn_undo)
            .setOnClickListener { _: View? ->
                if (deleted != null) {
                    historyAdapter?.recover(deleted!!, deletedPosition, deletedActualIndex)
                    Message.toast("History Restored.")
                }
                deleted = null
                popupWindow?.dismiss()
            }

        val currentNum = ++popupNum
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentNum == popupNum && popupWindow?.isShowing == true) popupWindow?.dismiss()
        }, 10000)
    }

    private fun filterShow() {
        filter!!.visibility = View.VISIBLE
        filter!!.requestFocus()
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(filter, 0)
    }

    private fun filterHide() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            filter!!.windowToken,
            0
        )
        filter!!.setText("")
        filter!!.visibility = View.GONE
    }

    private fun initializeRecyclerView() {
        recyclerView?.setLayoutManager(LinearLayoutManager(this))
        historyAdapter = HistoryAdapter({ item: HistoryUrl? ->
            val intent = Intent("bookmark")
            intent.putExtra("link", item?.url)
            intent.putExtra("idx", -1)
            intent.putExtra("new page", false)
            setResult(RESULT_OK, intent)
            finish()
        }, { item: HistoryUrl? ->
            val intent = Intent("bookmark")
            intent.putExtra("link", item?.url)
            intent.putExtra("idx", -1)
            intent.putExtra("new page", true)
            setResult(RESULT_OK, intent)
            finish()
            true
        })
        recyclerView?.setAdapter(historyAdapter)

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val position = viewHolder.getBindingAdapterPosition()
                    if (historyAdapter?.getItemViewType(position) == historyAdapter?.header) return 0
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.getBindingAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        val removed = historyAdapter?.remove(position)
                        deleted = removed?.first
                        deletedActualIndex = removed?.second!!
                        deletedPosition = position
                        initializePopup()
                    }
                }
            })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}