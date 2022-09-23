package com.volkanunlu.kotlinmaps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.volkanunlu.kotlinmaps.R
import com.volkanunlu.kotlinmaps.adapter.PlaceAdapter
import com.volkanunlu.kotlinmaps.databinding.ActivityMainBinding
import com.volkanunlu.kotlinmaps.model.Place
import com.volkanunlu.kotlinmaps.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable=CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        val db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
        val placeDao=db.placeDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

    }


    private fun handleResponse(placeList: List<Place>){  //Bu metodun bana list of placei vermesi lazım.

        binding.recyclerView.layoutManager=LinearLayoutManager(this)  //recyclerview görünümü
        val adapter=PlaceAdapter(placeList)  //adapterımı oluşturdum.
        binding.recyclerView.adapter=adapter  //bağlama işlemi.

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //menüyü bağlama

        val menuInflater= menuInflater
        menuInflater.inflate(R.menu.places_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean { //menüden bir şey seçildiğinde

        if(item.itemId== R.id.add_place){

            val intent=Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("info","new") //yeni bir şey gönderiyorum diyorum.
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }

}