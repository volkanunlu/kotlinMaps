package com.volkanunlu.kotlinmaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.room.RoomDatabase

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.volkanunlu.kotlinmaps.R
import com.volkanunlu.kotlinmaps.databinding.ActivityMapsBinding
import com.volkanunlu.kotlinmaps.model.Place
import com.volkanunlu.kotlinmaps.roomdb.PlaceDao
import com.volkanunlu.kotlinmaps.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager:LocationManager  //konumla ilgili bütün işlemleri yapan sınıf.
    private lateinit var locationListener: LocationListener //konum değişikliklerini dinleyen arayüz.
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //izin almak adına oluşturdum.
    private lateinit var sharedPreferences: SharedPreferences  //son konumumu tutmak adına tanımladım.
    private var trackBoolean:Boolean? =null  //son konuma 1 kez zoomlama adına boolean kontrol değişkenim.

    private var selectedLatitude: Double? = null      //seçilmiş enlem onmaplongclicklistener sebebi
    private var selectedLongitude: Double? = null    //seçilmiş boylam onmaplongclicklistener sebebi

    private lateinit var db:PlaceDatabase
    private lateinit var placeDao: PlaceDao

    val compositeDisposable= CompositeDisposable()  //Kullan at ögemiz.
    var placeFromMain: Place?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        sharedPreferences=this.getSharedPreferences("com.volkanunlu.kotlinmaps", MODE_PRIVATE)
        trackBoolean=false
        selectedLatitude=0.0
        selectedLongitude=0.0

        db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            .allowMainThreadQueries()
            .build() //veritabanı initialize ettim.
        placeDao=db.placeDao()

        binding.saveButton.isEnabled=false



    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)  //uzun tıklama

        val intent=intent
        val info=intent.getStringExtra("info")

        if(info.equals("new")){

            binding.saveButton.visibility=View.VISIBLE
            binding.deleteButton.visibility=View.GONE


            //casting uygulayacağız. alacağım şeyden eminim.
            //android sistem servisleri , any! olarak döner ve casting yapıyorum eminim diyorum.
            locationManager= this.getSystemService(LOCATION_SERVICE) as LocationManager

            //  locationListener=LocationListener{  location-> }

            locationListener= object : LocationListener{   //konum değişikliklerini dinliyor.
                override fun onLocationChanged(p0: Location) {

                    //Haritada 1 defaya mahsus çalışsın ekranımda sürekli yer işgal etmesin diye verdim.
                    trackBoolean=sharedPreferences.getBoolean("trackBoolean",false)

                    if(trackBoolean==false){

                        val userLocation= LatLng(p0.latitude,p0.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }




                }
            }

            //izin alman gerekli
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //permission denied, because we are use != at top.

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request permission

                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)  //izin isteme işlemi

                    }.show()

                }
                else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)  //izin isteme işlemi


                }


            }
            else{
                //permission granted

                //konumgüncellemelerini istek atıyorur managera, sağlayıcımız , ne kadar sürede yenilensin , metre aralığı , listenerımız
                //yapacağın app'e göre süre yenilenmesi değişken olabilir.

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation!=null){
                    val lastUserLocation=LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))

                }
                mMap.isMyLocationEnabled=true  //konumumu gösteren mavi yuvarlak item.


            }



        }


        else  //Eski bir verim var ise.
        {
        mMap.clear()
        placeFromMain=intent.getSerializableExtra("selectedPlace") as Place
        placeFromMain?.let {

            val latlng=LatLng(it.latitude,it.longitude)
            mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))
            binding.placeText.setText(it.name)
            binding.saveButton.visibility=View.GONE
            binding.deleteButton.visibility=View.VISIBLE


        }

        }









    }

    private fun registerLauncher(){

        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if(result){
                //permission granted

                //Ek kontrole ihtiyaç duyduk. Bir kez daha izin verilip verilmediğini şarta aldık.
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(lastLocation!=null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    }

                }
                mMap.isMyLocationEnabled=true   //konumumu gösteren mavi yuvarlak item.




            }
            else{
                //permission denied  //kullanıcıya toast mesajı.
                Toast.makeText(this@MapsActivity,"Permission Needed",Toast.LENGTH_LONG).show()

            }

        }

    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear() // daha önce eklenen markerları temizlesin
        mMap.addMarker(MarkerOptions().position(p0))  //marker ekledik

        selectedLatitude=p0.latitude
        selectedLongitude=p0.longitude
        binding.saveButton.isEnabled=true



    }

    fun save(view: View){

        //Main Thread UI , Default Thread -> Cpu işlemleri  , IO Thread -> İnternet istekleri , veritabanı işlemleri.

        if(selectedLongitude!=null && selectedLatitude!=null){

            val place= Place(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!)
            compositeDisposable.add(
                placeDao.insert(place) //dao'yu yazdık.
                    .subscribeOn(Schedulers.io()) //arkaplanda çalıştır
                    .observeOn(AndroidSchedulers.mainThread()) //main threadda gözlemle
                    .subscribe(this::handleResponse) //bitince de bunu çalıştır.
            )

        }


    }

    private fun handleResponse(){
        val intent=Intent(this@MapsActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    fun delete(view: View){

        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)   //silme işlemi.
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

    }




}