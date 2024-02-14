package com.dx.testlib.manager

import com.dx.testlib.view.MainActivity
import com.nxp.nfclib.desfire.DESFireFile
import com.nxp.nfclib.desfire.IDESFireEV1
import dx.android.common.logger.Log

/**
 * The [DataFileManager] class provides methods to manage data files on a DESFireEV1 card.
 *
 * @property desFireEV1 The DESFireEV1 instance.
 * @property cardManager The CardManager instance.
 */
class DataFileManager(
    private val desFireEV1: IDESFireEV1,
    private val cardManager: CardManager
) {

    private companion object {
        private const val TAG = "DataFileManager"

        val std:Int = 1
        val value:Int = 2
        val l:Int = 3
        val c:Int = 4
        private val STD_READ_ACCESS_KEY = std.toByte()
        private val STD_WRITE_ACCESS_KEY = std.toByte()
        private val STD_READ_WRITE_ACCESS_KEY = std.toByte()
        private val STD_CHANGE_ACCESS_KEY = std.toByte()

        private val VALUE_READ_ACCESS_KEY = value.toByte()
        private val VALUE_WRITE_ACCESS_KEY = value.toByte()
        private val VALUE_READ_WRITE_ACCESS_KEY = value.toByte()
        private val VALUE_CHANGE_ACCESS_KEY = value.toByte()

        private val L_READ_ACCESS_KEY = l.toByte()
        private val L_WRITE_ACCESS_KEY = l.toByte()
        private val L_READ_WRITE_ACCESS_KEY = l.toByte()
        private val L_CHANGE_ACCESS_KEY = l.toByte()

        private val C_READ_ACCESS_KEY = c.toByte()
        private val C_WRITE_ACCESS_KEY = c.toByte()
        private val C_READ_WRITE_ACCESS_KEY = c.toByte()
        private val C_CHANGE_ACCESS_KEY = c.toByte()

        private const val STD_FILE_NO = 1
        private const val VALUE_FILE_NO = 2
        private const val LINEAR_RECORD_FILE_NO = 3
        private const val CIRCULAR_RECORD_FILE_NO = 4
    }

    /**
     * Creates a standard data file on the card.
     */
    fun createStandardDataFile() {
        if (!checkFileExists(STD_FILE_NO)) {
            val fileSize = 1024
            val fileSettings = DESFireFile.StdDataFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                STD_READ_ACCESS_KEY,
                STD_WRITE_ACCESS_KEY,
                STD_READ_WRITE_ACCESS_KEY,
                STD_CHANGE_ACCESS_KEY,
                fileSize
            )
            desFireEV1.createFile(STD_FILE_NO, fileSettings)
        }
    }

    /**
     * Checks if a file with the given file number exists on the card.
     *
     * @param fileNo The file number to check.
     * @return `true` if the file exists, `false` otherwise.
     */
    private fun checkFileExists(fileNo: Int): Boolean {
        val fileIdsByteArray = desFireEV1.fileIDs
        val fileIds = fileIdsByteArray.map { it.toInt() }
        return fileIds.contains(fileNo)
    }

    /**
     * Writes data to the standard data file on the card.
     */
    fun writeDataToStandardFile() {
        cardManager.authenticateToApplication(STD_WRITE_ACCESS_KEY)
        val fileOffset = 0
        val content = ("Hello, my name is Ahmad. Have a nice day :)").toByteArray()
//        val content = byteArrayOf(0xFA.toByte(), 0xCE.toByte(), 0xBA.toByte(), 0xBE.toByte())
        desFireEV1.writeData(STD_FILE_NO, fileOffset, content)
        desFireEV1.commitTransaction()
    }

    /**
     * Creates a value file on the card.
     */
    fun createValueFile() {
        if (!checkFileExists(VALUE_FILE_NO)) {
            val lowerLimit = 0
            val upperLimit = 100
            val value = 50
            val limitedCredit = true
            val fileSettings = DESFireFile.ValueFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                VALUE_READ_ACCESS_KEY,
                VALUE_WRITE_ACCESS_KEY,
                VALUE_READ_WRITE_ACCESS_KEY,
                VALUE_CHANGE_ACCESS_KEY,
                lowerLimit,
                upperLimit,
                value,
                limitedCredit,
                true
            )
            desFireEV1.createFile(VALUE_FILE_NO, fileSettings)
        }
    }

    /**
     * Credits the value in the value file on the card.
     *
     * @param value The value to credit.
     */
    fun creditValue(value: Int) {
        cardManager.authenticateToApplication(VALUE_READ_WRITE_ACCESS_KEY)
        desFireEV1.credit(VALUE_FILE_NO, value, IDESFireEV1.CommunicationType.Enciphered)
        desFireEV1.commitTransaction()
    }

    /**
     * Debits the value from the value file on the card.
     *
     * @param value The value to debit.
     */
    fun debitValue(value: Int) {
        cardManager.authenticateToApplication(VALUE_READ_WRITE_ACCESS_KEY)
        desFireEV1.debit(VALUE_FILE_NO, value)
        desFireEV1.commitTransaction()
    }

    /**
     * Creates a linear record file on the card.
     */
    fun createLinearRecordFile() {
        if (!checkFileExists(LINEAR_RECORD_FILE_NO)) {
            val recordSize = 16
            val maxRecords = 10
            val fileSettings = DESFireFile.LinearRecordFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                L_READ_ACCESS_KEY,
                L_WRITE_ACCESS_KEY,
                L_READ_WRITE_ACCESS_KEY,
                L_CHANGE_ACCESS_KEY,
                recordSize,
                maxRecords,
                0, // Set the initial number of records to 0
            )
            desFireEV1.createFile(LINEAR_RECORD_FILE_NO, fileSettings)
        }
    }

    /**
     * Writes a record to the linear record file on the card.
     */
    fun writeRecordToFile() {
        cardManager.authenticateToApplication(L_READ_WRITE_ACCESS_KEY)
        val offsetInRecord = 0
//        val data = byteArrayOf(0x01, 0x02, 0x03)

        val data = ("Hello").toByteArray()
        desFireEV1.writeRecord(
            LINEAR_RECORD_FILE_NO,
            offsetInRecord,
            data,
            IDESFireEV1.CommunicationType.Enciphered
        )
        desFireEV1.commitTransaction()
    }

    /**
     * Creates a cyclic record file on the card.
     */
    fun createCyclicRecordFile() {
        if (!checkFileExists(CIRCULAR_RECORD_FILE_NO)) {
            val recordSize = 256
            val maxRecords = 10
            val fileSettings = DESFireFile.CyclicRecordFileSettings(
                IDESFireEV1.CommunicationType.Enciphered,
                C_READ_ACCESS_KEY,
                C_WRITE_ACCESS_KEY,
                C_READ_WRITE_ACCESS_KEY,
                C_CHANGE_ACCESS_KEY,
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

    /**
     * Writes a record to the cyclic record file on the card.
     */
    fun writeCyclicRecordToFile() {
        cardManager.authenticateToApplication(C_WRITE_ACCESS_KEY)
        val offsetInRecord = 0
//        val data = byteArrayOf(0x01, 0x02, 0x03)
        val data = ("Hi Cyclic Record").toByteArray()
        desFireEV1.writeRecord(
            CIRCULAR_RECORD_FILE_NO,
            offsetInRecord,
            data,
            IDESFireEV1.CommunicationType.Enciphered
        )
        desFireEV1.commitTransaction()
    }

    /**
     * Reads the value from the value file on the card.
     */
    fun readValueFile(fileNo: Int) {
        cardManager.authenticateToApplication(VALUE_READ_ACCESS_KEY)
        val value = desFireEV1.getValue(fileNo)
        Log.i(TAG, "Value File No: $fileNo: Current value is $value")
    }

    /**
     * Reads the content from the standard file on the card.
     */
    fun readStandardFile(fileNo: Int) {
        cardManager.authenticateToApplication(STD_READ_ACCESS_KEY)
        val content = desFireEV1.readData(fileNo, 0 , 0)
        Log.i(TAG, "Standard File No: $fileNo: Content is ${String(content)}")
    }

    /**
     * Reads records from the linear record file on the card.
     */
    fun readLinearRecordFile(fileNo: Int, recordSize: Int) {
        // Authenticate again to acquire read access permission
        cardManager.authenticateToApplication(L_READ_ACCESS_KEY)

        val offsetRecords = 0
        val noOfRecords = 0 // Number of records to read

        val fileData = desFireEV1.readRecords(
            fileNo,
            offsetRecords,
            noOfRecords
        )

        Log.i(MainActivity.TAG, "Linear Record File No: $fileNo")
        for (i in 0 until fileData.size / recordSize) {
            val startIndex = i * recordSize
            val endIndex = startIndex + recordSize
            val record = fileData.copyOfRange(startIndex, endIndex)
//            val recordAsString = record.joinToString(", ") { byte -> byte.toInt().toString() }
//            Log.i(MainActivity.TAG, "Record $i: $recordAsString")
            Log.i(MainActivity.TAG, "Record $i: Linear is ${String(record)}")
        }
    }

    /**
     * Reads records from the cyclic record file on the card.
     */
    fun readCyclicRecordFile(fileNo: Int, recordSize: Int) {
        // Authenticate again to acquire read access permission
        cardManager.authenticateToApplication(C_READ_ACCESS_KEY)

        val offsetRecords = 0
        val noOfRecords = 0 // Number of records to read

        val fileData = desFireEV1.readRecords(fileNo, offsetRecords, noOfRecords)

        Log.i(MainActivity.TAG, "Cyclic Record File No: $fileNo")
        for (i in 0 until fileData.size / recordSize) {
            val startIndex = i * recordSize
            val endIndex = startIndex + recordSize
            val record = fileData.copyOfRange(startIndex, endIndex)
//            val recordAsString = record.joinToString(", ") { byte -> byte.toInt().toString() }
//            Log.i(MainActivity.TAG, "Record $i: $recordAsString")
            Log.i(MainActivity.TAG, "Record $i: Cyclic is ${String(record)}")
        }
    }

    fun getAllFiles(): List<Int> {

        val fileIdsByteArray = desFireEV1.fileIDs

        return fileIdsByteArray.map { it.toInt() }
    }

}
