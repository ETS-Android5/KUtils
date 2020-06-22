package com.kiven.sample.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.kiven.kutils.activityHelper.KActivityDebugHelper
import com.kiven.kutils.activityHelper.KHelperActivity
import com.kiven.sample.R
import org.jetbrains.anko.toast

/**
 * Created by oukobayashi on 2020/6/22.
 */
class ToyVpnClient : KActivityDebugHelper() {
    class Prefs {
        companion object {
            const val NAME = "connection"
            const val SERVER_ADDRESS = "server.address"
            const val SERVER_PORT = "server.port"
            const val SHARED_SECRET = "shared.secret"
            const val PROXY_HOSTNAME = "proxyhost"
            const val PROXY_PORT = "proxyport"
            const val ALLOW = "allow"
            const val PACKAGES = "packages"
        }
    }

    override fun onCreate(activity: KHelperActivity, savedInstanceState: Bundle?) {
        super.onCreate(activity, savedInstanceState)
        setContentView(R.layout.ah_my_vpn)

        val serverAddress = findViewById<TextView>(R.id.address)
        val serverPort = findViewById<TextView>(R.id.port)
        val sharedSecret = findViewById<TextView>(R.id.secret)
        val proxyHost = findViewById<TextView>(R.id.proxyhost)
        val proxyPort = findViewById<TextView>(R.id.proxyport)

        val allowed: RadioButton = findViewById(R.id.allowed)
        val packages = findViewById<TextView>(R.id.packages)

        val prefs = mActivity.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)
        serverAddress.text = prefs.getString(Prefs.SERVER_ADDRESS, "")
        val serverPortPrefValue = prefs.getInt(Prefs.SERVER_PORT, 0)
        serverPort.text = if (serverPortPrefValue == 0) "" else serverPortPrefValue.toString()
        sharedSecret.text = prefs.getString(Prefs.SHARED_SECRET, "")
        proxyHost.text = prefs.getString(Prefs.PROXY_HOSTNAME, "")
        val proxyPortPrefValue = prefs.getInt(Prefs.PROXY_PORT, 0)
        proxyPort.text = if (proxyPortPrefValue == 0) "" else proxyPortPrefValue.toString()

        allowed.isChecked = prefs.getBoolean(Prefs.ALLOW, true)
        packages.text = prefs.getStringSet(Prefs.PACKAGES, setOf())?.joinToString() ?: ""

        findViewById<View>(R.id.connect).setOnClickListener { v: View? ->
            if (!checkProxyConfigs(proxyHost.text.toString(),
                            proxyPort.text.toString())) {
                return@setOnClickListener
            }

            val packageSet: Set<String> = packages.text.split(",")
                    .map { obj: String -> obj.trim { it <= ' ' } }
                    .filter { s -> s.isNotEmpty() }
                    .toSet()
            if (!checkPackages(packageSet)) {
                return@setOnClickListener
            }
            val serverPortNum: Int = try {
                serverPort.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
            val proxyPortNum: Int = try {
                proxyPort.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
            prefs.edit()
                    .putString(Prefs.SERVER_ADDRESS, serverAddress.text.toString())
                    .putInt(Prefs.SERVER_PORT, serverPortNum)
                    .putString(Prefs.SHARED_SECRET, sharedSecret.text.toString())
                    .putString(Prefs.PROXY_HOSTNAME, proxyHost.text.toString())
                    .putInt(Prefs.PROXY_PORT, proxyPortNum)
                    .putBoolean(Prefs.ALLOW, allowed.isChecked())
                    .putStringSet(Prefs.PACKAGES, packageSet)
                    .apply()
            val intent = VpnService.prepare(mActivity)
            if (intent != null) {
                mActivity.startActivityForResult(intent, 988)
            } else {
                onActivityResult(0, Activity.RESULT_OK, null)
            }
        }
        findViewById<View>(R.id.disconnect).setOnClickListener { v: View? -> mActivity.startService(getServiceIntent().setAction(MyVPNService.ACTION_DISCONNECT)) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == 988) {
            mActivity.startService(Intent(mActivity, MyVPNService::class.java))
        }
    }

    private fun checkProxyConfigs(proxyHost: String, proxyPort: String): Boolean {
        val hasIncompleteProxyConfigs = proxyHost.isEmpty() != proxyPort.isEmpty()
        if (hasIncompleteProxyConfigs) {
            mActivity.toast("代理设置不完整。对于HTTP代理，我们需要主机名和端口设置。")
        }
        return !hasIncompleteProxyConfigs
    }

    private fun checkPackages(packageNames: Set<String>): Boolean {
        val hasCorrectPackageNames = packageNames.isEmpty() ||
                mActivity.packageManager.getInstalledPackages(0)
                        .map { pi -> pi.packageName }
                        .containsAll(packageNames)
        if (!hasCorrectPackageNames) {
            mActivity.toast("某些指定的包名称与任何已安装的包都不对应。")
        }
        return hasCorrectPackageNames
    }

    /*protected override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == RESULT_OK) {
            startService(getServiceIntent().setAction(ToyVpnService.ACTION_CONNECT))
        }
    }*/

    private fun getServiceIntent(): Intent {
        return Intent(mActivity, MyVPNService::class.java)
    }
}