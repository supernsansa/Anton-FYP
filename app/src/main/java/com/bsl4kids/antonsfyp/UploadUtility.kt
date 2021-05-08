package com.bsl4kids.antonsfyp

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadUtility(activity: Activity) {

    var activity = activity;
    var dialog: ProgressDialog? = null
    //var serverURL: String = "http://192.168.1.173:8080/FYP_Scripts/upload.php"
    var serverURL: String = "http://" + MainActivity.ip_address + "/FYP_Scripts/upload.php"
    var serverUploadDirectoryPath: String = "http://" + MainActivity.ip_address + "FYP_Scripts/Videos/"
    val client = OkHttpClient()

    //Use this method if uploading an image instead of a video
    fun setImageMode(image: Boolean) {
        if(image == true) {
            serverURL = "http://" + MainActivity.ip_address + "/FYP_Scripts/uploadImage.php"
        }
        else {
            return
        }
    }

    fun uploadFile(sourceFilePath: String, uploadedFileName: String? = null) {
        uploadFile(File(sourceFilePath), uploadedFileName)
    }

    fun uploadFile(sourceFileUri: Uri, uploadedFileName: String? = null): String {
        val pathFromUri = URIPathHelper().getPath(activity,sourceFileUri)
        return uploadFile(File(pathFromUri), uploadedFileName)
    }

    fun uploadFile(sourceFile: File, uploadedFileName: String? = null): String {
        //Thread {
            val mimeType = getMimeType(sourceFile);
            if (mimeType == null) {
                Log.e("file error", "Not able to get mime type")
                //return@Thread
                return "fail"
            }
            val fileName: String = if (uploadedFileName == null)  sourceFile.name else uploadedFileName
            toggleProgressDialog(true)
            try {
                val requestBody: RequestBody =
                        MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("uploaded_file", fileName,sourceFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                                .build()

                val request: Request = Request.Builder().url(serverURL).post(requestBody).build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("File upload","success")
                    showToast("File uploaded successfully")
                    toggleProgressDialog(false)
                    return "success"
                } else {
                    Log.e("File upload", "failed")
                    showToast("File uploading failed")
                    toggleProgressDialog(false)
                    return "fail"
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("File upload", "failed")
                showToast("File uploading failed")
                toggleProgressDialog(false)
                return "fail"
            }
        //}.start()
    }

    // url = file path or whatever suitable URL you want.
    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun showToast(message: String) {
        activity.runOnUiThread {
            Toast.makeText( activity, message, Toast.LENGTH_LONG ).show()
        }
    }

    fun toggleProgressDialog(show: Boolean) {
        activity.runOnUiThread {
            if (show) {
                dialog = ProgressDialog.show(activity, "", "Uploading file...", true);
            } else {
                dialog?.dismiss();
            }
        }
    }

}