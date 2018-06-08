package ap.com.example.admin.genericclassesforimagefetching.genericfilechooser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

import ap.com.example.admin.genericclassesforimagefetching.R;

public class SdCardPicProvider {
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
     * <activity android:name=".sdcardpichooser.SdCardPicProvider$ResultReciverActivity"
     * android:theme="@style/Theme.AppCompat.Translucent"
     * />
     * }
     * <p>
     * {@code
     * <activity
     * android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
     * android:theme="@style/crop_imge_theme" />
     * }
     * </p>
     * <p>
     * {@code
     * <provider
     * android:name="android.support.v4.content.FileProvider"
     * android:authorities="com.encoders.eva.eva.provider"
     * android:exported="false"
     * android:grantUriPermissions="true">
     * <meta-data
     * android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/provider_paths" />
     * </provider>
     * }
     * </p>
     * <p>
     * add a xml file named provider_path under xml folder in res folder,if not existed create one
     * </p>
     * <p>
     * {@code
     * <paths xmlns:android="http://schemas.android.com/apk/res/android">
     * <external-path name="external_files" path="."/>
     * </paths>
     * }
     * </p>
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

    public SdCardPicProvider(Context context, boolean wantToCrop, GetBitmapListener listener) {
        this.context = context;
        this.listener = listener;
        this.wantToCrop = wantToCrop;
        openPermissionAndResultReciver();
    }

    public void openPermissionAndResultReciver() {
        Intent intent = new Intent(context, ResultReciverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static class ResultReciverActivity extends AppCompatActivity {

        public static final int SELECT_FILE = 0;
        private Bitmap bitmapImage;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            showFileChooser();
        }

        private void showFileChooser() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE) {
                    onSelectFromGalleryResult(data);
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        getImageFromCropActivity(result);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        error.printStackTrace();
                        finish();
                    }
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "image selection cancel", Toast.LENGTH_SHORT).show();
                finish();
            }


        }

        private void getImageFromCropActivity(CropImage.ActivityResult result) {
            if (result != null) {
                try {
                    Uri selectedImage = result.getUri();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    listener.onGetBitmap(bitmapImage, "image.jpg");
                    this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.finish();
                }
            } else {
                finish();
            }

        }

        private void onSelectFromGalleryResult(Intent data) {
            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    if (!wantToCrop) {
                        listener.onGetBitmap(bitmapImage, "image.jpg");
                        finish();
                    } else {
                        showImageCroperActivity(data);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }

        private void showImageCroperActivity(Intent data) {

            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    CropImage.activity(selectedImage)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setActivityMenuIconColor(getResources().getColor(R.color.colorWhite))
                            .setBorderCornerColor(getResources().getColor(R.color.colorAccent))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public interface GetBitmapListener {
        void onGetBitmap(Bitmap bitmap, String imagePath);
    }


}
