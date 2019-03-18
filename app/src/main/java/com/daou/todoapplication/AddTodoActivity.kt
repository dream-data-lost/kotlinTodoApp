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
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add_todo.*
import java.text.SimpleDateFormat
import java.util.*

class AddTodoActivity : AppCompatActivity() {

//    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

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

                db.insert("tb_todo", null, contentValues)

                db.close()
                //Realm
//            val newItem = realm.createObject<TodoDao>(nextId())
//            newItem.title = addTitleEditView.text.toString()
//            newItem.content = addContentEditView.text.toString()
//            newItem.date = addDateView.text.toString()
//            newItem.completed = false
//
//            realm.commitTransaction()

                setResult(Activity.RESULT_OK)
                finish()
            }else {
                Toast.makeText(this, "모든 데이터가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }


        }
        return super.onOptionsItemSelected(item)
    }

//    private fun nextId(): Int {
//        val maxId = realm.where<TodoDao>().max("id")
//        if(maxId != null) {
//            return maxId.toInt() + 1
//        }
//        return 0
//    }

}
