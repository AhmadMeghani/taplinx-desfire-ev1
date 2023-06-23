package com.dx.testlib

import com.dx.testlib.MainActivity.Companion.TAG
import com.nxp.nfclib.desfire.DESFireFile
import com.nxp.nfclib.desfire.IDESFireEV1
import dx.android.common.logger.Log

class DataFileManager(private val desFireEV1: IDESFireEV1, private val cardManager: CardManager) {

    private val READ_ACCESS_KEY = 0x00
    private val WRITE_ACCESS_KEY = 0x01
    private val READ_WRITE_ACCESS_KEY = 0x02
    private val CHANGE_ACCESS_KEY = 0x03


    private val STD_FILE_NO = 1
    private val VALUE_FILE_NO = 2
    private val LINEAR_RECORD_FILE_NO = 3
    private val CIRCULAR_RECORD_FILE_NO = 4

    fun createStandardDataFile() {
        // Create a standard data file
        if (!checkFileExists(STD_FILE_NO)) {
            val fileSize = 64
            desFireEV1.createFile(
                STD_FILE_NO, DESFireFile.StdDataFileSettings(
                    IDESFireEV1.CommunicationType.Enciphered,
                    READ_ACCESS_KEY.toByte(),
                    WRITE_ACCESS_KEY.toByte(),
                    READ_WRITE_ACCESS_KEY.toByte(),
                    CHANGE_ACCESS_KEY.toByte(),
                    fileSize
                )
            )
        }
    }

    private fun checkFileExists(fileNo: Int): Boolean {
        val fileIdsByteArray = desFireEV1.fileIDs
        val fileIds = fileIdsByteArray.map { it.toInt() }

        return fileIds.contains(fileNo)
    }

    fun writeDataToStandardFile() {
        // Write data to the standard data file
        val fileOffset = 0
        val content = byteArrayOf(0xFA.toByte(), 0xCE.toByte(), 0xBA.toByte(), 0xBE.toByte())


//             Authenticate again to acquire write access permission
        cardManager.authenticateToApplication(WRITE_ACCESS_KEY)

        desFireEV1.writeData(STD_FILE_NO, fileOffset, content)
    }

    fun createValueFile() {
        // Create a value file
        if (!checkFileExists(VALUE_FILE_NO)) {
            val lowerLimit = 0
            val upperLimit = 100
            val value = 50
            val limitedCredit = true

            val fileSettings = DESFireFile.ValueFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                READ_ACCESS_KEY.toByte(),
                WRITE_ACCESS_KEY.toByte(),
                READ_WRITE_ACCESS_KEY.toByte(),
                CHANGE_ACCESS_KEY.toByte(),
                lowerLimit,
                upperLimit,
                value,
                limitedCredit,
                true
            )

            desFireEV1.createFile(VALUE_FILE_NO, fileSettings)
        }
    }

    fun creditValue(value: Int) {
        // Credit the value in the value file

        // Authenticate again to acquire write access permission
        cardManager.authenticateToApplication(READ_WRITE_ACCESS_KEY)
        desFireEV1.credit(VALUE_FILE_NO, value, IDESFireEV1.CommunicationType.Enciphered)

        desFireEV1.commitTransaction()
    }

    fun debitValue(value: Int) {
        // Credit the value in the value file

        // Authenticate again to acquire write access permission
        cardManager.authenticateToApplication(READ_WRITE_ACCESS_KEY)
        desFireEV1.debit(VALUE_FILE_NO, value)

        desFireEV1.commitTransaction()
    }

    fun createLinearRecordFile() {
        // Create a linear record file
        if (!checkFileExists(LINEAR_RECORD_FILE_NO)) {
            val recordSize = 16
            val maxRecords = 10

            val fileSettings = DESFireFile.LinearRecordFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                READ_ACCESS_KEY.toByte(),
                WRITE_ACCESS_KEY.toByte(),
                READ_WRITE_ACCESS_KEY.toByte(),
                CHANGE_ACCESS_KEY.toByte(),
                recordSize,
                maxRecords,
                0, // Set the initial number of records to 0
            )

            desFireEV1.createFile(LINEAR_RECORD_FILE_NO, fileSettings)
        }
    }

    fun writeRecordToFile() {
        // Write a record to the linear record file
        // Authenticate again to acquire write access permission
        cardManager.authenticateToApplication(READ_WRITE_ACCESS_KEY)

        val offsetInRecord = 0
        val data = byteArrayOf(0x01, 0x02, 0x03)
        desFireEV1.writeRecord(
            LINEAR_RECORD_FILE_NO, offsetInRecord, data,
            IDESFireEV1.CommunicationType.Enciphered
        )


        desFireEV1.commitTransaction()
    }

    fun createCyclicRecordFile() {
        // Create a cyclic record file
        if (!checkFileExists(CIRCULAR_RECORD_FILE_NO)) {
            val recordSize = 16
            val maxRecords = 10

            val fileSettings = DESFireFile.CyclicRecordFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                READ_ACCESS_KEY.toByte(),
                WRITE_ACCESS_KEY.toByte(),
                READ_WRITE_ACCESS_KEY.toByte(),
                CHANGE_ACCESS_KEY.toByte(),
                recordSize,
                maxRecords,
                0, // Set the initial number of records to 0
            )

            desFireEV1.createFile(CIRCULAR_RECORD_FILE_NO, fileSettings)
            Log.i(
                TAG,
                "Cyclic record file with file number $CIRCULAR_RECORD_FILE_NO created successfully."
            )
        } else {
            Log.i(
                TAG,
                "Cyclic record file with file number $CIRCULAR_RECORD_FILE_NO already exists. Skipping creation."
            )
        }
    }

    fun writeCyclicRecordToFile() {
        // Write a record to the cyclic record file

        // Authenticate again to acquire write access permission
        cardManager.authenticateToApplication(WRITE_ACCESS_KEY)

        val offsetInRecord = 0
        val data = byteArrayOf(0x01, 0x02, 0x03)
        desFireEV1.writeRecord(
            CIRCULAR_RECORD_FILE_NO, offsetInRecord, data,
            IDESFireEV1.CommunicationType.Enciphered
        )


        desFireEV1.commitTransaction()
    }

    fun readValueFile() {
        // Authenticate again to acquire read access permission
        cardManager.authenticateToApplication(READ_ACCESS_KEY)

        val value = desFireEV1.getValue(VALUE_FILE_NO)

        Log.i(TAG, "Value File No: $LINEAR_RECORD_FILE_NO: Current value is $value")
    }

    fun readLinearRecordFile(recordSize: Int) {
        // Authenticate again to acquire read access permission
        cardManager.authenticateToApplication(READ_ACCESS_KEY)

        val offsetRecords = 0
        val noOfRecords = 1 // Number of records to read

        val fileData = desFireEV1.readRecords(
            LINEAR_RECORD_FILE_NO,
            offsetRecords,
            noOfRecords
        )

        Log.i(TAG, "Linear Record File No: $LINEAR_RECORD_FILE_NO")
        for (i in 0 until fileData.size / recordSize) {
            val startIndex = i * recordSize
            val endIndex = startIndex + recordSize
            val record = fileData.copyOfRange(startIndex, endIndex)
            val recordAsString = record.joinToString(", ") { byte -> byte.toInt().toString() }
            Log.i(TAG, "Record $i: $recordAsString")
        }
    }

    fun readCyclicRecordFile(recordSize: Int) {
        // Authenticate again to acquire read access permission
        cardManager.authenticateToApplication(READ_ACCESS_KEY)

        val offsetRecords = 0
        val noOfRecords = 1 // Number of records to read

        val fileData = desFireEV1.readRecords(CIRCULAR_RECORD_FILE_NO, offsetRecords, noOfRecords)

        Log.i(TAG, "Cyclic Record File No: $CIRCULAR_RECORD_FILE_NO")
        for (i in 0 until fileData.size / recordSize) {
            val startIndex = i * recordSize
            val endIndex = startIndex + recordSize
            val record = fileData.copyOfRange(startIndex, endIndex)
            val recordAsString = record.joinToString(", ") { byte -> byte.toInt().toString() }
            Log.i(TAG, "Record $i: $recordAsString")
        }
    }
}
