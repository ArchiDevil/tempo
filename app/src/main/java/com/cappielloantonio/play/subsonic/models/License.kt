package com.cappielloantonio.play.subsonic.models

import java.util.*

class License {
    var isValid = false
    var email: String? = null
    var licenseExpires: Date? = null
    var trialExpires: Date? = null
}