package com.domslab.makeit.view.menu;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.model.Manual;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.model.ManualPage;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddFragment extends Fragment {

    private Button load, upload;
    private TextView info;
    private Manual manual;
    private Spinner spinner;
    private HashMap<String, String> images = new HashMap<>();
    boolean noError = true;

    public AddFragment() {
        // Required empty public constructor
    }

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = getView().findViewById(R.id.categories_spinner);
        load = this.getView().findViewById(R.id.search_file);
        info = this.getView().findViewById(R.id.info);
        upload = getView().findViewById(R.id.upload_file);
        upload.setVisibility(View.INVISIBLE);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setMessage(Utilities.checkUpload);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadManual(new FirebaseCallBack() {
                            @Override
                            public void onCallBack(List<String> list, boolean business, boolean wait) {
                                if (noError) {
                                    Toast.makeText(getContext(), "Fatto.",
                                            Toast.LENGTH_SHORT).show();
                                } else Toast.makeText(getContext(), "Errore, riprova.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }, manual);
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.create().show();
            }
        });
        loadCategories();
        load.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                /*byte[] decodedString = Base64.getDecoder().decode("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");
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);*/
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                //Utilities.setManual("1");

            }
        });
    }

    private void loadCategories() {
        Utilities.showProgressDialog(getContext(), true);
        ArrayList<String> categories = new ArrayList<>();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        Query query = reference.child("categories");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot o : snapshot.getChildren()) {
                        categories.add(o.getValue().toString());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, categories);
                    spinner.setAdapter(adapter);
                    Utilities.closeProgressDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                info.setText(Utilities.locationLabel + uri.getLastPathSegment());
                String fileContent = readTextFile(uri);
                Integer page = 1;
                manual = new Manual();
                try {
                    JSONObject object = new JSONObject(fileContent);
                    manual.setOwner(Utilities.getAuthorisation().getCurrentUser().getUid());
                    if (object.has("name"))
                        manual.setName(object.getString("name"));
                    if (object.has("cover")) {
                        if (object.getString("cover").getBytes().length < Utilities.MAX_IMAGE_SIZE) {
                            images.put("cover", object.getString("cover"));
                            manual.setCover("y");
                        } else {
                            Toast.makeText(getContext(), Utilities.COVER_SIZE_EXCEEDED,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getContext(), Utilities.NO_COVER,
                                Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (object.has("description"))
                        manual.setDescription(object.getString("description"));
                    else {
                        Toast.makeText(getContext(), Utilities.NO_DESCRIPTION,
                                Toast.LENGTH_LONG).show();
                        return;

                    }
                    while (page <= object.getInt("numpage")) {
                        JSONArray jsonArray = object.getJSONArray(page.toString());
                        ManualPage manualPage = new ManualPage();
                        // System.out.println(jsonArray);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.has("text"))
                                manualPage.add("text", jsonObject.getString("text"));
                            if (jsonObject.has("image")) {
                                //manualPage.add("image", jsonObject.getString("image"));
                                manualPage.add("image", "y");
                                images.put("image" + page.toString(), jsonObject.getString("image"));
                            }
                            if (jsonObject.has("timer"))
                                manualPage.add("timer", jsonObject.getString("timer"));
                            if(jsonObject.has("yt_video"))
                                manualPage.add("yt_video",jsonObject.getString("yt_video"));
                        }
                        if (manualPage.getPageContent().isEmpty()) {
                            Toast.makeText(getContext(), Utilities.PAGE_EMPTY + page,
                                    Toast.LENGTH_SHORT).show();
                            return;

                        }
                        manual.addPage(page.toString(), manualPage);
                        ++page;
                    }

                    upload.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void uploadManual(FirebaseCallBack callBack, Manual manual) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference("manual");
        StringBuilder lastId = new StringBuilder();
        Query query = reference.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Utilities.showProgressDialog(getContext(), false);
                if (snapshot.exists())
                    lastId.append(snapshot.getChildrenCount());
                else {
                    lastId.append(Integer.toString(0));
                }
                System.out.println(lastId.toString());
                Integer id = Integer.parseInt(lastId.toString()) + 1;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                manual.setDate(sdf.format(calendar.getTime()));
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                for (String key : images.keySet()) {
                    byte[] b = images.get(key).getBytes(StandardCharsets.UTF_8);

                    UploadTask uploadTask = firebaseStorage.getReference(id.toString()).child(key).putBytes(b);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            noError = false;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (key.equals("cover")) {
                                manual.setCover(images.get(key));
                                ManualFlyweight.getInstance().addManual(id.toString(), manual);
                            }
                        }
                    });
                }
                manual.setTime(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                manual.setCategory(spinner.getSelectedItem().toString());
                reference.child(id.toString()).setValue(manual);
                Utilities.closeProgressDialog();


                callBack.onCallBack(null, false, false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private String readTextFile(Uri uri) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(this.getActivity().getContentResolver().openInputStream(uri)));

            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}