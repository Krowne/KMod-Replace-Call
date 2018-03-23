package com.kmod.replace.call;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class XMod implements IXposedHookLoadPackage {
    // variables
    private static String v_a = "com.whatsapp";
    private static String audio_call = "audio_call";
    private static String menu = "onCreateOptionsMenu";
    private static String conv = ".Conversation";
    private static String numberName;
    private static String str = "string";
    private static String getXtra = "getStringExtra";
    private static String tlf = "tel:";
    private static Class<Menu> cMenu = Menu.class;

    private static String getSCall() {
        final android.content.Context context = AndroidAppHelper.currentApplication().getApplicationContext();
        android.content.res.Resources res = context.getResources();
        String call = res.getString(res.getIdentifier(audio_call, str, v_a));
        return call;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedHelpers.findAndHookMethod(Intent.class, getXtra, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam p) throws Throwable {
                String result = (String) p.getResult();
                if (result != null) {
                    if (result.contains("@")) {
                        String jid = result;
                        numberName = jid.split("@")[0];
                    }
                }
            }
        });

        findAndHookMethod(v_a + conv, lpparam.classLoader, menu, cMenu, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam p) throws Throwable {
                super.afterHookedMethod(p);
                final Activity activity = (Activity) p.thisObject;
                if (p.args[0] != null) {
                    Menu menu = (Menu) p.args[0];
                    MenuItem item = menu.getItem(0);
                    MenuItem item_call = menu.getItem(1);
                    if (item_call.toString().contains(getSCall())) {
                    item_call.getActionView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse(tlf + Uri.encode("+" + numberName.trim())));
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(callIntent);
                        }
                    });
                    }
                }
            }
        });

    }
}
