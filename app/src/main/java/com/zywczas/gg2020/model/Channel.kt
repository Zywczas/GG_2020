package com.zywczas.gg2020.model

class Channel (val name: String, val id: String) {
    override fun toString(): String {
        return "#$name"
    }
}