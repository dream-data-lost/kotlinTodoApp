package com.daou.todoapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_header.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_main.view.*
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {

    //할일 목록
    var list: MutableList<ItemVO> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectDB()

        fab.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            intent.putExtra("flag", "insert")
            startActivityForResult(intent, 10)
        }

        recyclerView.addItemDecoration(MyDecoration())
    }

    private fun selectDB() {
        list = mutableListOf()
        val helper = DBHelper(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("select * from tb_todo order by date desc", null)

        var preDate: Calendar? = null
        while(cursor.moveToNext()) {
            val dbdate = cursor.getString(3)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).parse(dbdate)
            val currentDate = GregorianCalendar()
            currentDate.time = date

            if(!currentDate.equals(preDate)) {
                val headerItem = HeaderItem(dbdate)
                list.add(headerItem)
                preDate = currentDate
            }

            val completed = cursor.getInt(4) != 0
            val dataItem = DataItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), completed)
            list.add(dataItem)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(list)
    }

    //항목 추가 후 화면으로 돌아올 때 사용
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==10 && resultCode== Activity.RESULT_OK) {
            selectDB()
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerView = view.itemHeaderView
    }

    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val completedIconView = view.completedIconView!!
        val mainItemLayout = view.mainItemLayout!!
        val itemTitleView = view.itemTitleView!!
        val itemContentView = view.itemContentView!!
        val itemDeleteView = view.itemDeleteView!!
    }

    inner class MyAdapter(val list: MutableList<ItemVO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return list.get(position).type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ItemVO.TYPE_HEADER) {
                val layoutInflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            } else {
                val layoutInflater = LayoutInflater.from(parent.context)
                return DataViewHolder(layoutInflater.inflate(R.layout.item_main, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemVO = list.get(position)

            if(itemVO.type == ItemVO.TYPE_HEADER) {
                val viewHolder = holder as HeaderViewHolder
                val headerItem = itemVO as HeaderItem
                viewHolder.headerView.text = headerItem.date
            } else {
                val viewHolder = holder as DataViewHolder
                val dataItem = itemVO as DataItem
                viewHolder.itemTitleView.setText(dataItem.title)
                viewHolder.itemContentView.setText(dataItem.content)
                if(dataItem.completed) {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                }else {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon)
                }

                viewHolder.completedIconView.setOnClickListener {
                    val helper = DBHelper(this@MainActivity)
                    val db = helper.writableDatabase

                    if(dataItem.completed) {
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(0, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon)
                    } else {
                        db.execSQL("update tb_todo set completed=? where _id=?", arrayOf(1, dataItem.id))
                        viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                    }
                    dataItem.completed = !dataItem.completed
                    db.close()
                }

                viewHolder.mainItemLayout.setOnClickListener {
                    val intent = Intent(this@MainActivity, AddTodoActivity::class.java)
                    val currentData = ParcelableDataItem(dataItem.id, dataItem.title, dataItem.content, dataItem.completed)
                    intent.putExtra("flag", "update")
                    intent.putExtra("dataItem", currentData)
                    startActivityForResult(intent, 10)
                }

                viewHolder.itemDeleteView.setOnClickListener {
                    val helper = DBHelper(this@MainActivity)
                    val db = helper.writableDatabase

                    db.execSQL("delete from tb_todo where _id =" + dataItem.id)
                    db.close()
                    selectDB()
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    inner class MyDecoration() : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.getChildAdapterPosition(view)
            val itemVO = list.get(index)
            if(itemVO.type == ItemVO.TYPE_DATA) {
                view.setBackgroundColor(0xFFFFFFFF.toInt())
                ViewCompat.setElevation(view, 10.0f)
            }

            outRect.set(30, 20, 30, 20)
        }
    }

}

abstract class ItemVO {
    abstract val type: Int
    companion object {
        val TYPE_HEADER = 0
        val TYPE_DATA = 1
    }
}

class HeaderItem(var date: String) : ItemVO() {
    override val type: Int
        get() = ItemVO.TYPE_HEADER
}

internal class DataItem(var id: Int, var title: String, var content: String,
                        var completed: Boolean = false) : ItemVO() {
    override val type: Int
        get() = ItemVO.TYPE_DATA
}

internal class ParcelableDataItem constructor(var id: Int, var title: String, var content: String,
                                              var completed: Boolean = false) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(content)
        dest.writeValue(completed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableDataItem> {
        override fun createFromParcel(parcel: Parcel): ParcelableDataItem {
            return ParcelableDataItem(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableDataItem?> {
            return arrayOfNulls(size)
        }
    }

}

