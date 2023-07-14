package com.codellyrandom.hassle

interface Command {
    val id: Int?

    fun copy(id: Int?): Command
}
