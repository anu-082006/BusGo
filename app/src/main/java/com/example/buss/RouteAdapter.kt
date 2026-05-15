package com.example.buss

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

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
            Toast.makeText(
                context,
                "Stops: ${route.stops.joinToString(" → ")}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun getItemCount() = routes.size

    fun updateData(newRoutes: List<BusRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    private fun updateFavoriteIcon(imageView: ImageView, routeNo: String, context: Context) {
        val favs = SharedPreferencesHelper.getFavourites(context)
        if (favs.contains(routeNo)) {
            imageView.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            imageView.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
}
