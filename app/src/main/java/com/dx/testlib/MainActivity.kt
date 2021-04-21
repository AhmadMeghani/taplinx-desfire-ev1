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
import com.nxp.nfclib.desfire.DESFireFactory
import com.nxp.nfclib.desfire.DESFireFile.StdDataFileSettings
import com.nxp.nfclib.desfire.EV2ApplicationKeySettings
import com.nxp.nfclib.desfire.IDESFireEV1
import com.nxp.nfclib.desfire.IDESFireEV2
import com.nxp.nfclib.exceptions.NxpNfcLibException
import com.nxp.nfclib.interfaces.IKeyData
import com.nxp.nfclib.utils.Utilities

class MainActivity : AppCompatActivity() {

    lateinit var nfc: NfcAdapter
    var nfcPendingIntent: PendingIntent? = null

    lateinit private var libInstance: NxpNfcLib

    companion object {
        const val licenseKey = "f00ce3219672be96dc487e971d62ff2f"
        var objKEY_2KTDES: IKeyData? = null
        val KEY_2KTDES = byteArrayOf(
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        val timeOut = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfc = NfcAdapter.getDefaultAdapter(getApplicationContext())
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        initializeLibrary()

        val keyDataObj = KeyData()
        var k: Key = SecretKeySpec(KEY_2KTDES, "DESede")
        keyDataObj.key = k
        objKEY_2KTDES = keyDataObj
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
            print(ex.message)
        } catch (e: Exception) {
            print(e.message)
        }
    }

    private fun checkCard(intent: Intent) {
        val type = libInstance.getCardType(intent) //Get the type of the card

        if (type == CardType.UnknownCard) {
            print("Unknown card type")
        }

        when (type) {
            CardType.DESFireEV2 -> {
                val desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(libInstance.customModules)
                if (desFireEV2.subType == IDESFireEV2.SubType.MIFAREIdentity) {
                    val mfID = DESFireFactory.getInstance().getMIFAREIdentity(libInstance.customModules)
                    val fciData = mfID.selectMIFAREIdentityAppAndReturnFCI()
                } else {
                    try {
                        desFireEV2.reader.connect()
                        writeToCard(desFireEV2)
                        print("DESFire card detected")
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        print("Unknown Error Tap Again")
                    }
                }
            }
            else -> {
                print("Not a DESFire card")
            }
        }
    }

    private fun writeToCard(desFireEV2: IDESFireEV2) {

        val tagname = desFireEV2.type.tagName
        val tagUID = desFireEV2.uid
        val totalMem = desFireEV2.totalMemory
        val freeMem = desFireEV2.freeMemory

        try {
            desFireEV2.reader.timeout = timeOut.toLong()
            val getVersion = desFireEV2.version

            if (getVersion[0] == 0x04.toByte()) {
                println("NXP")
            } else {
                println("not NXP")
            }

            if (getVersion[6] == 0x05.toByte()) {
                println("ISO/IEC 14443–4")
            } else {
                println("unknown")
            }

            desFireEV2.selectApplication(0)
            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES)

            val app_Ids = desFireEV2.applicationIDs
            for (app_id in app_Ids) {
                val ids: ByteArray = Utilities.intToBytes(app_id, 3)
                val str: String = Utilities.byteToHexString(ids)
                println("AID: " + str)
            }

            // create new application
            val appAID = byteArrayOf(0x05, 0x05, 0x05)
            val ks = byteArrayOf(0x0F, 0x0E)
            val appKs = EV2ApplicationKeySettings(ks)
            desFireEV2.createApplication(appAID, appKs)

            // select an application and authenticate to it
            desFireEV2.selectApplication(appAID)
            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES)

            // create a standard file of 64 bytes size under the application
            val fileSize = 64
            val fileNo = 0
            desFireEV2.createFile(fileNo, StdDataFileSettings(
                    IDESFireEV1.CommunicationType.Enciphered,
                    0x2.toByte(), 0x1.toByte(), 0.toByte(), 0.toByte(), fileSize))

        } catch (e: java.lang.Exception) {
            println("Unable to read")
        }
    }

}