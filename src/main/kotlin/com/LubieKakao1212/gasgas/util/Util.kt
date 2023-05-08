package com.LubieKakao1212.gasgas.util

fun <T> T?.chainToList(sizePrediction : Int = 1, nextGetter : (T) -> T?) : List<T> {
    val chain = ArrayList<T>(sizePrediction)
    var node = this
    while(node != null) {
        chain.add(node)
        node = nextGetter(node)
    }
    return chain
}
