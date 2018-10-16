package com.gas.medidor.detector.entities

open class Medidor {

    var valor:Int?=0
    var mensaje:String?=""

    constructor()

    constructor(valor: Int?, mensaje: String?) {
        this.valor = valor
        this.mensaje = mensaje
    }
}