package dx.android.common.logger

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

/** Simple TextView which is used to output log data received through the LogNode interface.
 */
class LogView : AppCompatTextView, LogNode {
    constructor(context: Context?) : super(context!!) {
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
    }

    /**
     * Formats the log data and prints it out to the LogView.
     * @param priority Log level of the data being logged.  Verbose, Error, etc.
     * @param tag Tag for for the log data.  Can be used to organize log statements.
     * @param msg The actual message to be logged. The actual message to be logged.
     * @param tr If an exception was thrown, this can be sent along for the logging facilities
     * to extract and print useful information.
     */
    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        var priorityStr: String? = null
        when (priority) {
            Log.VERBOSE -> priorityStr = "VERBOSE"
            Log.DEBUG -> priorityStr = "DEBUG"
            Log.INFO -> priorityStr = "INFO"
            Log.WARN -> priorityStr = "WARN"
            Log.ERROR -> priorityStr = "ERROR"
            Log.ASSERT -> priorityStr = "ASSERT"
            else -> {
            }
        }

        // Handily, the Log class has a facility for converting a stack trace into a usable string.
        var exceptionStr: String? = null
        if (tr != null) {
            exceptionStr = Log.getStackTraceString(tr)
        }

        // Take the priority, tag, message, and exception, and concatenate as necessary
        // into one usable line of text.
        val outputBuilder = StringBuilder()
        val delimiter = "\t"
        appendIfNotNull(outputBuilder, priorityStr, delimiter)
        appendIfNotNull(outputBuilder, tag, delimiter)
        appendIfNotNull(outputBuilder, msg, delimiter)
        appendIfNotNull(outputBuilder, exceptionStr, delimiter)

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        (context as Activity).runOnUiThread(Thread(Runnable { // Display the text we just generated within the LogView.
            appendToLog(outputBuilder.toString())
        }))
        if (next != null) {
            next!!.println(priority, tag, msg, tr)
        }
    }

    /** Takes a string and adds to it, with a separator, if the bit to be added isn't null. Since
     * the logger takes so many arguments that might be null, this method helps cut out some of the
     * agonizing tedium of writing the same 3 lines over and over.
     * @param source StringBuilder containing the text to append to.
     * @param addStr The String to append
     * @param delimiter The String to separate the source and appended strings. A tab or comma,
     * for instance.
     * @return The fully concatenated String as a StringBuilder
     */
    private fun appendIfNotNull(source: StringBuilder, addStr: String?, delimiter: String): StringBuilder {
        var delimiter: String? = delimiter
        if (addStr != null) {
            if (addStr.length == 0) {
                delimiter = ""
            }
            return source.append(addStr).append(delimiter)
        }
        return source
    }

    // The next LogNode in the chain.
    var next: LogNode? = null

    /** Outputs the string as a new line of log data in the LogView.  */
    fun appendToLog(s: String) {
        append("""
    
    $s
    """.trimIndent())
    }
}