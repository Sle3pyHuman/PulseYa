package com.examen.pulseya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventoAdapterUser extends RecyclerView.Adapter<EventoAdapterUser.EventViewHolder> {
    private final Context context;
    private final List<Evento> eventoList;
    private OnMapButtonClickListener listener;

    public EventoAdapterUser(Context context, List<Evento> eventoList) {
        this.context = context;
        this.eventoList = eventoList;
    }

    public void setOnMapButtonClickListener(OnMapButtonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.evento_itemuser, parent, false);
        return new EventViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evento evento = eventoList.get(position);

        holder.textViewNombreUsuario.setText(evento.getCreadorId());
        holder.textViewTitulo.setText(evento.getTitulo());
        holder.textViewFecha.setText(evento.getFecha());
        holder.textViewHora.setText(evento.getHoraInicio());

        // dejar descripcion corta
        String description = evento.getDescripcion();
        if (evento.isExpanded() || description.length() < 65) {
            // mostrar full descripcion
            holder.textViewDescripcion.setText(description);
        } else {
            // mostrar corto la descripcion
            String shortDescription = description.length() > 65 ? description.substring(0, 64) : description;
            String fullText = shortDescription + "... mostrar más."; // Añadir el "mostrar más" al final

            // Crear SpannableString
            SpannableString spannableString = new SpannableString(fullText);
            int colorLink = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_link);

            // Encontrar la posición de la palabra "mostrar más"
            int start = shortDescription.length();
            int end = fullText.length();

            // Aplicar color a "mostrar más"
            spannableString.setSpan(new ForegroundColorSpan(colorLink), start, end, 0); // Cambiar Color.RED a cualquier color que desees

            // Establecer el texto con el color aplicado
            holder.textViewDescripcion.setText(spannableString);
        }

        // manejo en expanderse el descripcion
        holder.textViewDescripcion.setOnClickListener(v -> {
            evento.setExpanded(!evento.isExpanded());
            notifyItemChanged(position);
        });


        // cargar imagen usando Glide
        if (evento.getImagenUrl() != null && !evento.getImagenUrl().isEmpty()) {
            Glide.with(context).load(evento.getImagenUrl()).into(holder.imageViewFlyer);
        } else {
            holder.imageViewFlyer.setImageResource(R.drawable.placeholder_img);
        }

        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notificar al fragmento principal que se hizo clic en el botón
                if (listener != null) {
                    listener.onMapButtonClick(); // Aquí se notifica el clic
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombreUsuario, textViewTitulo, textViewDescripcion, textViewFecha, textViewHora;
        ImageView imageViewFlyer;
        Button btnEliminar;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreUsuario = itemView.findViewById(R.id.nombre_usuario);
            textViewTitulo = itemView.findViewById(R.id.titulo_evento);
            textViewDescripcion = itemView.findViewById(R.id.descripcion_evento);
            textViewFecha = itemView.findViewById(R.id.fecha_evento);
            textViewHora = itemView.findViewById(R.id.horaInicio_Evento);
            imageViewFlyer = itemView.findViewById(R.id.flayer);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    public interface OnMapButtonClickListener {
        void onMapButtonClick();
    }

}
