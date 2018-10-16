package com.gas.medidor.detector.views.adapters

import android.bluetooth.BluetoothDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gas.medidor.detector.R
import java.util.ArrayList

class IdentificadorAdapter(private var identificador: ArrayList<BluetoothDevice>, private var layout: Int?, private var listener: OnItemClickListener?) : RecyclerView.Adapter<IdentificadorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layout?.let { LayoutInflater.from(parent.context).inflate(it, parent, false) }
        return ViewHolder(v!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listener?.let { holder.bind(identificador[position], it) }
    }

    override fun getItemCount(): Int {
        return identificador.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewAddress: TextView = itemView.findViewById(R.id.textViewAddress)

        internal fun bind(i: BluetoothDevice, listener: OnItemClickListener) {
            textViewName.text = i.name
            textViewAddress.text = i.address
            itemView.setOnClickListener {
                listener.onClick(i, adapterPosition)
            }
        }
    }


    interface OnItemClickListener {
        fun onClick(i: BluetoothDevice, position: Int)
    }


}