package ir.roudi.weather.data

data class Result<out T>(
        val status: Status,
        val data: T? = null,
        val message: String? = null
) {
    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    val isSuccessful : Boolean
        get() = status == Status.SUCCESS

    val isLoading : Boolean
        get() = status == Status.LOADING

    val errorOccurred : Boolean
        get() = status == Status.ERROR

    companion object {
        fun <T> success(data: T? = null): Result<T> {
            return Result(Status.SUCCESS, data)
        }

        fun <T> success(message: String) : Result<T> {
            return Result(Status.SUCCESS, message = message)
        }

        fun <T> error(message: String? = null) : Result<T> {
            return Result(Status.ERROR, message = message)
        }

        fun <T> loading(message: String? = null): Result<T> {
            return Result(Status.LOADING, message = message)
        }
    }

}