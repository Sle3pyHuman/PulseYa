package com.examen.pulseya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventViewHolder> {
    private final Context context;
    private final List<Evento> eventoList;

    public EventoAdapter(Context context, List<Evento> eventoList) {
        this.context = context;
        this.eventoList = eventoList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.evento_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evento evento = eventoList.get(position);

        holder.textViewTitulo.setText(evento.getTitulo());
        holder.textViewFecha.setText(evento.getFecha());
        holder.textViewHora.setText(evento.getHoraInicio());

        // dejar descripcion corta
        String description = evento.getDescripcion();
        if (evento.isExpanded()) {
            // mostrar full descripcion
            holder.textViewDescripcion.setText(description);
        } else {
            // mostrar corto la descripcion
            holder.textViewDescripcion.setText(description.length() > 65 ? description.substring(0, 65) + "... mostrar mas." : description);
        }

        // manejo en expanderse el descripcion
        holder.textViewDescripcion.setOnClickListener(v -> {
            evento.setExpanded(!evento.isExpanded());
            notifyItemChanged(position);
        });


        // cargar imagen usando Glide
        if (evento.getImagenURL() != null && !evento.getImagenURL().isEmpty()) {
            Glide.with(context).load(evento.getImagenURL()).into(holder.imageViewFlyer);
        } else {
            holder.imageViewFlyer.setImageResource(R.drawable.placeholder_img);
        }
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitulo, textViewDescripcion, textViewFecha, textViewHora;
        ImageView imageViewFlyer;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.titulo_evento);
            textViewDescripcion = itemView.findViewById(R.id.descripcion_evento);
            textViewFecha = itemView.findViewById(R.id.fecha_evento);
            textViewHora = itemView.findViewById(R.id.horaInicio_Evento);
            imageViewFlyer = itemView.findViewById(R.id.flayer);
        }
    }
}
