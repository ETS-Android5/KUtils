package com.kiven.sample.autoService;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kiven.kutils.logHelper.KLog;
import com.kiven.kutils.tools.KAppTool;
import com.kiven.kutils.tools.KUtil;
import com.kiven.kutils.util.ArrayMap;
import com.kiven.sample.util.UtilsKt;

public class WXShareTask implements AutoInstallService.AccessibilityTask {
    public static int logType = 0;//控制打印日志, 微信工具用

    private final String LauncherUI = "com.tencent.mm.ui.LauncherUI";//微信 界面
    private final String SnsTimeLineUI = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";//朋友圈 界面
    private final String settingUI = "com.tencent.mm.plugin.setting.ui.setting.SettingsUI";//设置 界面
    private final String tongYongSettingUI = "com.tencent.mm.plugin.setting.ui.setting.SettingsAboutSystemUI";//设置->通用 界面
    private final String SettingsPluginsUI = "com.tencent.mm.plugin.setting.ui.setting.SettingsPluginsUI";//设置->通用->辅助功能 界面
    private final String ContactInfoUI = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";//设置->通用->辅助功能->群发助手 界面
    private final String MassSendHistoryUI = "com.tencent.mm.plugin.masssend.ui.MassSendHistoryUI";//设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面
    //设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面->选择收信人界面
    private final String MassSendSelectContactUI = "com.tencent.mm.plugin.masssend.ui.MassSendSelectContactUI";
    //设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面->选择收信人界面->群发消息输入界面
    // 这个界面点击发送后，回到'MassSendHistoryUI'界面
    private final String MassSendMsgUI = "com.tencent.mm.plugin.masssend.ui.MassSendMsgUI";


    private final ArrayMap<String, AccessibilityStep> steps = new ArrayMap<>();

    WXShareTask() {
        steps.put("toMain", new AccessibilityStep() {
            @Override
            public boolean isThis(AccessibilityNodeInfo rootNode) {


                return false;
            }

            @Override
            public void deal(AccessibilityNodeInfo rootNode) {

            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityService service, AccessibilityEvent event) {
//        KLog.i("onAccessibilityEvent: " + (event == null ? "null" : event.getPackageName().toString()));
        if (event == null) return;


        AccessibilityNodeInfo eventNode = event.getSource();
        if (eventNode == null) return;

//        KLog.i("eventNode:" + eventNode);

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow(); //当前窗口根节点
        if (rootNode == null) return;

        deal(event, rootNode);


        eventNode.recycle();
    }

    private String curWXUI;

    private void deal(AccessibilityEvent event, AccessibilityNodeInfo rootNode) {


        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            KLog.i("点击：：：：：：：：：：：：：");
            AccessibilityUtil.printTree(event.getSource());
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            curWXUI = event.getClassName().toString();
        }

        if (curWXUI == null) return;

        // 放在 curWXUI 被记录之后
        switch (logType % 3) {
            case 0:
                KLog.i(String.format("%s %x %x", event.getClassName().toString(), event.getEventType(), event.getAction()));
                break;
            case 1:
                AccessibilityUtil.printTree(rootNode);
                break;
            case 2:
                return;
        }

        // step 1 : 微信主界面
        if (TextUtils.equals(curWXUI, LauncherUI)) {

            AccessibilityNodeInfo myNode = AccessibilityUtil.findTxtNode(rootNode, "我", "com.tencent.mm:id/djv");
            if (myNode != null) {

                AccessibilityNodeInfo settingNode = AccessibilityUtil.findTxtNode(rootNode, "设置", "android:id/title");

                if (settingNode == null) {
                    AccessibilityUtil.clickNode(myNode, true);
                } else {
                    AccessibilityUtil.clickNode(settingNode, true);
                }
            }

            return;
        }

        // step 2 : 设置界面
        if (TextUtils.equals(curWXUI, settingUI)) {
            /*AccessibilityNodeInfo tongyongNode = AccessibilityUtil.findTxtNode(rootNode, "通用", "android:id/title");
            if (tongyongNode == null){
                UtilsKt.showToast("未找到操作，请确认微信版本正确");
            }else {
                AccessibilityUtil.clickNode(tongyongNode, true);
            }*/

            AccessibilityUtil.findTxtClick(rootNode, "通用", "android:id/title");
        }

        // step 3 : 通用界面
        if (TextUtils.equals(curWXUI, tongYongSettingUI)) {
            AccessibilityUtil.findTxtClick(rootNode, "辅助功能", "android:id/title");
        }
        // step 4: 设置->通用->辅助功能 界面
        if (TextUtils.equals(curWXUI, SettingsPluginsUI)) {
            AccessibilityUtil.findTxtClick(rootNode, "群发助手", "android:id/title");
        }
        // step 5: 设置->通用->辅助功能->群发助手 界面
        if (TextUtils.equals(curWXUI, ContactInfoUI)) {
            AccessibilityNodeInfo startNode = AccessibilityUtil.findTxtNode(rootNode, "开始群发", "android:id/title");
            if (startNode == null){
                // 如果没有开始群发按钮，那么就是没有开启，先开启功能
                AccessibilityUtil.findTxtClick(rootNode, "启用该功能", "android:id/title");
            }else {
                AccessibilityUtil.clickNode(startNode, true);
            }
        }
        // step 6: 设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面
        // 注意：发送信息完成之后会回到这个界面，这个界面就是群发的历史记录界面
        if (TextUtils.equals(curWXUI, MassSendHistoryUI)) {
            // 新建群发按钮
            AccessibilityUtil.findNodeClickById(rootNode, "com.tencent.mm:id/dhn");
        }
        // step 7: 设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面->选择收信人界面
        if (TextUtils.equals(curWXUI, MassSendSelectContactUI)) {
        }
        // step 8: 设置->通用->辅助功能->群发助手->点击'开始群发'出现的有'新建群发'按钮的界面->选择收信人界面->群发消息输入界面
        // 注意：这个界面点击发送后，回到'MassSendHistoryUI'界面
        if (TextUtils.equals(curWXUI, MassSendMsgUI)) {
        }
        // step 9: 应该是回到了'MassSendHistoryUI'界面，该怎么处理呢
        if (TextUtils.equals(curWXUI, "")) {
        }

        /*switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                curWXUI = event.getClassName().toString();
                AccessibilityUtil.findTxtClick(rootNode, "我", "com.tencent.mm:id/djv");
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                break;
            default:
                break;
        }*/

    }

    public interface AccessibilityStep {
        boolean isThis(AccessibilityNodeInfo rootNode);

        void deal(AccessibilityNodeInfo rootNode);
    }

    @Override
    public void onServiceConnected(AccessibilityService service) {
        KAppTool.startApp(service, "com.tencent.mm");
    }
}
