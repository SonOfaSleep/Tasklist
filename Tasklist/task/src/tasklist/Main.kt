package tasklist

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

val jsonFile = File("tasklist.json")

data class Task (var priority: String, var date: String, var time: String, var tasks: MutableList<String>) {
    fun getPrClr(): String {
        return when (priority) {
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            else -> "\u001B[104m \u001B[0m"
        }
    }
    fun getTagClr(): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Kiev")).date
        val daysToTask = currentDate.daysUntil(date.toLocalDate())
        return when {
            // >0 = "I"; ==0 = "T"; <0 = "O"
            daysToTask > 0 -> "\u001B[102m \u001B[0m"
            daysToTask == 0 -> "\u001B[103m \u001B[0m"
            else -> "\u001B[101m \u001B[0m"
        }
    }
}

class Tasker (private var taskList: MutableList<Task> = mutableListOf()) {
    init {
        if (jsonFile.exists()) writeLoadJSON("load")
        menu()
    }

    private fun menu() {
        while (true) {
            println("Input an action (add, print, edit, delete, end):")
            when (readLine()!!.lowercase()) {
                "add" -> addTasks()
                "print" -> tablePrint()
                "edit" -> editMenu()
                "delete" -> deleteMenu()
                "end" -> {
                    writeLoadJSON("write")
                    break
                }
                else -> println("The input action is invalid")
            }
        }
        println("Tasklist exiting!")
    }

    private fun addTasks() {
        val priority = getPriority()
        val date = getDate()
        val time = getTime()
        val tasks = getTasks()

        if (tasks.isNotEmpty()) taskList.add(Task(priority, date, time, tasks))
    }
    private fun getPriority(): String {
        val regex = Regex("[CHNL]")
        var priority = ""
        while (!priority.matches(regex)) {
            println("Input the task priority (C, H, N, L):")
            priority = readLine()!!.uppercase()
        }
        return priority
    }
    private fun getDate(): String {
        var date: LocalDate
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val dateString = readLine()!!.split("-")
            try {
                date = LocalDate.of(dateString[0].toInt(), dateString[1].toInt(), dateString[2].toInt())
                break
            } catch (e:Exception) {
                println("The input date is invalid")
            }
        }
        return date.toString()
    }
    private fun getTime(): String {
        var time: LocalTime
        while (true) {
            println("Input the time (hh:mm):")
            try {
                val input = readLine()!!.split(":")
                time = LocalTime.of(input[0].toInt(), input[1].toInt())
                break
            } catch (e:Exception) {
                println("The input time is invalid")
            }
        }
        return time.toString()
    }
    private fun getTasks(): MutableList<String> {
        val tasks = mutableListOf<String>()
        println("Input a new task (enter a blank line to end):")
        while (true) {
            val input = readLine()!!
            if (input.isBlank()) {
                if (tasks.isEmpty()) println("The task is blank")
                break
            } else {
                tasks += formatString(input)
            }
        }
        return tasks
    }
    private fun formatString(input: String): MutableList<String> {
        val list = input.chunked(44).toMutableList()
        for (i in list.indices) {
            list[i] = list[i].padEnd(44, ' ')
        }
        return list
    }

    private fun tablePrint() {
        if (taskList.isEmpty()) {
            println("No tasks have been input")
        } else {
            println("""
            +----+------------+-------+---+---+--------------------------------------------+
            | N  |    Date    | Time  | P | D |                   Task                     |
            +----+------------+-------+---+---+--------------------------------------------+
        """.trimIndent())
            val space = "+----+------------+-------+---+---+--------------------------------------------+"

            taskList.forEachIndexed { index, task ->
                val nWS = if (index > 8) "${index + 1}" else "${index + 1} "
                val prior = task.getPrClr()
                val tag = task.getTagClr()
                println("| $nWS | ${task.date} | ${task.time} | $prior | $tag |${task.tasks[0]}|")
                for (i in 1..task.tasks.lastIndex) {
                    println("|    |            |       |   |   |${task.tasks[i]}|")
                }
                println(space)
            }
        }
    }

    private fun checkEmpty(): Boolean {
        return if (taskList.isNotEmpty()) true
        else {
            println("No tasks have been input")
            false
        }
    }
    private fun getTaskIndex(): Int {
        while (true) {
            println("Input the task number (1-${taskList.size}):")
            val input = readLine()!!
            if (input.matches("[1-${taskList.size}]".toRegex())) return input.toInt() - 1
            else println("Invalid task number")
        }
    }

    private fun editMenu() {
        if (checkEmpty()) {
            tablePrint()
            editTask(getTaskIndex())
        }
    }
    private fun editTask(taskIndex: Int) {
        val allowed = listOf("priority", "date", "time", "task")
        var input = ""
        while (true) {
            println("Input a field to edit (priority, date, time, task):")
            input = readLine()!!.lowercase()
            if (allowed.contains(input)) break
            else println("Invalid field")
        }
        when (input) {
            "priority" -> taskList[taskIndex].priority = getPriority()
            "date" -> taskList[taskIndex].date = getDate()
            "time" -> taskList[taskIndex].time = getTime()
            else -> taskList[taskIndex].tasks = getTasks()
        }
        println("The task is changed")
    }

    private fun deleteMenu() {
        if (checkEmpty()) {
            tablePrint()
            deleteTask(getTaskIndex())
        }
    }
    private fun deleteTask(taskIndex: Int) {
        taskList.removeAt(taskIndex)
        println("The task is deleted")
    }

    private fun writeLoadJSON (purpose: String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
        val taskListAdapter = moshi.adapter<MutableList<Task>>(type)

        when (purpose) {
            "write" -> {
                val jsonString = taskListAdapter.indent(" ").toJson(taskList)
                val jsonFile = File("tasklist.json")
                jsonFile.writeText(jsonString)
            }
            "load" -> {
                val jsonString = jsonFile.readText()
                try {
                    val newTasks = taskListAdapter.fromJson(jsonString)
                    taskList = newTasks!!
                } catch (e:Exception) {
                    println("Not correct JSON")
                }
            }
        }

    }
}

fun main() {
    val tasks = Tasker()
}