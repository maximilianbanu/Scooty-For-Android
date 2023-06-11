package com.example.licentaremastered.data_management

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object DataSyncService {


    fun sendMessageToWatch(context: Context, message: String) {
        val dataItem = PutDataMapRequest.create("/message").run {
            dataMap.putString("message", message)
            asPutDataRequest()
        }

        Wearable.getDataClient(context).putDataItem(dataItem)
    }

}