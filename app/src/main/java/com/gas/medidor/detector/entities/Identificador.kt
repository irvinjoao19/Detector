package com.gas.medidor.detector.entities

open class Identificador {

    var name: String? = ""
    var address:String? = ""

    constructor(name: String?, address: String?) {
        this.name = name
        this.address = address
    }

    constructor()
}