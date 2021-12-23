package com.spyneai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.spyneai.room.AppDatabase
import com.spyneai.room.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomTestActivity : AppCompatActivity() {

    val TAG = RoomTestActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_test)


        GlobalScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "spyne-db"
            ).build()


            val userDao = db.userDao()
            userDao.insertAll(
                User(0,
                    "sandeep",
                    "singh"
                )
            )
            val users: List<User> = userDao.getAll()

            users.forEach {
                Log.d(TAG, "onCreate: "+it.firstName)
                Log.d(TAG, "onCreate: "+it.lastName)
                Log.d(TAG, "onCreate: "+it.uid)
            }
        }

    }
}