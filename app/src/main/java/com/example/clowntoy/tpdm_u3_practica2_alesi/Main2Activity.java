package com.example.clowntoy.tpdm_u3_practica2_alesi;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    EditText nameChar,sourceChar,sexChar;
    Button insUpd,del,con,conAll;
    FirebaseFirestore storage;
    ListView lista;
    List<Personaje> listaPersonaje;
    List<String> listNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        storage=FirebaseFirestore.getInstance();
        lista=findViewById(R.id.listCharacter);

        nameChar=findViewById(R.id.cNombre);
        sourceChar=findViewById(R.id.cFranquicia);
        sexChar=findViewById(R.id.cSexo);

        insUpd=findViewById(R.id.insUpdCharacter);
        del=findViewById(R.id.delCharacter);
        con=findViewById(R.id.conCharacter);
        conAll=findViewById(R.id.conAllCharacters);

        insUpd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insert();
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consult();
            }
        });

        conAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscar();
            }
        });


    }

    private void insert(){
        Personaje personaje=new Personaje(nameChar.getText().toString(),
                sourceChar.getText().toString(),
                sexChar.getText().toString());
        storage.collection("personajes").document(nameChar.getText().toString()).set(personaje)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this,"Inserción/Actualización exitosa",Toast.LENGTH_SHORT)
                                .show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Main2Activity.this,"Fallo en el proceso",Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void delete(){
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        final EditText idEliminar=new EditText(this);
        alert.setTitle("Eliminar").setMessage("Escriba el nombre:").setView(idEliminar)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(idEliminar.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this,"El personaje no existe",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminar(idEliminar.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }

    private void eliminar(String n){
        storage.collection("personajes").document(n).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Main2Activity.this,"Eliminación exitosa",Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Main2Activity.this,"No hubo eliminación",Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void consult(){
        AlertDialog.Builder alert=new AlertDialog.Builder(Main2Activity.this);
        final EditText telCons=new EditText(Main2Activity.this);
        alert.setTitle("Busqueda").setMessage("Escriba el nombre del personaje")
                .setView(telCons)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(telCons.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this,"Introduce un telefono",Toast.LENGTH_SHORT);
                            return;
                        }
                        buscar(telCons.getText().toString());
                    }
                }).show();
    }

    private void buscar(String n){
        storage.collection("personajes").whereEqualTo("nombre",n).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                Query q=queryDocumentSnapshots.getQuery();
                q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot snap:task.getResult()){
                                Map<String,Object> datos=snap.getData();
                                nameChar.setText(datos.get("nombre").toString());
                                sourceChar.setText(datos.get("franquicia").toString());
                                sexChar.setText(datos.get("sexo").toString());
                            }
                        }
                    }
                });
            }
        });
    }

    private void buscar(){
        storage.collection("personajes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                listaPersonaje=new ArrayList<>();
                listNames=new ArrayList<>();
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snap:task.getResult()){
                        Map<String,Object> datos=snap.getData();
                        listNames.add(snap.getId());
                        Personaje p=new Personaje(datos.get("nombre").toString(),
                                datos.get("franquicia").toString(),
                                datos.get("sexo").toString());
                        listaPersonaje.add(p);
                    }
                    asignarListView();
                }
            }
        });
    }

    private void asignarListView(){
        if(listaPersonaje.size()<=0){
            return;
        }
        String[] datos=new String[listaPersonaje.size()];

        for (int i=0;i<listaPersonaje.size();i++){
            Personaje p=listaPersonaje.get(i);
            datos[i]=p.nombre;
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<>(Main2Activity.this,android.R.layout.simple_list_item_1
        ,datos);
        lista.setAdapter(adapter);
    }

}
