package com.cappielloantonio.play.subsonic.models

class MediaType {
    var value: String? = null

    companion object {
        var MUSIC = "music"
        var PODCAST = "podcast"
        var AUDIOBOOK = "audiobook"
        var VIDEO = "video"
    }
}