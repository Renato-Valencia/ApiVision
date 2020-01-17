package com.example.apivision;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.R.layout;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    public Vision vision;
    ImageView imagencargar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imagencargar=(ImageView)findViewById(R.id.imageView2);

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyCV_ADpVQp5_K1CQ98gc6KeOVq5p1sjqKQ"));
        vision = visionBuilder.build();
    }


    public void cargarImagen(View v){
        Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent,"Seleccione la ruta"),10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Uri path =data.getData();
            imagencargar.setImageURI(path);
        }
    }

    public void buttonclic(View v){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ImageView imagen=(ImageView)findViewById(R.id.imageView2);

                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                bitmap=scaleBitmapDown(bitmap,1200);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();


                //Paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //Paso 2
                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");

                //Paso 3
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //Paso 4
                try {
                    Vision.Images.Annotate annotateRequest=vision.images().annotate(batchRequest);
                    //Paso 5 Enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse batchResponse = annotateRequest.execute();

                    //Paso 6
                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();

                    //Paso 7
                    final String result=text.getText();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.textView2);
                            imageDetail.setText(result);
                        }
                    });
                }catch (IOException e){e.printStackTrace();}
            }
        });
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }



    public void buttonclicFace(View v){
        AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
            ImageView imagen=(ImageView)findViewById(R.id.imageView2);

            BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            bitmap=scaleBitmapDown(bitmap,1200);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageInByte = stream.toByteArray();


            //Paso 1
            Image inputImage = new Image();
            inputImage.encodeContent(imageInByte);

            //Paso 2
            Feature desiredFeature = new Feature();
            desiredFeature.setType("FACE_DETECTION");

            //Paso 3
            AnnotateImageRequest request = new AnnotateImageRequest();
            request.setImage(inputImage);
            request.setFeatures(Arrays.asList(desiredFeature));
            BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
            batchRequest.setRequests(Arrays.asList(request));

            //Paso 4
            try {
                Vision.Images.Annotate annotateRequest=vision.images().annotate(batchRequest);
                //Paso 5 Enviamos la solicitud
                annotateRequest.setDisableGZipContent(true);
                BatchAnnotateImagesResponse batchResponse = annotateRequest.execute();

                //Paso 6
                List<FaceAnnotation> faces = batchResponse.getResponses().get(0).getFaceAnnotations();
                int numberOfFaces = faces.size();
                String likelihoods = "";
                for(int i=0; i<numberOfFaces; i++) {
                    likelihoods += "\n It is " + faces.get(i).getJoyLikelihood() +
                            " that face " + i + " is happy";
                }

                //Paso 7
                final String message = "This photo has " + numberOfFaces + " faces" + likelihoods;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView imageDetail = (TextView)findViewById(R.id.textView2);
                        imageDetail.setText(message);
                    }
                });
            }catch (IOException e){e.printStackTrace();}
        }
    });
    }


    public void buttonclicObjeto(View v){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ImageView imagen=(ImageView)findViewById(R.id.imageView2);

                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                bitmap=scaleBitmapDown(bitmap,1200);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();


                //Paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //Paso 2
                Feature desiredFeature = new Feature();
                desiredFeature.setType("LABEL_DETECTION");

                //Paso 3
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //Paso 4
                try {
                    Vision.Images.Annotate annotateRequest=vision.images().annotate(batchRequest);
                    //Paso 5 Enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse batchResponse = annotateRequest.execute();

                    //Paso 6
                    StringBuilder message = new StringBuilder("I found these things:\n\n");
                    List<EntityAnnotation> labels = batchResponse.getResponses().get(0).getLabelAnnotations();
                    if (labels != null) {
                        for (EntityAnnotation label : labels) {
                            message.append(String.format(Locale.US, "%.3f: %s",label.getScore(), label.getDescription()));
                            message.append("\n");
                        }
                    } else {
                        message.append("nothing");
                    }
                    //Paso 7

                    final String mensaje=message.toString();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.textView2);
                            imageDetail.setText(mensaje);
                        }
                    });
                }catch (IOException e){e.printStackTrace();}
            }
        });
    }
}
