package com.daou.todoapplication

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TodoDao(
    @PrimaryKey var id: Long = 0,
    var title: String = "",
    var content: String = "",
    var completed: Boolean = false,
    var date: String = "",
    var type: Int = 1
):RealmObject()