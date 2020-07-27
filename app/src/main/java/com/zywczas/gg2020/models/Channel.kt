package com.zywczas.gg2020.models

class Channel (val name: String, val id: String) {
    override fun toString(): String {
        return "#$name"
    }
}