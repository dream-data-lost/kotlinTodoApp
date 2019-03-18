package com.daou.todoapplication

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TodoDao (
    @PrimaryKey var id: Int = 0,
    var title: String = "",
    var content: String = "",
    var date: String,
    var completed: Boolean
): RealmObject()