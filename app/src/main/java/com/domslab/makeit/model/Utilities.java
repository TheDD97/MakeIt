package com.domslab.makeit.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.domslab.makeit.callback.FirebaseCallBack;
import com.domslab.makeit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {
    public static final long MAX_FILE_SIZE = 1024 * 1024;
    public static final long MAX_IMAGE_SIZE = 1024 * 30;
    public static final String COVER_SIZE_EXCEEDED = "La dimensione della cover è troppo elevata, dev'essere pari o inferiore a " + (MAX_IMAGE_SIZE) / 1024 + "Kb";
    public static final String IMAGE_SIZE_EXCEEDED = "La dimensione di una immagine è troppo elevata, dev'essere pari o inferiore a " + (MAX_IMAGE_SIZE) / 1024 + "Kb";
    public static final String NO_COVER = "Non è stata trovata la cover, controlla il file";
    public static final String NO_IMAGE = "Hai dichiarato di voler inserire un'immagine ma non è stata trovata, controlla il file";
    public static final String NO_DESCRIPTION = "Descrizione non trovata, controlla il file";
    public static final String PAGE_EMPTY = "Contenuto della pagina non trovato, controlla pagina: ";
    public static final String INVALID_TIMER_VALUE = "Timer impostato in maniera errata, controlla la durata";
    public static final String INVALID_VIDEO_ID = "Codice video non valido, controlla che sia scritto correttamente";
    public static final String NO_NUMPAGE = "Numero di pagine non indicato, controlla il file";
    public static final String INVALID_NUMPAGE_VALUE = "Dev'essere presente almeno una pagina all'interno di un manuale, controlla il file";
    public static final String INVALID_NAME = "Il nome non può essere vuoto, controlla il file";
    public static final String NO_NAME = "Nome del manuale non presente, controlla il file";
    public static CharSequence noOldEmail = "Devi inserire l'attuale email se desideri cambiarla";
    public static String locationLabel = "Posizione: ";
    public static String checkUpload = "Confermi la tua scelta?";
    public static String manualHelp = "Per realizzare un manuale assicurati di utilizzare i seguenti attributi nel file JSON da realizzare:\n-name: per indicarne il nome\n-description: per indicarne la descrizione\n-cover: per indicare la copertina\n-numpage: per indicare il numero di pagine che ha il manuale.\n\n\nPer indicare il contenuto di una pagina bisogna indicare il numero della pagina ed attribuire i contenuti mediate i tag:\n-text: per inserire del testo\n-image: per inserire un'immagine\n-timer: per inserire un timer indicando il tempo in millisecondi\n-yt_video: per inserire l'id di un video proveniente da Youtube";
    public static final String helpTitle = "Come si crea un manuale?";
    private static String currentUID;
    public static UserHelperClass currentUser;
    public static final String sharedPreferencesName = "lsaas";
    public static final String verifying = "Verifica in corso...";
    public static final String loading = "Caricamento in corso...";
    public static final String path = "https://makeit-27047-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final String noUsername = "Username non inserito";
    public static final String noPassword = "Password non inserita";
    public static final String signUpNameError = "Controlla il nome";
    public static final String signUpEmptyEmail = "Email non inserita";
    public static final String signUpWrongEmailFormat = "Controlla l'email";
    public static final String signUpSurnameError = "Controlla il cognome";
    public static final String signUpUsernameAlreadyExists = "Questo username è già esistente";
    public static final String signUpEmptyUsername = "Controlla l'username";
    public static final String noConfirmPassword = "Inserisci la password in entrambi i campi";
    public static final String noBothPsw = "Le password sono differenti";
    public static final String passwordFormat = "La password deve contenere almeno 8 caratteri, un carattere maiuscolo, un carattere speciale ed un numero";
    public static String emailNoMatch = "Questa email non corrisponde a quella attuale";
    public static String currentPage = "Pagina: ";
    public static String FoodLabel = "Alimenti";
    public static String ToyLabel = "Giochi";
    public static String HomeLabel = "Casa";
    private static FirebaseDatabase rootNode;
    private static DatabaseReference reference;
    private static FirebaseAuth auth = null;
    private static AlertDialog.Builder progressDialog;
    private static Dialog dialog;

    private Utilities() {
    }

    public static void showProgressDialog(Context context) {
        if (progressDialog == null) {
            progressDialog = new AlertDialog.Builder(context, R.style.MyLoadingDialogTheme);
            progressDialog.setView(R.layout.progess);
            progressDialog.setCancelable(false);
            dialog = progressDialog.create();
            dialog.show();
        }
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            dialog.dismiss();
            progressDialog = null;

        }
    }


    public static FirebaseAuth getAuthorisation() {
        if (auth == null)
            auth = FirebaseAuth.getInstance();
        return auth;
    }

    public static void setCurrentUsername(String user) {
        currentUID = user;
        //setUser();
    }

    private static void setUser(UserHelperClass user) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        readData(new FirebaseCallBack() {
            @Override
            public void onCallBack(List<String> list, boolean business, boolean wait) {
                currentUser = new UserHelperClass(list.get(0), list.get(1), list.get(2), business, list.get(3), wait);
            }
        });
    }


    private static void readData(FirebaseCallBack callBack) {
        Query checkUser = reference.orderByChild(currentUID);
        ArrayList<String> userData = new ArrayList<>();
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = "", surname = "", email = "", username = "";
                boolean business = false, waiting = false;
                UserHelperClass userHelperClass = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(currentUID)) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            email = o.child("email").getValue().toString();
                            business = (boolean) o.child("advanced").getValue();
                            username = o.child("username").getValue().toString();
                            waiting = (boolean) o.child("waiting").getValue();
                            userData.addAll(Arrays.asList(name, surname, email, username));
                            callBack.onCallBack(userData, business, waiting);
                            userHelperClass = new UserHelperClass(name, surname, email, business, username, waiting);
                            currentUser = userHelperClass;
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void updateUser(UserHelperClass userHelperClass, String id) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        reference.child(id).setValue(userHelperClass);
        currentUser = userHelperClass;
    }

    public static void clear() {
        currentUser = null;
        currentUID = null;
        auth = null;
    }


    public static String getCurrentUID() {
        return currentUID;
    }
}
