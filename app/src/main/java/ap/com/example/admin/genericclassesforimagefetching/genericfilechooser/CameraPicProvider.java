package ap.com.example.admin.genericclassesforimagefetching.genericfilechooser;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ap.com.example.admin.genericclassesforimagefetching.R;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * <p>Generic Plain java class which help to fetch image as a bitmap taking from camera
 * without the need to setting all the permission request call backs or onActivityresults
 * from the hosting activity.</p>
 * <p>
 */


public class CameraPicProvider {

    private Context context;
    private static GetBitmapListener listener;
    private static boolean wantToCrop;

    /**
     * <p>
     * Add the following dependency in app level build gradle
     * </p>
     * {@code
     * implementation 'com.theartofdev.edmodo:android-image-cropper:2.7.+'
     * }
     * <p>
     * <p>
     * Add the following line of code in the manifest under the application tag
     * </p>
     * {@code
     * <activity
     * android:name=".camerapicchooser.CameraPicProvider$PermissionAndResultReciverActivity"
     * android:theme="@style/Theme.AppCompat.Translucent"/>
     * }
     * <p>
     * {@code
     * <activity
     * android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
     * android:theme="@style/crop_imge_theme" />
     * }
     * </p>
     * <p>
     * Add the following permission in the manifest file
     * </p>
     * <p>
     * {@code
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.CAMERA" />
     * }
     * </p>
     *
     * <p>
     * Add the following line of code in the style.xml
     * </p>
     * <p>
     * {@code
     * <style name="Theme.AppCompat.Translucent" parent="Theme.AppCompat.NoActionBar">
     * <item name="android:background">#33000000</item> <!-- Or any transparency or color you need -->
     * <item name="android:windowNoTitle">true</item>
     * <item name="android:windowBackground">@android:color/transparent</item>
     * <item name="android:colorBackgroundCacheHint">@null</item>
     * <item name="android:windowIsTranslucent">true</item>
     * <item name="android:windowAnimationStyle">@android:style/Animation</item>
     * </style>
     * }
     * </p>
     *
     * @param context    activity context
     *                   </p><p>
     * @param wantToCrop if true,This provider class open an image cropper activity screen
     *                   where you cancrop the picked image.
     *                   </p><p>
     * @param listener   listener for getting the bitmap image and the file path
     *                   </p>
     */

    public CameraPicProvider(Context context, boolean wantToCrop, GetBitmapListener listener) {
        this.context = context;
        this.listener = listener;
        this.wantToCrop = wantToCrop;
        openPermissionAndResultReciver();
    }

    private void openPermissionAndResultReciver() {
        Intent intent = new Intent(context, PermissionAndResultReciverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * <p>A static inner activity which is not visible to user its main pupose is to recive</p>
     * {@link #onRequestPermissionsResult(int, String[], int[])}
     * <p>and</p>
     * {@link #onActivityResult(int, int, Intent)}
     * <p>
     * <p>
     * it fetches the image bitmap internally and callback to the listener
     * </p>
     */

    public static class PermissionAndResultReciverActivity extends AppCompatActivity {
        public static final int SELECT_FILE = 0;
        private static final int REQUEST_FOR_EXTERNAL_STORAGE = 1001;
        public static final int REQUEST_FOR_CAMERA = 1002;
        private int REQUEST_CAMERA = 0;
        private Bitmap bitmapImage;
        private String mCurrentPhotoPath;
        private Uri file;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestPermissionForExternalStorage();
        }

        public boolean requestPermissionForExternalStorage() {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
// explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_FOR_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_FOR_EXTERNAL_STORAGE);
                }
                return true;
            } else {
                requestPermissionForCamera();
                return false;
            }
        }

        public boolean requestPermissionForCamera() {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
// explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.CAMERA)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA},
                            REQUEST_FOR_CAMERA);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA},
                            REQUEST_FOR_CAMERA);
                }
                return true;
            } else {
                cameraIntent();
                return false;
            }
        }

        private void cameraIntent() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //for nougat
            if (Build.VERSION.SDK_INT >= 24) {
                File nFile = getFile();
                file = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider", nFile);
            } else {
                //getting uri of the file
                file = Uri.fromFile(getFile());
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, file);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CAMERA);
            } else {
                Toast.makeText(this, "There is no Camera Application found", Toast.LENGTH_SHORT).show();
            }
        }

        private File getFile() {

            File folder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);// the file path

            //if it doesn't exist the folder will be created
            if (!folder.exists()) {
                folder.mkdir();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File image_file = null;

            try {
                image_file = File.createTempFile(imageFileName, ".jpeg", folder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCurrentPhotoPath = image_file.getAbsolutePath();
            return image_file;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_FOR_EXTERNAL_STORAGE: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                        requestPermissionForCamera();
                    } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case REQUEST_FOR_CAMERA: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                        cameraIntent();
                    } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }


            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_CAMERA) {
                    onCaptureImageResult(data);
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        getImageFromCropActivity(result);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        finish();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Capturing cancel", Toast.LENGTH_SHORT);
                finish();
            }


        }

        private void onCaptureImageResult(Intent data) {
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                if (bitmapImage == null) {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                }
                if (!wantToCrop) {
                    listener.onGetBitmap(bitmapImage, mCurrentPhotoPath);
                    finish();
                } else {
                    showImageCroperActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }


        }

        private void showImageCroperActivity() {
            CropImage.activity(file)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setActivityMenuIconColor(getResources().getColor(R.color.colorWhite))
                    .setBorderCornerColor(getResources().getColor(R.color.colorAccent))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        private void getImageFromCropActivity(CropImage.ActivityResult result) {
            if (result != null) {
                try {
                    Uri selectedImage = result.getUri();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    listener.onGetBitmap(bitmapImage, mCurrentPhotoPath);
                    this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.finish();
                }
            } else {
                finish();
            }

        }


    }


    public interface GetBitmapListener {

        void onGetBitmap(Bitmap bitmapImage, String filepath);
    }

}
