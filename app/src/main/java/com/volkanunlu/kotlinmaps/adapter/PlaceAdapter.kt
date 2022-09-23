package com.volkanunlu.kotlinmaps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.Placeholder
import androidx.recyclerview.widget.RecyclerView
import com.volkanunlu.kotlinmaps.databinding.RecyclerRowBinding
import com.volkanunlu.kotlinmaps.model.Place
import com.volkanunlu.kotlinmaps.view.MapsActivity


                //listemi verdim.
class PlaceAdapter( val placeList:List<Place>) : RecyclerView.Adapter<PlaceAdapter.Placeholder>() {
                        //recyclerrow bağladım.
    class  Placeholder( val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Placeholder { //recycler view oluşturma
        val recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Placeholder(recyclerRowBinding)

    }

    override fun onBindViewHolder(holder: Placeholder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text=placeList.get(position).name  //elementin ismi recycler row da bağlama
        holder.itemView.setOnClickListener {  //item'a tıklanınca ne olsun
            val intent= Intent(holder.itemView.context,MapsActivity::class.java)
            //çalışması için , sınıfın serialazible olması gerek , serileştirilebilir.
            intent.putExtra("selectedPlace",placeList.get(position))

            intent.putExtra("info", "old") //Eski bir veri gönderiyorum demek.


            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size   //list size kadar item oluştur.

    }

}