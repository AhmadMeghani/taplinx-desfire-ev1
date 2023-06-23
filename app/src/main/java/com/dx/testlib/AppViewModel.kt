package com.dx.testlib

import androidx.lifecycle.ViewModel
import com.dx.testlib.MainActivity.Companion.TAG
import com.nxp.nfclib.desfire.IDESFireEV1
import dx.android.common.logger.Log

class AppViewModel(private val cardManager: CardManager, private val dataFileManager: DataFileManager) : ViewModel() {

    private val appAID = byteArrayOf(0x06, 0x06, 0x06)

    fun createStandardDataFile() {
        try {
            performInitialOperations()

            dataFileManager.createStandardDataFile()
            dataFileManager.writeDataToStandardFile()

            Log.i(TAG, "Success creating and writing to Standard Data File")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }

    fun createValueFileAndCreditValue(value: Int) {
        try {
            performInitialOperations()

            dataFileManager.createValueFile()
            dataFileManager.creditValue(value)

            Log.i(TAG, "Success creating Value File and crediting value")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }

    fun createLinearRecordFileAndWriteRecord() {
        try {
            performInitialOperations()

            dataFileManager.createLinearRecordFile()
            dataFileManager.writeRecordToFile()

            Log.i(TAG, "Success creating Linear Record File and writing record")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }

    fun createCyclicRecordFileAndWriteRecord() {
        try {
            performInitialOperations()

            dataFileManager.createCyclicRecordFile()
            dataFileManager.writeCyclicRecordToFile()

            Log.i(TAG, "Success creating Cyclic Record File and writing record")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }


    fun readCyclicFileRecords() {
        try {
            performInitialOperations()

            dataFileManager.readCyclicRecordFile(16)

            Log.i(TAG, "Success cyclic Linear Record File record")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }

    fun readLinearFileRecords() {
        try {
            performInitialOperations()

            dataFileManager.readLinearRecordFile(16)

            Log.i(TAG, "Success reading Linear Record File record")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            // Handle error
        }
    }

    fun readValueFile() {
        try {
            dataFileManager.readValueFile()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    private fun performInitialOperations() {
        cardManager.performInitialChecks()

        if (!cardManager.checkApplicationExists(appAID))
            cardManager.createApplication(appAID)

        cardManager.selectApplicationByAID(appAID)
        cardManager.authenticateToApplication()
    }
}
