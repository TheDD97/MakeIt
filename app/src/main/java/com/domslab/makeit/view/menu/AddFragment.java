package com.domslab.makeit.view.menu;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.domslab.makeit.callback.FirebaseCallBack;
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
    private SharedPreferences preferences;
    private HashMap<String, String> images = new HashMap<>();
    private HashMap<String, String> categoryLabel = new HashMap<>();
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
        preferences = getActivity().getSharedPreferences(Utilities.sharedPreferencesName,getActivity().MODE_PRIVATE);
        spinner = getView().findViewById(R.id.categories_spinner);
        load = this.getView().findViewById(R.id.search_file);
        info = this.getView().findViewById(R.id.info);
        upload = getView().findViewById(R.id.upload_file);
        upload.setVisibility(View.INVISIBLE);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogTheme);
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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void loadCategories() {
        Utilities.showProgressDialog(getContext());
        ArrayList<String> categories = new ArrayList<>();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        Query query = reference.child("categories");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot o : snapshot.getChildren()) {
                        String value = o.getValue().toString();
                        String key = "";
                        if (value.equals("Food"))
                            key = Utilities.FoodLabel;
                        if (value.equals("Toy"))
                            key = Utilities.ToyLabel;
                        if (value.equals("Home"))
                            key = Utilities.HomeLabel;
                        categoryLabel.put(key, value);
                        categories.add(key);
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
                        if (!object.getString("name").equals(""))
                            manual.setName(object.getString("name"));
                        else {
                            Toast.makeText(getContext(), Utilities.INVALID_NAME,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    else {
                        Toast.makeText(getContext(), Utilities.NO_NAME,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
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
                    if (object.has("numpage"))
                        if (object.getInt("numpage") > 0) {
                            while (page <= object.getInt("numpage")) {
                                JSONArray jsonArray = object.getJSONArray(page.toString());
                                ManualPage manualPage = new ManualPage();
                                // System.out.println(jsonArray);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (jsonObject.has("text"))
                                        manualPage.add("text", jsonObject.getString("text"));
                                    if (jsonObject.has("image")) {
                                        if (jsonObject.getString("image").equals("")) {
                                            Toast.makeText(getContext(), Utilities.NO_IMAGE,
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (jsonObject.getString("image").getBytes().length < Utilities.MAX_IMAGE_SIZE) {
                                            manualPage.add("image", "y");
                                            images.put("image" + page.toString(), jsonObject.getString("image"));
                                        } else {
                                            Toast.makeText(getContext(), Utilities.IMAGE_SIZE_EXCEEDED,
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    if (jsonObject.has("timer"))
                                        if (Integer.parseInt(jsonObject.getString("timer")) > 0)
                                            manualPage.add("timer", jsonObject.getString("timer"));
                                        else {
                                            Toast.makeText(getContext(), Utilities.INVALID_TIMER_VALUE,
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    if (jsonObject.has("yt_video"))
                                        if (!jsonObject.getString("yt_video").equals(""))
                                            manualPage.add("yt_video", jsonObject.getString("yt_video"));
                                        else {
                                            Toast.makeText(getContext(), Utilities.INVALID_VIDEO_ID,
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
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
                        } else {
                            Toast.makeText(getContext(), Utilities.INVALID_NUMPAGE_VALUE,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    else {
                        Toast.makeText(getContext(), Utilities.NO_NUMPAGE,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
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
        Query query = reference;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Utilities.showProgressDialog(getContext());
                if (snapshot.exists()) {
                    int id = 0;
                    for (DataSnapshot o : snapshot.getChildren()) {
                        System.out.println(o.getKey());
                        if (id<Integer.parseInt(o.getKey()))
                            id = Integer.parseInt(o.getKey());
                    }
                    lastId.append(id);

                } else
                    lastId.append(0);

                Integer id = Integer.parseInt(String.valueOf(lastId)) + 1;
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
                manual.setCategory(categoryLabel.get(spinner.getSelectedItem().toString()));
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