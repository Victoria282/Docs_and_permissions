package com.example.docs_and_permissions.utils

import android.Manifest.permission
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/*   Класс, для работы с пользовательскими разрешениями на устройстве

     Android Version           API level            NameVersion
            14                    34
            13                    33                    TIRAMISU
            12                    31 / 32               S/S_V2
            11                    30                    R
            10                    29                    Q
            9                     28                    P
            8                     26 / 27               O/O_MR1
            7                     24 / 25               N/N_MR1
            6.0                   23                    M
 */

private const val NOTIFICATIONS_REQUEST_CODE = 1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP_MR1)
fun isApiLevel22(): Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isApiLevel29(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isApiLevel30(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isApiLevel33(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/* Начиная с Android 13 (API level 33), необходимо предоставить раздрешения на Уведомления; */
fun Activity.requestAndroid13NotificationsPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    if (!hasNotificationsPermissions())
        requestPermissions(arrayOf(POST_NOTIFICATIONS), NOTIFICATIONS_REQUEST_CODE)
}

fun hasNotificationsPermissions(permissions: Map<String, Boolean>): Boolean =
    if (isApiLevel33()) permissions[POST_NOTIFICATIONS] ?: false
    else true

fun Context.hasNotificationsPermissions(): Boolean =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
    else ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED

fun Context.requestNotificationsPermission(
    requestHandEnableLambda: () -> Unit = { },
    alreadyGrantedLambda: () -> Unit
) {
    val permissionsToRequest = mutableListOf<String>()
    if (isApiLevel33()) {
        val permissionsGranted =
            ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED
        if (!permissionsGranted) permissionsToRequest.add(POST_NOTIFICATIONS)
    }

    permissionsToRequest.takeIf { it.isNotEmpty() }
        ?.let { requestHandEnableLambda() }
        ?: run { alreadyGrantedLambda() }
}

fun Context.hasStoragePermissions(): Boolean =
    if (isApiLevel30()) Environment.isExternalStorageManager()
    else ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED

fun Activity.requestStoragePermission(
    permissionsLauncher: ActivityResultLauncher<Array<String>>,
    alreadyGrantedLambda: () -> Unit
) {
    val hasStoragePermission = hasStoragePermissions()

    val permissionsToRequest = mutableListOf<String>()

    when {
        hasStoragePermission && isApiLevel30() -> alreadyGrantedLambda()
        !hasStoragePermission && isApiLevel30() -> requestStorageAndroid11(this, 1)

        !hasStoragePermission && !isApiLevel30() -> {
            permissionsToRequest.add(WRITE_EXTERNAL_STORAGE)
            permissionsToRequest.add(READ_EXTERNAL_STORAGE)
        }
    }

    if (permissionsToRequest.isNotEmpty())
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    else {
        if (isApiLevel30()) return
        alreadyGrantedLambda()
    }
}

fun hasStoragePermissions(
    permissions: Map<String, Boolean>,
    activity: Activity,
    requestCode: Int
): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val isNeedRequestPermission = permissions[permission.MEDIA_CONTENT_CONTROL] == true &&
            permissions[WRITE_EXTERNAL_STORAGE] == true
    if (isNeedRequestPermission)
        requestStoragePermissions(activity, requestCode)
    isNeedRequestPermission
} else
    hasStoragePermissionOldVersion(permissions, activity, requestCode)


private fun hasStoragePermissionOldVersion(
    permissions: Map<String, Boolean>,
    activity: Activity,
    requestCode: Int
): Boolean {
    val isNeedRequestPermission = permissions[READ_EXTERNAL_STORAGE] == true &&
            permissions[WRITE_EXTERNAL_STORAGE] == true
    if (isNeedRequestPermission)
        requestStoragePermissions(activity, requestCode)
    return isNeedRequestPermission
}

fun requestStoragePermissions(activity: Activity, requestCode: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        requestStorageAndroid11(activity, requestCode)
    else ActivityCompat.requestPermissions(
        activity, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), requestCode
    )
}

@RequiresApi(Build.VERSION_CODES.R)
private fun requestStorageAndroid11(activity: Activity, requestCode: Int) = try {
    val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    intent.addCategory("android.intent.category.DEFAULT")
    intent.data = Uri.parse(String.format("package:%s", activity.packageName))
    activity.startActivityForResult(intent, requestCode)
} catch (e: Exception) {
    val intent = Intent()
    intent.action = ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
    activity.startActivityForResult(intent, RESULT_OK)
}

fun hasPermissionsToDownload(
    permissions: Map<String, Boolean>,
    activity: Activity,
    requestCode: Int
): Boolean = if (isApiLevel33())
    permissions[POST_NOTIFICATIONS] ?: false
else hasStoragePermissions(permissions, activity, requestCode)