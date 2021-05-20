package ir.roudi.weather.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ir.roudi.weather.data.Result
import retrofit2.HttpException
import java.lang.Exception
import java.net.UnknownHostException

abstract class SafeApiCall {

    suspend fun<T> apiCall(call: suspend () -> T) : Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(call.invoke())
            } catch (exception: Exception) {
                Result.error(extractMessage(exception))
            }
        }
    }

    private fun extractMessage(exception: Exception) : String {
        return when (exception) {
            is UnknownHostException -> {
                "No Internet"
            }
            is HttpException -> {
                when (exception.code()) {
                    404 -> "Not found!"
                    else -> "Something went wrong"
                }
            }
            else -> {
                exception.printStackTrace()
                exception.message ?: "Something went wrong"
            }
        }
    }
}