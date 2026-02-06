package com.cericatto.skillpulse.data.model

import java.util.UUID

data class Task(
	val id : String = UUID.randomUUID().toString(),
	val userId: String = "a@a.com",
	val description : String = "Create this app",
	val timestamp : String = "",
	val startTime : String = "",
	val endTime : String = "",
)

fun initTaskList() = List(10) { Task() }