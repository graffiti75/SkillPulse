package com.cericatto.skillpulse.data.model

import java.util.UUID

data class Task(
	val id : String = UUID.randomUUID().toString(),
	val description : String = "Aguar as plantas",
	val timestamp : String = "",
	val startTime : String = "",
	val endTime : String = "",
)

fun initTaskList() = List(10) { Task() }