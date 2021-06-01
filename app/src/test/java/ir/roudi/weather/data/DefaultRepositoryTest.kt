package ir.roudi.weather.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import ir.roudi.weather.MainCoroutineRule
import ir.roudi.weather.data.Result.Status.*
import ir.roudi.weather.data.TestUtil.expectData
import ir.roudi.weather.data.TestUtil.expectStatuses
import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.FakeCityDao
import ir.roudi.weather.data.local.db.dao.FakeWeatherDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.db.getOrAwaitValueTest
import ir.roudi.weather.data.local.pref.FakeSharedPrefHelper
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.FakeService
import ir.roudi.weather.data.repository.DefaultRepository
import ir.roudi.weather.data.repository.Repository

import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.time.ExperimentalTime

// Note: Using [runBlockingTest] causes some tests to be flaky by throwing [IllegalStateException]
// sometimes so I used [runBlocking] instead.
@ExperimentalCoroutinesApi
@ExperimentalTime
class DefaultRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: Repository

    private lateinit var cityDao : CityDao
    private lateinit var weatherDao: WeatherDao
    private lateinit var service: FakeService
    private lateinit var pref : SharedPrefHelper

    // These two items exists in the api but not in the database
    private lateinit var diffCity : RemoteCity
    private lateinit var diffWeather : RemoteWeather

    @Before
    fun setup() {
        // Generate local cities and weathers with cityId starting from 1
        val localCityMap = TestUtil.generateLocalCityMap(3)
        val localWeatherMap = TestUtil.generateLocalWeatherMap(3)

        // Generate remote cities and weathers with cityId starting from 1
        val remoteCities = TestUtil.generateRemoteCity(4)
        diffCity = remoteCities.last()

        val remoteWeatherMap = TestUtil.generateRemoteWeatherMap(4)
        diffWeather = remoteWeatherMap[diffCity.id]!!

        // Instantiating dependencies
        cityDao = FakeCityDao(localCityMap)
        weatherDao = FakeWeatherDao(localWeatherMap)
        service = FakeService(remoteCities, remoteWeatherMap)
        pref = FakeSharedPrefHelper()

        repository = DefaultRepository(
                cityDao,
                weatherDao,
                service,
                pref,
        )
    }

    suspend fun pickSomeCityFromDatabase() =
            cityDao.getAllCities().getOrAwaitValueTest()[0]

    @Test
    fun `cities - Contains all cities saved in database`() {
        // WHEN
        val repositoryCities = repository.cities.getOrAwaitValueTest()

        // THEN
        val citiesInDb = cityDao.getAllCities().getOrAwaitValueTest()
        assertThat(repositoryCities).isEqualTo(citiesInDb)
    }

    @Test
    fun `insertCity(Double,Double) - Happy path`() = runBlocking {
        // WHEN
        val result = diffCity.coordinates.let {
            repository.insertCity(it.latitude, it.longitude)
        }

        // THEN
        result.expectStatuses(LOADING, SUCCESS)

        val cityInDb = cityDao.getCity(diffCity.id).getOrAwaitValueTest()
        assertThat(cityInDb).isEqualTo(diffCity.toLocalCity())
    }

    @Test
    fun `insertCity(Double,Double) - Network error - Returns error`() = runBlocking {
        // GIVEN
        service.shouldReturnNetworkError = true

        // WHEN
        val result = diffCity.coordinates.let {
            repository.insertCity(it.latitude, it.longitude)
        }

        // THEN
        result.expectStatuses(LOADING, ERROR)
    }

    @Test
    fun `insertCity(Double,Double) - Invalid city - Returns error`() = runBlocking {
        // WHEN - there is no city available with the given coordinate
        val result = repository.insertCity(100.0, 100.0)

        // THEN
        result.expectStatuses(LOADING, ERROR)
    }

    @Test
    fun `insertCity(RemoteCity) - Happy path`() = runBlocking {
        // WHEN
        val result = repository.insertCity(diffCity)

        // THEN
        result.expectStatuses(LOADING, SUCCESS)

        val cityInDb = cityDao.getCity(diffCity.id).getOrAwaitValueTest()
        assertThat(cityInDb).isEqualTo(diffCity.toLocalCity())

        val weatherInDb = weatherDao.getWeather(diffCity.id).getOrAwaitValueTest()
        assertThat(weatherInDb).isEqualTo(
                diffWeather.toLocalWeather(diffCity.id)
        )
    }

    @Test
    fun `insertCity(RemoteCity) - Network error - Returns success and then error`() = runBlocking {
        // GIVEN
        service.shouldReturnNetworkError = true

        // WHEN
        val result = repository.insertCity(diffCity)

        // THEN
        // Success means it could save the city in the database
        // Error means it couldn't fetch the city's weather
        result.expectStatuses(LOADING, SUCCESS, ERROR)

        val cityInDb = cityDao.getCity(diffCity.id).getOrAwaitValueTest()
        assertThat(cityInDb).isEqualTo(diffCity.toLocalCity())
    }

    @Test
    fun `deleteCity(LocalCity) - Happy path`() = runBlocking {
        // GIVEN
        val someCity = pickSomeCityFromDatabase()

        // WHEN
        repository.deleteCity(someCity)

        // THEN
        val citiesInDb = cityDao.getAllCities().getOrAwaitValueTest()
        assertThat(citiesInDb).doesNotContain(someCity)
    }

    @Test
    fun `updateCity(LocalCity) - Happy path`() = runBlocking {
        // GIVEN
        val someCity = pickSomeCityFromDatabase()

        // WHEN
        someCity.name = "updated name"
        repository.updateCity(someCity)

        // THEN
        val cityInDb = cityDao.getCity(someCity.cityId).getOrAwaitValueTest()
        assertThat(cityInDb).isEqualTo(someCity)
    }

    @Test
    fun `getCity(Int) - Happy path`() = runBlocking {
        // GIVEN
        val someCity = pickSomeCityFromDatabase()

        // WHEN
        val city = repository.getCity(someCity.cityId)

        // THEN
        assertThat(city.getOrAwaitValueTest()).isEqualTo(someCity)
    }

    @Test
    fun `findCity(String) - Happy path`() = runBlocking {
        // WHEN
        val result = repository.findCity(diffCity.name)

        // THEN
        result.expectStatuses(LOADING, SUCCESS)
        result.expectData(diffCity)
    }

    @Test
    fun `findCity(String) - Network error - Returns error`() = runBlocking {
        // GIVEN
        service.shouldReturnNetworkError = true

        // WHEN
        val result = repository.findCity(diffCity.name)

        // THEN
        result.expectStatuses(LOADING, ERROR)
    }

    @Test
    fun `findCity(String) - Not available city with the given name - Returns error`() = runBlocking {
        // WHEN
        val result = repository.findCity("a")

        // THEN
        result.expectStatuses(LOADING, ERROR)
    }

    @Test
    fun `getWeather(Int) - Happy path`() = runBlocking {
        // GIVEN
        val someCity = pickSomeCityFromDatabase()

        // WHEN
        val weather = repository.getWeather(someCity.cityId)

        // THEN
        val weatherInDb = weatherDao.getWeather(someCity.cityId).getOrAwaitValueTest()
        assertThat(weather.getOrAwaitValueTest()).isEqualTo(weatherInDb)
    }

    // TODO
    @Test
    fun `fetchWeather(Int) - Happy path`() {
        /*
        throws kotlinx.coroutines.test.UncompletedCoroutinesError:
        Unfinished coroutines during teardown.
        Ensure all coroutines are completed or cancelled by your test.
        */
//        val weather = repository.fetchWeather(diffCity.id).getOrAwaitValueTest()
//        assertThat(weather).isEqualTo(weatherDao.getWeather(diffCity.id).getOrAwaitValueTest())
    }

    @Test
    fun `setInt(String, Int) - Happy path`() {
        // WHEN
        repository.setInt("a", 1)

        // THEN
        val value = repository.getInt("a")
        assertThat(value).isEqualTo(1)
    }

    @Test
    fun `getInt(String) - Happy path`() {
        // GIVEN
        repository.setInt("a", 1)

        // WHEN
        val value = repository.getInt("a")

        // THEN
        assertThat(value).isEqualTo(1)
    }

    @Test
    fun `getInt(String) - Default value`() {
        // WHEN
        val value = repository.getInt("a", 0)

        // THEN
        assertThat(value).isEqualTo(0)
    }

    @Test
    fun `refreshWeather(Int) - Happy path`() = runBlocking {
        // GIVEN
        val someCity = pickSomeCityFromDatabase()

        // Update city's weather in the imaginary server
        val updatedWeather = RemoteWeather(
                "updated main", "u desc", "u id",
                1f, 1, 1, 1, 1,
                1, 1, 1.0, 1.0,
                Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()
        )
        service.updateWeather(someCity.cityId, updatedWeather)

        // WHEN
        val result = repository.refresh()

        // THEN
        result.expectStatuses(LOADING, SUCCESS)

        val weatherInDb = weatherDao.getWeather(someCity.cityId).getOrAwaitValueTest()
        assertThat(weatherInDb).isEqualTo(
                updatedWeather.toLocalWeather(someCity.cityId)
        )
    }

    @Test
    fun `refreshWeather(Int) - Network error - Returns error`() = runBlocking {
        // GIVEN
        service.shouldReturnNetworkError = true
        val someCity = pickSomeCityFromDatabase()

        // Update its weather in the imaginary server
        val updatedWeather = RemoteWeather(
                "updated main", "u desc", "u id",
                1f, 1, 1, 1, 1,
                1, 1, 1.0, 1.0,
                Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()
        )
        service.updateWeather(someCity.cityId, updatedWeather)

        // WHEN
        val result = repository.refresh()

        // THEN
        result.expectStatuses(LOADING, ERROR)
    }

}