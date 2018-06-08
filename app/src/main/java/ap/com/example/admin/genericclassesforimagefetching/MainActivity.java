package ap.com.example.admin.genericclassesforimagefetching;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ap.com.example.admin.genericclassesforimagefetching.genericfilechooser.CameraPicProvider;
import ap.com.example.admin.genericclassesforimagefetching.genericfilechooser.SdCardPicProvider;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
    }

    public void openCamera(View view) {
        new CameraPicProvider(this, true, new CameraPicProvider.GetBitmapListener() {
            @Override
            public void onGetBitmap(Bitmap bitmapImage, String filepath) {
                imageView.setImageBitmap(bitmapImage);
            }
        });
    }

    public void openSd(View view) {
        new SdCardPicProvider(this, false, new SdCardPicProvider.GetBitmapListener() {
            @Override
            public void onGetBitmap(Bitmap bitmap, String imagePath) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }
}
