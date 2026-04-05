package edu.nd.jnkouka.hwapp.four.screens

import kotlinx.serialization.Serializable


object Routes {
    const val LOGIN = "login"
    const val LIST = "list"
    const val POST = "post/{postId}"

    fun post(postId: String): String = "post/$postId"
}


