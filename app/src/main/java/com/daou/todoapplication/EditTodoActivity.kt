package com.daou.todoapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add_todo.*
import java.text.SimpleDateFormat
import java.util.*

class EditTodoActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()

    private var dataId = 99999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        val intent: Intent = intent

        if(intent.hasExtra("dataItem")) {
            val item = intent.getParcelableExtra<ParcelableDataItem>("dataItem")
            initDataByItem(item)
        }
        val date = Date()
        val sdFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        addDateView.text = sdFormat.format(date)

        addDateView.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                var mm = (month + 1).toString()
                var dd = dayOfMonth.toString()
                if(month < 10) {
                    mm = "0$mm"
                }
                if(dayOfMonth < 10) {
                    dd = "0$dd"
                }
                addDateView.text = "$year-$mm-$dd"
            }, year, month, day).show()
        }
    }

    private fun initDataByItem(dataItem: ParcelableDataItem) {
        dataId = dataItem.id
        addTitleEditView.setText(dataItem.title)
        addContentEditView.setText(dataItem.content)

        //Realm
//        val todo = realm.where<TodoDao>().equalTo("id", dataItem.id).findFirst()!!
//        addTitleEditView.setText(todo.title)
//        addContentEditView.date = todo.date
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_add) {
            if(addTitleEditView.text.toString() != null && addContentEditView.text.toString() != null) {
                val helper = DBHelper(this)
                val db = helper.writableDatabase
                val contentValues = ContentValues()
                contentValues.put("title", addTitleEditView.text.toString())
                contentValues.put("content", addContentEditView.text.toString())
                contentValues.put("date", addDateView.text.toString())
                contentValues.put("completed", 0)

                db.update("tb_todo", contentValues, "_id=$dataId", null)

                db.close()

                //Realm
//                realm.beginTransaction()
//                val updateItem = realm.where<TodoDao>().equalTo("id", dataId).findFirst()!!
//                updateItem.title = addTitleEditView.text.toString()
//                updateItem.content = addContentEditView.text.toString()
//                updateItem.date = addDateView.text.toString()
//
//                realm.commitTransaction()

                setResult(Activity.RESULT_OK)
                finish()
            }else {
                Toast.makeText(this, "모든 데이터가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
