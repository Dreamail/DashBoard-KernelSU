package com.dashboard.kotlin

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.clashConfig
import com.dashboard.kotlin.clashhelper.clashStatus
import com.dashboard.kotlin.clashhelper.commandhelper
import com.dashboard.kotlin.suihelper.suihelper
import kotlinx.android.synthetic.main.fragment_main_page.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File


class MainPage : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }


    private val clashStatusClass = clashStatus()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "MainPageViewCreated")

        toolbar.title = getString(R.string.app_name)
        //TODO 添加 app 图标

        if (!suihelper.checkPermission()) {
            clash_status.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.error, context?.theme)
            )
            clash_status_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_service_not_running,
                    context?.theme
                )
            )
            clash_status_text.text = getString(R.string.sui_disable)
            netspeed_status_text.visibility = View.GONE


            GlobalScope.async {
                while (true) {
                    if (suihelper.checkPermission(request = false)) {
                        restartApp()
                        break
                    }
                    delay(1 * 1000)
                }
            }

        } else {

            if (clashStatus().runStatus()) {
                clash_status.setCardBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.colorPrimary, context?.theme)
                )
                clash_status_icon.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_activited, context?.theme)
                )
                clash_status_text.text = getString(R.string.clash_enable)

                netspeed_status_text.visibility = View.VISIBLE


                clashStatusClass.getTraffic()

                GlobalScope.launch(Dispatchers.IO) {
                    while (clashStatusClass.trafficThreadFlag) {
                        try {
                            val jsonObject = JSONObject(clashStatusClass.trafficRawText)
                            val upText: String = commandhelper.autoUnit(jsonObject.optString("up"))
                            val downText: String =
                                commandhelper.autoUnit(jsonObject.optString("down"))

                            withContext(Dispatchers.Main) {
                                netspeed_status_text.text =
                                    getString(R.string.netspeed_status_text).format(
                                        upText,
                                        downText
                                    )
                            }
                        } catch (ex: Exception) {
                            Log.w("trafficText", ex.toString())
                        }
                        delay(1000)
                    }
                }


            } else {
                clash_status.setCardBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.gray, context?.theme)
                )
                clash_status_icon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_service_not_running,
                        context?.theme
                    )
                )
                clash_status_text.text =
                    getString(R.string.clash_disable)
                netspeed_status_text.visibility = View.GONE

            }

        }

        clash_status.setOnClickListener {
            it.isClickable = false
            clash_status.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorPrimary,
                    context?.theme
                )
            )
            clash_status_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_refresh,
                    context?.theme
                )
            )
            clash_status_text.text = getString(R.string.clash_charging)
            netspeed_status_text.visibility = View.GONE

            GlobalScope.async {
                val result = doAssestsShellFile(
                    "${clashConfig.getClashType()}_" +
                            (if (clashStatus().runStatus()) {
                                "Stop"
                            } else {
                                "Start"
                            }) +
                            ".sh", !clashStatus().runStatus()
                )
                if (result == "")
                    restartApp()
                else
                    withContext(Dispatchers.Main){
                        cmd_result.text = "指令输出：\n$result\n"
                    }
                true
            }
        }





        menu_ip_check.setOnClickListener {
            it.findNavController().navigate(R.id.action_mainPage_to_ipCheckPage)
        }


        menu_web_dashboard.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("URL", "http://127.0.0.1:9090/ui/")
                    //if ((context?.resources?.configuration?.uiMode
                    //        ?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES) {
                    //    "?theme=dark"
                    //}else{
                    //    "?theme=light"
                    //})
            it.findNavController().navigate(R.id.action_mainPage_to_webViewPage, bundle)
        }

        menu_speed_test.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("URL", "https://fast.com/zh/cn/")
            it.findNavController().navigate(R.id.action_mainPage_to_webViewPage, bundle)
        }

        menu_setting.setOnClickListener {
            it.findNavController().navigate(R.id.action_manPage_to_settingPage)
        }
/*
        menu_web_dashboard_download.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("TYPE", "DASHBOARD")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)

        }

        menu_sub_download.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("TYPE", "SUB")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)
        }

        menu_mmdb_download.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("TYPE", "MMDB")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)
        }


        menu_version_switch.setOnClickListener {
            val versionArray = arrayOf<CharSequence>("CFM", "CPFM")
            val diaLogObj: AlertDialog? = activity?.let { itD ->
                AlertDialog.Builder(itD).let { it ->
                    it.setItems(
                        versionArray
                    ) { _, which ->
                        KV.encode("ClashType", versionArray[which].toString())
                        restartApp()
                    }
                    it.create()
                }
            }
            diaLogObj?.show()
        }
        */
    }

    override fun onStart() {
        super.onStart()
        cmd_result.text = suihelper.suCmd("${clashConfig.corePath} -v") + suihelper.suCmd("cat ${clashConfig.clashPath}/run/run.logs")
    }


    override fun onDestroyView() {
        clashStatusClass.stopGetTraffic()
        Log.d("DestroyView", "MainPageDestroyView")
        super.onDestroyView()
    }


    private fun restartApp() {
        val intent: Intent? = activity?.baseContext?.packageManager
            ?.getLaunchIntentForPackage(activity?.baseContext!!.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.putExtra("REBOOT", "reboot")
        startActivity(intent)
    }

    private suspend fun doAssestsShellFile(fileName: String, isStart: Boolean = false): String {
        context?.assets?.open(fileName)?.let { op ->
            File(context?.externalCacheDir, fileName).let { fo ->
                fo.outputStream().let { ip ->
                    op.copyTo(ip)
                }



                val result = suihelper.suCmd("sh '${context?.externalCacheDir}/${fileName}'")

                fo.delete()

                return result

            }
        }
        return ""
    }
}