package com.daou.todoapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_main.view.*


class MainActivity : AppCompatActivity() {

    //할일 목록
    private val realm = Realm.getDefaultInstance()
    private var todoList: MutableList<TodoDao> = mutableListOf()
    val TYPE_HEADER = 0
    val TYPE_DATA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectDB()
        recyclerView.addItemDecoration(MyDecoration())

        fab.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivityForResult(intent, 10)
        }

    }

    private fun selectDB() {
        val list: RealmResults<TodoDao> = realm.where<TodoDao>().findAll().sort("date", Sort.DESCENDING)
        convertListForHeader(list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(todoList)
    }

    private fun convertListForHeader(list: RealmResults<TodoDao>) {
        val newList:MutableList<TodoDao> = mutableListOf()
        var date: String? = null
        for(item in list) {
            if((date == null) || (!date.equals(item.date))) {
                val headerItem = TodoDao(nextId())
                headerItem.date = item.date
                headerItem.type = 0
                newList.add(headerItem)
                newList.add(item)
                date = item.date
            } else {
                newList.add(item)
            }
        }
        todoList = newList
    }

    private fun nextId(): Long {
        val maxId = realm.where<TodoDao>().max("id")
        if(maxId != null) {
            return maxId.toLong() + 1
        }
        return 0
    }

    //항목 추가 후 화면으로 돌아올 때 사용
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==10 && resultCode== Activity.RESULT_OK) {
            selectDB()
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerView = view.itemHeaderView!!
    }

    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val completedIconView = view.completedIconView!!
        val mainItemLayout = view.mainItemLayout!!
        val itemTitleView = view.itemTitleView!!
        val itemContentView = view.itemContentView!!
        val itemDeleteView = view.itemDeleteView!!
    }

    inner class MyAdapter(private val list: MutableList<TodoDao>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return if(list[position].type == TYPE_HEADER){
                TYPE_HEADER
            }else {
                TYPE_DATA
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return if(viewType == TYPE_HEADER) {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            } else {
                DataViewHolder(layoutInflater.inflate(R.layout.item_main, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val todoItem = list[position]
            if (todoItem.type == TYPE_HEADER) {
                val viewHolder = holder as HeaderViewHolder
                viewHolder.headerView.text = todoItem.date
            } else {
                val viewHolder = holder as DataViewHolder
                viewHolder.itemTitleView.text = todoItem.title
                viewHolder.itemContentView.text = todoItem.content
                if (todoItem.completed) {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                } else {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon)
                }
                viewHolder.completedIconView.setOnClickListener {
                    realm.beginTransaction()
                    val updateItem = realm.where<TodoDao>().equalTo("id", todoItem.id).findFirst()!!
                    updateItem.completed = !todoItem.completed
                    realm.commitTransaction()
                    if (todoItem.completed) {
                        viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                    } else {
                        viewHolder.completedIconView.setImageResource(R.drawable.icon)
                    }
                }

                viewHolder.mainItemLayout.setOnClickListener {
                    val intent = Intent(this@MainActivity, EditTodoActivity::class.java)
                    val dataId = todoItem.id
                    intent.putExtra("dataId", dataId)
                    startActivityForResult(intent, 10)
                }

                viewHolder.itemDeleteView.setOnClickListener {
                    realm.beginTransaction()

                    val deleteItem = realm.where<TodoDao>().equalTo("id", todoItem.id).findFirst()!!
                    RealmObject.deleteFromRealm(deleteItem)

                    realm.commitTransaction()
                    selectDB()
                }
            }
        }
        override fun getItemCount(): Int {
            return list.size
        }
    }

    inner class MyDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.getChildAdapterPosition(view)
            val todoItem = todoList[index]
            if(todoItem.type == TYPE_DATA) {
                view.setBackgroundColor(0xFFFFFFFF.toInt())
                ViewCompat.setElevation(view, 10.0f)
            }

            outRect.set(30, 20, 30, 20)
        }
    }

}