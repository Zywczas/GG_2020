package com.zywczas.gg2020.services

import android.graphics.Color
import com.zywczas.gg2020.controller.App
import java.util.*

object UserDataService {

    var id = ""
    var name = ""
    var email = ""
    var avatarColor = ""
    var avatarName = ""

    fun logout(){
        id = ""
        name = ""
        email = ""
        avatarName = ""
        avatarColor = ""
        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false
        MessageService.clearMessages()
        MessageService.clearChannels()
    }

    fun returnAvatarColor(components: String) : Int {
        //[0.8117647058823529, 0.5176470588235295, 0.7372549019607844, 1] - function argument
        //we need to remove special marks to leave only Double values with white space between them
        val strippedColor = components.replace("[", "")
                                                .replace("]", "")
                                                .replace(",", "")
        var r = 0
        var g = 0
        var b = 0
        val scanner = Scanner(strippedColor)
        if (scanner.useLocale(Locale.US).hasNextDouble()){
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }
        return Color.rgb(r, g, b)
    }
}