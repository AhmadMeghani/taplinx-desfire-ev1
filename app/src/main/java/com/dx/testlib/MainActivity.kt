package com.dx.testlib

import java.security.Key
import javax.crypto.spec.SecretKeySpec

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.nxp.nfclib.CardType
import com.nxp.nfclib.KeyType
import com.nxp.nfclib.NxpNfcLib
import com.nxp.nfclib.defaultimpl.KeyData
import com.nxp.nfclib.desfire.*
import com.nxp.nfclib.desfire.DESFireFile.StdDataFileSettings
import com.nxp.nfclib.exceptions.NxpNfcLibException
import com.nxp.nfclib.interfaces.IKeyData
import com.nxp.nfclib.utils.Utilities
import dx.android.common.logger.Log
import dx.android.common.logger.LogFragment
import dx.android.common.logger.LogWrapper
import dx.android.common.logger.MessageOnlyLogFilter
import java.nio.ByteBuffer
import java.util.*

data class MyFileSettings(val settings : DESFireFile.FileSettings, val len:Int) {}

fun IDESFireEV2.getFileSettings(fileNo: Byte) : MyFileSettings {
    val response = this.reader.transceive(
            byteArrayOf(0x90.toByte(),0xF5.toByte(), 0x00, 0x00, 0x01, fileNo.toByte(), 0x00))

    val sw = response.takeLast(2).toByteArray()
    if (!Arrays.equals(sw, byteArrayOf(0x91.toByte(), 0x00))) {
        throw SecurityException("Failed to get file size for file ${fileNo}")
    }

    val buf3 = response.slice(4..6).toByteArray()
    buf3.reverse()
    val buf4 = ByteArray(4)
    buf3.copyInto(buf4,1, 0, buf3.size)
    val fileSize = ByteBuffer.wrap(buf4).int
    val fs = this.getFileSettings(fileNo.toInt())
    val settings = StdDataFileSettings(
            fs.comSettings,
            fs.readAccess,
            fs.writeAccess,
            fs.readWriteAccess,
            fs.changeAccess,
            fileSize)

    return MyFileSettings(settings, fileSize)
}

class MainActivity : AppCompatActivity() {

    lateinit var nfc: NfcAdapter
    var nfcPendingIntent: PendingIntent? = null

    lateinit private var libInstance: NxpNfcLib

    companion object {
        const val licenseKey = "f00ce3219672be96dc487e971d62ff2f"
        var objKEY_2TDEA: IKeyData? = null
        val KEY_2TDEA = byteArrayOf(
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        const val timeOut = 2000L
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfc = NfcAdapter.getDefaultAdapter(getApplicationContext())
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        initializeLogging()
        initializeLibrary()
        initializeKeys()
    }

    override fun onResume() {
        super.onResume()
        nfc.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfc.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            checkCard(intent)
        }
    }

    private fun initializeLibrary() {
        libInstance = NxpNfcLib.getInstance()
        try {
            libInstance.registerActivity(this, licenseKey)
        } catch (ex: NxpNfcLibException) {
            Log.i(TAG, ex.message)
        } catch (e: Exception) {
            Log.i(TAG, e.message)
        }
    }

    private fun initializeLogging() {
        val msgFilter = MessageOnlyLogFilter()
        val logFragment = supportFragmentManager.findFragmentById(R.id.mylogfragment) as LogFragment
        msgFilter.next = logFragment.logView

        val logWrapper = LogWrapper()
        logWrapper.next = msgFilter

        Log.logNode = logWrapper
    }

    private fun initializeKeys() {
        val keyDataObj = KeyData()
        var k: Key = SecretKeySpec(KEY_2TDEA, "DESede")
        keyDataObj.key = k
        objKEY_2TDEA = keyDataObj
    }

    private fun checkCard(intent: Intent) {
        val type = libInstance.getCardType(intent) //Get the type of the card

        if (type == CardType.UnknownCard) {
            Log.i(TAG, "Unknown card type")
            return
        }

        when (type) {
            CardType.DESFireEV2 -> {
                val desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(libInstance.customModules)
                if (desFireEV2.subType == IDESFireEV2.SubType.MIFAREIdentity) {
                    val mfID = DESFireFactory.getInstance().getMIFAREIdentity(libInstance.customModules)
                    val fciData = mfID.selectMIFAREIdentityAppAndReturnFCI()
                    Log.i(TAG, "IDESFireEV2.SubType.MIFAREIdentity")
                } else {
                    try {
                        desFireEV2.reader.connect()
                        desFireEV2.reader.timeout = timeOut
                        writeToCard(desFireEV2)
                    } catch (t: Throwable) {
                        Log.e(TAG, t.message)
                    }
                }
            }
            else -> {
                Log.i(TAG, "${type.tagName} not implemented")
            }
        }
    }

    private fun writeToCard(desFireEV2: IDESFireEV2) {

        val tagname = desFireEV2.type.tagName
        val tagUID = desFireEV2.uid
        val totalMem = desFireEV2.totalMemory
        val freeMem = desFireEV2.freeMemory

        try {
            val getVersion = desFireEV2.version

            if (getVersion[0] != 0x04.toByte()) {
                Log.i(TAG, "not NXP")
            }

            if (getVersion[6] == 0x05.toByte()) {
                Log.i(TAG, "ISO/IEC 14443–4")
            } else {
                Log.i(TAG, "unknown protocol")
            }

            var keyNo = 0
            desFireEV2.selectApplication(0)
            desFireEV2.authenticate(keyNo, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2TDEA)

            val app_Ids = desFireEV2.applicationIDs
            for (app_id in app_Ids) {
                val ids: ByteArray = Utilities.intToBytes(app_id, 3)
                val str: String = Utilities.byteToHexString(ids)
                Log.i(TAG,"AID: " + str)
            }

            // create new application
            val appAID = byteArrayOf(0x05, 0x05, 0x05)

            val appSetting = EV2ApplicationKeySettings.Builder()
                    .setMaxNumberOfApplicationKeys(10)
                    .setAppKeySettingsChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setAuthenticationRequiredForFileManagement(false)
                    .setAppMasterKeyChangeable(true)
                    .build()

            desFireEV2.createApplication(appAID, appSetting)

            // select the application and authenticate to it
            desFireEV2.selectApplication(appAID)
            desFireEV2.authenticate(keyNo, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2TDEA)

            // create a standard file of 64 bytes size under the application
            val fileSize = 64
            val fileNo = 0
            val fileOffset = 0
            desFireEV2.createFile(fileNo, StdDataFileSettings(
                    IDESFireEV1.CommunicationType.Enciphered,
                    0x01, 0x02, 0x03, 0x04, fileSize))

            // authenticate again to acquire write access permission
            keyNo = 0x02
            desFireEV2.authenticate(keyNo, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2TDEA)

            val content = byteArrayOf(0xFA.toByte(), 0xCE.toByte(), 0xBA.toByte(), 0xBE.toByte())
            desFireEV2.writeData(fileNo, fileOffset, content)
            Log.i(TAG,"Success write to ${Utilities.byteToHexString(appAID)}/${fileNo}: " + Utilities.byteToHexString(content))
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message)
        }
    }

}