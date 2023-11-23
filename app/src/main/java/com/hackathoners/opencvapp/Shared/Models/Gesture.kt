package com.hackathoners.opencvapp.Shared.Models

enum class Gesture(val value: String) {
    // "None", "Closed_Fist", "Open_Palm", "Pointing_Up", "Thumb_Down", "Thumb_Up", "Victory", "ILoveYou"
    None("None"),
    Closed_Fist("Closed_Fist"),
    Open_Palm("Open_Palm"),
    Pointing_Up("Pointing_Up"),
    Thumb_Down("Thumb_Down"),
    Thumb_Up("Thumb_Up"),
    Victory("Victory"),
    ILoveYou("ILoveYou")
}

// q: how to create instance of Gesture given a string?
// a: Gesture.valueOf("Closed_Fist")