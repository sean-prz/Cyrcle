package com.github.se.cyrcle.model.parking.offline

import com.github.se.cyrcle.model.parking.ImageReport
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.Tile
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineParkingRepositoryRoom @Inject constructor(parkingDatabase: ParkingDatabase) :
    OfflineParkingRepository {

  private val parkingManager: ParkingDao = parkingDatabase.parkingDao

  private val coroutine = CoroutineScope(Dispatchers.IO)

  override fun downloadParkings(parkings: List<Parking>, onComplete: () -> Unit) {
    coroutine.launch {
      parkingManager.upsertAll(parkings)
      onComplete()
    }
  }

  override fun deleteTiles(tiles: Set<Tile>, onComplete: () -> Unit) {
    coroutine.launch {
      parkingManager.deleteAllInTiles(tiles)
      onComplete()
    }
  }

  override fun getNewUid(): String {
    throw UnSupportedOperationException("Get new UID called on offline repository")
  }

  override fun onSignIn(onSuccess: () -> Unit) {
    // Do nothing
    onSuccess()
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    coroutine.launch {
      parkingManager.getParking(id)?.let { onSuccess(it) }
          ?: onFailure(Exception("Parking not found"))
    }
  }

  override fun getParkingsForTile(
      tile: Tile,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    coroutine.launch { onSuccess(parkingManager.getParkingsInTile(tile)) }
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    onFailure(Exception("Add parking called on offline repository"))
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(Exception("Update parking called on offline repository"))
  }

  override fun getReportsForParking(
      parkingId: String,
      onSuccess: (List<ParkingReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Get reports for parking called on offline repository"))
  }

  override fun getReportsForImage(
      parkingId: String,
      imageId: String,
      onSuccess: (List<ImageReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Get reports for parking called on offline repository"))
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    coroutine.launch {
      parkingManager.delete(id)
      onSuccess()
    }
  }

  override fun getParkingsByListOfIds(
      ids: List<String>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    coroutine.launch { onSuccess(parkingManager.getParkings(ids)) }
  }

  override fun addReport(
      report: ParkingReport,
      onSuccess: (ParkingReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Add report called on offline repository"))
  }

  override fun addImageReport(
      report: ImageReport,
      parking: String,
      onSuccess: (ImageReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Add report called on offline repository"))
  }
}
