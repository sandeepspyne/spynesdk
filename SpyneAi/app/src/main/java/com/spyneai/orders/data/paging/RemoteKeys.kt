package com.spyneai.orders.data.paging

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKeys(@PrimaryKey val projectId: String, val prevKey: Int?, val nextKey: Int?)