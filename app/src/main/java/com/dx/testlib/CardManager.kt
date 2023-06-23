package com.dx.testlib

import com.nxp.nfclib.KeyType
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings
import com.nxp.nfclib.desfire.IDESFireEV1
import com.nxp.nfclib.utils.Utilities
import dx.android.common.logger.Log

class CardManager(private val desFireEV1: IDESFireEV1) {
    fun performInitialChecks() {
        // Perform initial checks and authentication
        val getVersion = desFireEV1.version

        if (getVersion[0] != 0x04.toByte()) {
            Log.i(MainActivity.TAG, "not NXP")
        }

        if (getVersion[6] == 0x05.toByte()) {
            Log.i(MainActivity.TAG, "ISO/IEC 14443–4")
        } else {
            Log.i(MainActivity.TAG, "unknown protocol")
        }

        selectApplicationByIndex(0)
        authenticateToApplication()
    }



    fun selectApplicationByAID(appAID: ByteArray) {
        desFireEV1.selectApplication(appAID)
    }

    fun selectApplicationByIndex(appIndex: Int = 0) {
        desFireEV1.selectApplication(appIndex)
    }

    fun authenticateToApplication(ACCESS_KEY : Int = 0) {
        // Authenticate to the application
        desFireEV1.authenticate(
            ACCESS_KEY,
            IDESFireEV1.AuthType.Native,
            KeyType.THREEDES,
            MainActivity.objKEY_2TDEA
        )
    }

    fun checkApplicationExists(appAID: ByteArray): Boolean {
        // Check if the application already exists
        val appIds = desFireEV1.applicationIDs
        for (appId in appIds) {
            val ids: ByteArray = Utilities.intToBytes(appId, 3)
            val str: String = Utilities.byteToHexString(ids)
            if (str.equals(Utilities.byteToHexString(appAID), ignoreCase = true)) {
                return true // Application with targetAID exists
            }
        }
        return false // Application with targetAID does not exist
    }

    fun createApplication(appAID: ByteArray) {
        // Create the application
        val appSetting = EV1ApplicationKeySettings.Builder()
            .setMaxNumberOfApplicationKeys(10)
            .setAppKeySettingsChangeable(true)
            .setAuthenticationRequiredForDirectoryConfigurationData(false)
            .setAuthenticationRequiredForFileManagement(false)
            .setAppMasterKeyChangeable(true)
            .build()

        desFireEV1.createApplication(appAID, appSetting)
    }
}
