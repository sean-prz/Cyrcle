package com.github.se.cyrcle.model.parking

import com.github.se.cyrcle.model.user.TestInstancesUser
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParkingsTest {

  @Test
  fun testConstructor() {
    val point = Point.fromLngLat(6.545, 46.518)
    val location = Location(point)
    val parking =
        Parking(
            uid = "1",
            optName = "Parking",
            optDescription = null,
            location = location,
            images = listOf("image_url"),
            imageObjects = emptyList(),
            capacity = ParkingCapacity.LARGE,
            rackType = ParkingRackType.U_RACK,
            protection = ParkingProtection.COVERED,
            price = 0.0,
            hasSecurity = true,
            owner = TestInstancesUser.user1.public.userId,
            reportingUsers = emptyList(),
            reportedImages = emptyList(),
            maxNumOfImages = 0)

    assertEquals("1", parking.uid)
    assertEquals("Parking", parking.optName)
    assertNull(parking.optDescription)
    assertEquals(location, parking.location)
    assertEquals(listOf("image_url"), parking.images)
    assertEquals(ParkingCapacity.LARGE, parking.capacity)
    assertEquals(ParkingRackType.U_RACK, parking.rackType)
    assertEquals(ParkingProtection.COVERED, parking.protection)
    assertEquals(0.0, parking.price, 0.0)
    assertTrue(parking.hasSecurity)
  }

  @Test
  fun testEqualsAndHashCode() {
    val parking1 =
        Parking(
            uid = "1",
            optName = "Parking",
            optDescription = null,
            location = Location(Point.fromLngLat(6.545, 46.518)),
            images = listOf("image_url"),
            imageObjects = emptyList(),
            capacity = ParkingCapacity.LARGE,
            rackType = ParkingRackType.U_RACK,
            protection = ParkingProtection.COVERED,
            price = 0.0,
            hasSecurity = true,
            owner = TestInstancesUser.user1.public.userId,
            reportingUsers = emptyList(),
            reportedImages = emptyList(),
            maxNumOfImages = 0)

    val parking2 =
        Parking(
            uid = "1",
            optName = "Parking",
            optDescription = null,
            location = Location(Point.fromLngLat(6.545, 46.518)),
            images = listOf("image_url"),
            imageObjects = emptyList(),
            capacity = ParkingCapacity.LARGE,
            rackType = ParkingRackType.U_RACK,
            protection = ParkingProtection.COVERED,
            price = 0.0,
            hasSecurity = true,
            owner = TestInstancesUser.user1.public.userId,
            reportingUsers = emptyList(),
            reportedImages = emptyList(),
            maxNumOfImages = 0)

    assertEquals(parking1, parking2)
    assertEquals(parking1.hashCode(), parking2.hashCode())
  }
}
