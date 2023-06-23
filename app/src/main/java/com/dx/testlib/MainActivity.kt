package com.dx.testlib

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nxp.nfclib.CardType
import com.nxp.nfclib.NxpNfcLib
import com.nxp.nfclib.defaultimpl.KeyData
import com.nxp.nfclib.desfire.DESFireFactory
import com.nxp.nfclib.exceptions.NxpNfcLibException
import com.nxp.nfclib.interfaces.IKeyData
import com.nxp.nfclib.utils.Utilities
import dx.android.common.logger.Log
import dx.android.common.logger.LogFragment
import dx.android.common.logger.LogWrapper
import dx.android.common.logger.MessageOnlyLogFilter
import java.security.Key
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {

    lateinit var nfc: NfcAdapter
    var nfcPendingIntent: PendingIntent? = null

    private lateinit var viewModel: AppViewModel

    private lateinit var libInstance: NxpNfcLib

    companion object {
        const val licenseKey = "f00ce3219672be96dc487e971d62ff2f"
        var objKEY_2TDEA: IKeyData? = null
        val KEY_2TDEA = Utilities.stringToBytes("00000000000000000000000000000000")
        const val timeOut = 2000L
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfc = NfcAdapter.getDefaultAdapter(applicationContext)
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
            CardType.DESFireEV1 -> {
                val desFireEV1 = DESFireFactory.getInstance().getDESFire(libInstance.customModules)
                try {
                    desFireEV1.reader.connect()
                    desFireEV1.reader.timeout = timeOut

                    // Initialize ViewModel
                    val cardManager = CardManager(desFireEV1)
                    val dataFileManager = DataFileManager(desFireEV1, cardManager)
                    viewModel = AppViewModel(cardManager, dataFileManager)

                    // Call the methods from the ViewModel based on the file type
                    viewModel.createStandardDataFile()
                    viewModel.createValueFileAndCreditValue(10)
                    viewModel.createLinearRecordFileAndWriteRecord()
                    viewModel.createCyclicRecordFileAndWriteRecord()

                    viewModel.readValueFile()
                    viewModel.readLinearFileRecords()
                    viewModel.readCyclicFileRecords()
                } catch (t: Throwable) {
                    Log.e(TAG, t.message)
                }
            }

            CardType.DESFireEV2 -> {
                TODO("IMPLEMENTATION OF EV2")
            }

            else -> {
                Log.i(TAG, "${type.tagName} not implemented")
            }
        }
    }
}
