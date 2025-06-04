package org.example.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import java.sql.Connection
import java.sql.DriverManager

object Users : Table() {
    val chatId = long("chatId").uniqueIndex()
    val name = varchar("name", 50)
    val age = integer("age")
    val weight = integer("weight")
    val height = integer("height")
    val goal = varchar("goal", 255)
    val state = varchar("state", 50).default("COMPLETED")
    override val primaryKey = PrimaryKey(chatId)
}

object DatabaseService {
    fun init() {
        Database.connect("jdbc:sqlite:healthylife.db", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(Users)
        }
    }

    fun addOrUpdateUser(chatId: Long, name: String, age: Int, weight: Int, height: Int, goal: String, state: String = "COMPLETED") {
        transaction {
            val exists = Users.select { Users.chatId eq chatId }.count() > 0
            if (exists) {
                Users.update({ Users.chatId eq chatId }) {
                    it[Users.name] = name
                    it[Users.age] = age
                    it[Users.weight] = weight
                    it[Users.height] = height
                    it[Users.goal] = goal
                    it[Users.state] = state
                }
            } else {
                Users.insert {
                    it[Users.chatId] = chatId
                    it[Users.name] = name
                    it[Users.age] = age
                    it[Users.weight] = weight
                    it[Users.height] = height
                    it[Users.goal] = goal
                    it[Users.state] = state
                }
            }
        }
    }

    fun updateUserState(chatId: Long, state: String) {
        transaction {
            Users.update({ Users.chatId eq chatId }) {
                it[Users.state] = state
            }
        }
    }

    fun getUser(chatId: Long): ResultRow? {
        return transaction {
            Users.select { Users.chatId eq chatId }.singleOrNull()
        }
    }

    fun getAllUsers(): List<ResultRow> {
        return transaction {
            Users.selectAll().toList()
        }
    }
}
