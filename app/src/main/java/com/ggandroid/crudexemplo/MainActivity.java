package com.ggandroid.crudexemplo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editNome, editTelefone, editEmail, editId;
    Button btnNovo, btnSalvar, btnExcluir;
    ListView listViewContatos;
    ImageView imgFoto;

    private String HOST = "http://192.168.0.32/contatos";

    private int itemClicado;
    private String fotoClicada;

    ContatosAdapter contatosAdapter;
    List<Contato> lista;

    File fotoSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editNome = findViewById(R.id.editNome);
        editTelefone = findViewById(R.id.editTelefone);
        editEmail = findViewById(R.id.editEmail);
        editId = findViewById(R.id.editId);

        btnNovo = findViewById(R.id.btnNovo);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnExcluir = findViewById(R.id.btnExcuir);

        listViewContatos = findViewById(R.id.listViewContatos);

        imgFoto = (ImageView) findViewById(R.id.imgFoto);

        lista = new ArrayList<Contato>();
        contatosAdapter = new ContatosAdapter(MainActivity.this, lista);

        listViewContatos.setAdapter(contatosAdapter);

        listaContatos();

        imgFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(MainActivity.this);
            }
        });

        btnNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpaCampos();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String id = editId.getText().toString();
                final String nome = editNome.getText().toString();
                final String telefone = editTelefone.getText().toString();
                final String email = editEmail.getText().toString();

                if (nome.isEmpty()) {
                    editNome.setError("O Nome é obrigatório!");
                } else if (id.isEmpty()) {
                    // CREATE
                    String url = HOST + "/create.php";
                    Ion.with(MainActivity.this)
                            .load(url)
                            .setMultipartParameter("nome", nome)
                            .setMultipartParameter("telefone", telefone)
                            .setMultipartParameter("email", email)
                            .setMultipartFile("foto", fotoSelecionada)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {

                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    if (result.get("CREATE").getAsString().equals("OK")) {

                                        int idRetornado = Integer.parseInt(result.get("ID").getAsString());
                                        String fotoRetornada = result.get("FOTO").getAsString();

                                        Contato c = new Contato();

                                        c.setId(idRetornado);
                                        c.setNome(nome);
                                        c.setTelefone(telefone);
                                        c.setEmail(email);
                                        c.setFoto(fotoRetornada);

                                        lista.add(c);

                                        contatosAdapter.notifyDataSetChanged();

                                        limpaCampos();

                                        Toast.makeText(MainActivity.this,
                                                "Salvo com Sucesso!, id: " + idRetornado,
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                "Ocorreu um erro ao salvar!",
                                                Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                } else {
                    // UPDATE
                    String url = HOST + "/update.php";
                    Ion.with(MainActivity.this)
                            .load(url)
                            .setBodyParameter("id", id)
                            .setBodyParameter("nome", nome)
                            .setBodyParameter("telefone", telefone)
                            .setBodyParameter("email", email)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    if (result.get("UPDATE").getAsString().equals("OK")) {

                                        Contato c = new Contato();

                                        c.setId(Integer.parseInt(id));
                                        c.setNome(nome);
                                        c.setTelefone(telefone);
                                        c.setEmail(email);

                                        lista.set(itemClicado, c);

                                        contatosAdapter.notifyDataSetChanged();

                                        limpaCampos();

                                        Toast.makeText(MainActivity.this,
                                                "Atualizado com Sucesso!",
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                "Ocorreu um erro ao Atualizar!",
                                                Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                }
            }

        });

        listViewContatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Contato c = (Contato) adapterView.getAdapter().getItem(position);

                editId.setText(String.valueOf(c.getId()));
                editNome.setText(String.valueOf(c.getNome()));
                editTelefone.setText(String.valueOf(c.getTelefone()));
                editEmail.setText(String.valueOf(c.getEmail()));

                String fotoRecebida = HOST + "/" + c.getFoto();

                Picasso.get().load(fotoRecebida).into(imgFoto);

                itemClicado = position;
                fotoClicada = c.getFoto();
            }
        });

        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editId.getText().toString();

                if (id.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Nenhum Contato está Selecionado!",
                            Toast.LENGTH_LONG).show();
                } else {

                    // Tentar Apagar o Contato

                    String url = HOST + "/delete.php";

                    Ion.with(MainActivity.this)
                            .load(url)
                            .setBodyParameter("id", id)
                            .setBodyParameter("foto", fotoClicada)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    if (result.get("DELETE").getAsString().equals("OK")) {

                                        lista.remove(itemClicado);
                                        contatosAdapter.notifyDataSetChanged();

                                        limpaCampos();

                                        Toast.makeText(MainActivity.this,
                                                "Excluido com Sucesso!",
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                "Ocorreu um erro ao Excluir!",
                                                Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imgFoto.setImageURI(resultUri);
                fotoSelecionada = new File(resultUri.getPath());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void limpaCampos() {
        editId.setText("");
        editNome.setText("");
        editTelefone.setText("");
        editEmail.setText("");
        imgFoto.setImageResource(R.drawable.sem_foto);

        editNome.requestFocus();
    }

    private void listaContatos() {
        String url = HOST + "/read.php";
        Ion.with(getBaseContext())
                .load(url)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        for (int i = 0; i < result.size(); i++) {
                            JsonObject obj = result.get(i).getAsJsonObject();
                            Contato c = new Contato();

                            c.setId(obj.get("id").getAsInt());
                            c.setNome(obj.get("nome").getAsString());
                            c.setTelefone(obj.get("telefone").getAsString());
                            c.setEmail(obj.get("email").getAsString());
                            c.setFoto(obj.get("foto").getAsString());

                            lista.add(c);
                        }

                        contatosAdapter.notifyDataSetChanged();
                    }
                });
    }
}

