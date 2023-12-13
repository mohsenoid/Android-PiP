package com.mohsenoid.pip.core

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Context.isPipSupported(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    } else {
        false
    }
}

fun Context.isPipAllowedInSettings(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps?.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            @Suppress("DEPRECATION")
            appOps?.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        }
    } else {
        false
    }
}
