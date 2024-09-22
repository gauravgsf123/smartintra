package com.swayze.smartintra.network

/**
 *Created by Gaurav Kumar on 7-july-2022
 * QUYTECH
 */
sealed class Resource<T>(
    val message: String? = null,
    val response_code: Int? = null,
    val data: T? = null
){
    class Success<T>(message: String?,data: T?=null):Resource<T>(message, data = data)
    class Warning<T>(message: String?,response_code: Int?):Resource<T>(message,response_code)
    class Error<T>(message: String?,data: T?=null):Resource<T>(message, data = data)
    class Loading<T>:Resource<T>()
}
