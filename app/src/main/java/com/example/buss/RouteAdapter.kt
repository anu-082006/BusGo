package com.example.buss

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RouteAdapter(
    private var routes: List<BusRoute>,
    private val onFavoriteClick: (BusRoute) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRouteNo: TextView = view.findViewById(R.id.tvRouteNo)
        val tvRoutePath: TextView = view.findViewById(R.id.tvRoutePath)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        val context = holder.itemView.context

        holder.tvRouteNo.text = route.routeNo
        holder.tvRoutePath.text = "${route.source} → ${route.destination}"

        updateFavoriteIcon(holder.ivFavorite, route.routeNo, context)

        holder.ivFavorite.setOnClickListener {
            onFavoriteClick(route)
            updateFavoriteIcon(holder.ivFavorite, route.routeNo, context)
        }

        holder.itemView.setOnClickListener {
            showRouteDetails(context, route)
        }
    }

    private fun showRouteDetails(context: Context, route: BusRoute) {
        MaterialAlertDialogBuilder(context, R.style.Theme_Buss_Dialog)
            .setTitle("Route ${route.routeNo}")
            .setMessage("From: ${route.source}\nTo: ${route.destination}\n\nDo you want to track this bus in real-time?")
            .setNeutralButton("Close") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Track Bus") { _, _ ->
                val intent = Intent(context, BusTrackerActivity::class.java).apply {
                    putExtra("SELECTED_ROUTE", route)
                }
                context.startActivity(intent)
            }
            .show()
    }

    override fun getItemCount() = routes.size

    fun updateData(newRoutes: List<BusRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    private fun updateFavoriteIcon(imageView: ImageView, routeNo: String, context: Context) {
        val favs = SharedPreferencesHelper.getFavourites(context)
        val isFavorite = favs.contains(routeNo)
        
        if (isFavorite) {
            imageView.setImageResource(android.R.drawable.btn_star_big_on)
            imageView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.bmtc_blue)
            )
        } else {
            imageView.setImageResource(android.R.drawable.btn_star_big_off)
            imageView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.nav_unselected)
            )
        }
    }
}
