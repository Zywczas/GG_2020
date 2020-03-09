package com.example.gg2020.Model

class Channel (val name: String, val description: String, val id: String) {                         //bedzie przyjmowac wartosci kanalu od API
    override fun toString(): String {
        return "#$name"
    }
}