package com.ggandroid.crudexemplo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ContatosAdapter extends BaseAdapter {

    private Context ctx;
    private List<Contato> lista;

    private String HOST = "http://192.168.0.32/contatos";

    public ContatosAdapter(Context ctx2, List<Contato> lista2) {
        ctx = ctx2;
        lista = lista2;
    }
    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Contato getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;

        if(convertView == null) {
            LayoutInflater inflater = ((Activity)ctx).getLayoutInflater();
            v = inflater.inflate(R.layout.item_lista, null);
        } else {
            v = convertView;
        }

        Contato c = getItem(position);

        ImageView itemFoto = (ImageView) v.findViewById(R.id.itemFoto);
        TextView itemNome = (TextView) v.findViewById(R.id.itemNome);
        TextView itemTelefone = (TextView) v.findViewById(R.id.itemTelefone);
        TextView itemEmail = (TextView) v.findViewById(R.id.itemEmail);

        String fotoRecebida = HOST + "/" + c.getFoto();

        //Log.d("IMAGEM_RECEBIDA", fotoRecebida);

        Picasso.get().load(fotoRecebida).into(itemFoto);
        itemNome.setText(c.getNome());
        itemTelefone.setText(c.getTelefone());
        itemEmail.setText(c.getEmail());

        return v;
    }
}
