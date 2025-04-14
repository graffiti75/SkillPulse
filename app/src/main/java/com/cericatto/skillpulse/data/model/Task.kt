package com.cericatto.skillpulse.data.model

import java.util.UUID

data class Task(
	val id : String = UUID.randomUUID().toString(),
	val description : String = "Aguar as plantas",
	val timestamp : Long = System.currentTimeMillis(),
	val startTime : Long = System.currentTimeMillis(),
	val endTime : Long = System.currentTimeMillis()
)

fun initTaskList() = List(10) { Task() }