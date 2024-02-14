package com.dx.testlib.manager

import com.dx.testlib.view.MainActivity
import com.nxp.nfclib.KeyType
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings
import com.nxp.nfclib.desfire.IDESFireEV1
import com.nxp.nfclib.utils.Utilities
import dx.android.common.logger.Log

/**
 * This class manages the operations related to the DESFire card.
 *
 * @param desFireEV1 The DESFire EV1 instance.
 */
class CardManager(private val desFireEV1: IDESFireEV1) {

    /**
     * Performs initial checks and authentication for the card.
     */
    fun performInitialChecks() {
        val version = desFireEV1.version

        if (version[0] != 0x04.toByte()) {
            Log.i(MainActivity.TAG, "Card is not NXP")
        }

        if (version[6] == 0x05.toByte()) {
//            Log.i(MainActivity.TAG, "Card uses ISO/IEC 14443–4 protocol")
        } else {
            Log.i(MainActivity.TAG, "Unknown card protocol")
        }

        selectApplicationByIndex(0)
        authenticateToMasterApplication()
    }

    /**
     * Selects the application by its AID (Application ID).
     *
     * @param appAID The application AID.
     */
    fun selectApplicationByAID(appAID: ByteArray) {
        desFireEV1.selectApplication(appAID)
    }

    /**
     * Selects the application by its index.
     *
     * @param appIndex The application index.
     */
    private fun selectApplicationByIndex(appIndex: Int = 0) {
        desFireEV1.selectApplication(appIndex)
    }

    /**
     * Authenticates to the application using the specified access key.
     *
     * @param ACCESS_KEY The access key to authenticate with.
     */
    fun authenticateToApplication(ACCESS_KEY: Byte = 0) {
        desFireEV1.authenticate(
            ACCESS_KEY.toInt(),
            IDESFireEV1.AuthType.Native,
            KeyType.THREEDES,
            MainActivity.objKEY_2TDEA
        )
    }
    fun authenticateToMasterApplication(ACCESS_KEY: Byte = 0) {
        desFireEV1.authenticate(
            ACCESS_KEY.toInt(),
            IDESFireEV1.AuthType.Native,
            KeyType.THREEDES,
            MainActivity.objKEY_2TDEA_MASTER
        )
    }

    /**
     * Checks if the application with the specified AID exists.
     *
     * @param appAID The application AID to check.
     * @return True if the application exists, false otherwise.
     */
    fun checkApplicationExists(appAID: ByteArray): Boolean {
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

    /**
     * Creates a new application with the specified AID (Application ID).
     *
     * @param appAID The application AID.
     */
    fun createApplication(appAID: ByteArray) {
        val appSetting = EV1ApplicationKeySettings.Builder()
            .setMaxNumberOfApplicationKeys(10)
            .setAppKeySettingsChangeable(false)
            .setAuthenticationRequiredForDirectoryConfigurationData(true)
            .setAuthenticationRequiredForFileManagement(true)
            .setAppMasterKeyChangeable(false)
            .build()

        desFireEV1.createApplication(appAID, appSetting)
    }
}
