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
@ExperimentalCoroutinesApi
@SmallTest
class CityDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db : AppDatabase

    private lateinit var dao : CityDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cityDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllCities() = runBlockingTest {
        // GIVEN
        val list = AndroidTestUtil.generateCity(3).onEach { dao.insert(it) }

        // WHEN - get all cities from the database
        val cities = dao.getAllCities()

        // THEN - check if all cities have been returned
        assertThat(cities.getOrAwaitValueAndroidTest()).isEqualTo(list)
    }

    @Test
    fun getCityById() = runBlockingTest {
        // GIVEN
        val list = AndroidTestUtil.generateCity(3).onEach { dao.insert(it) }
        val someCity = list[0]

        // WHEN - get city with an id
        val cityInDb = dao.getCity(someCity.cityId)

        // THEN - verify the city
        assertThat(cityInDb.getOrAwaitValueAndroidTest()).isEqualTo(someCity)
    }

    @Test
    fun insertCity() = runBlockingTest {
        // GIVEN
        val city = AndroidTestUtil.generateOneCity()

        // WHEN
        dao.insert(city)

        // THEN
        val insertedCity = dao.getCity(city.cityId).getOrAwaitValueAndroidTest()
        assertThat(insertedCity).isEqualTo(city)
    }

    @Test
    fun updateCity() = runBlockingTest {
        // GIVEN - an inserted city item
        val city = AndroidTestUtil.generateOneCity().also {
            dao.insert(it)
        }

        // WHEN - update the city
        city.name = "updated name"
        dao.updateCity(city)

        // THEN - verify the update
        val updatedCity = dao.getCity(city.cityId).getOrAwaitValueAndroidTest()
        assertThat(updatedCity).isEqualTo(city)
    }

    @Test
    fun deleteCity() = runBlockingTest {
        val city = AndroidTestUtil.generateOneCity().also {
            dao.insert(it)
        }

        dao.delete(city)

        val allCities = dao.getAllCities().getOrAwaitValueAndroidTest()
        assertThat(allCities).doesNotContain(city)
    }

}