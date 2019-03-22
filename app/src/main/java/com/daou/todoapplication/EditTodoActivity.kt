package com.daou.todoapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add_todo.*
import java.util.*

class EditTodoActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private var dataId:Long = 99999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        val intent: Intent = intent

        if(intent.hasExtra("dataId")) {
            val id = intent.getLongExtra("dataId", 99999)
            dataId = id
            initDataByItem()

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
    }

    private fun initDataByItem() {
        val todoItem = realm.where<TodoDao>().equalTo("id", dataId).findFirst()!!
        addTitleEditView.setText(todoItem.title)
        addContentEditView.setText(todoItem.content)
        addDateView.text = todoItem.date
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_add) {
            if(addTitleEditView.text.toString() != null && addContentEditView.text.toString() != null) {
                realm.beginTransaction()

                val updateHeaderItem = realm.where<TodoDao>().equalTo("id", dataId).findFirst()!!
                updateHeaderItem.date = addDateView.text.toString()
                val updateDataItem = realm.where<TodoDao>().equalTo("id", dataId).findFirst()!!
                updateDataItem.title = addTitleEditView.text.toString()
                updateDataItem.content = addContentEditView.text.toString()

                realm.commitTransaction()

                setResult(Activity.RESULT_OK)
                finish()
            }else {
                Toast.makeText(this, "모든 데이터가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
