package com.dx.testlib.viewmodel

import androidx.lifecycle.ViewModel
import com.dx.testlib.manager.CardManager
import com.dx.testlib.manager.DataFileManager
import com.dx.testlib.view.MainActivity.Companion.TAG
import dx.android.common.logger.Log

class AppViewModel(
    private val cardManager: CardManager,
    private val dataFileManager: DataFileManager
) : ViewModel() {

    private val appAID = byteArrayOf(0x06, 0x06, 0x06)

    fun createStandardDataFile() {
        try {
            performInitialOperations()

            dataFileManager.createStandardDataFile()
//            dataFileManager.writeDataToStandardFile()

            Log.i(TAG, "Success creating and writing to Standard Data File")
        } catch (e: Exception) {
            Log.i(TAG, e.message)
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
            Log.i(TAG, e.message)
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
            Log.i(TAG, e.message)
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
            Log.i(TAG, e.message)
            // Handle error
        }
    }


    fun readCyclicFileRecords(fileNo: Int, recordSize: Int) {
        try {
            performInitialOperations()

            dataFileManager.readCyclicRecordFile(fileNo, recordSize)

        } catch (e: Exception) {
            Log.i(TAG, e.message)
            // Handle error
        }
    }

    fun readLinearFileRecords(fileNo: Int, recordSize: Int) {
        try {
            performInitialOperations()

            dataFileManager.readLinearRecordFile(fileNo, recordSize)
        } catch (e: Exception) {
            Log.i(TAG, e.message)
            // Handle error
        }
    }

    fun readStandardFile(fileNo: Int) {
        try {
            performInitialOperations()

            dataFileManager.readStandardFile(fileNo)
        } catch (e: Exception) {
            Log.i(TAG, e.message)
            // Handle error
        }
    }

    fun readValueFile(fileNo: Int) {
        try {
            dataFileManager.readValueFile(fileNo)
        } catch (e: Exception) {
            Log.i(TAG, e.message)
        }
    }

    private fun performInitialOperations() {
        cardManager.performInitialChecks()

        if (!cardManager.checkApplicationExists(appAID))
            cardManager.createApplication(appAID)

        cardManager.selectApplicationByAID(appAID)
        cardManager.authenticateToApplication()
    }

    fun readAllFiles() {
        performInitialOperations()
        val allFiles = dataFileManager.getAllFiles()
        for(file in allFiles) {
            when(file) {
                1,2,4,5 -> readStandardFile(file)
                3 -> readValueFile(file)
                6,7,9 -> readLinearFileRecords(file, 256)
                8 -> readCyclicFileRecords(file, 280)
            }
        }

    }
}
