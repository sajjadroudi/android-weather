package ir.roudi.weather.data.local.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import ir.roudi.weather.AndroidTestUtil
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.getOrAwaitValueAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
@ExperimentalCoroutinesApi
class WeatherDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase

    private lateinit var dao: WeatherDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.weatherDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getWeatherByCityId() = runBlockingTest {
        // GIVEN
        val weather = AndroidTestUtil.generateOneWeather()
            .also { dao.insert(it) }

        // WHEN
        val weatherInDb = dao.getWeather(weather.cityId)

        // THEN
        assertThat(weatherInDb.getOrAwaitValueAndroidTest()).isEqualTo(weather)
    }

    @Test
    fun insertOneWeatherItem() = runBlockingTest {
        // GIVEN
        val weather = AndroidTestUtil.generateOneWeather()

        // WHEN
        dao.insert(weather)

        // THEN
        val weatherInDb = dao.getWeather(weather.cityId).getOrAwaitValueAndroidTest()
        assertThat(weatherInDb).isEqualTo(weather)
    }

    @Test
    fun insertManyWeatherItems() = runBlockingTest {
        // GIVEN
        val weathers = AndroidTestUtil.generateWeather(3)

        // WHEN
        dao.insert(weathers)

        // THEN
        val weathersInDb = weathers.map { weather ->
            dao.getWeather(weather.cityId).getOrAwaitValueAndroidTest()
        }
        assertThat(weathersInDb).isEqualTo(weathers)
    }
}