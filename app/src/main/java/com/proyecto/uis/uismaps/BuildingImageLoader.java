package com.proyecto.uis.uismaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.proyecto.uis.uismaps.Content.Alerts;

/**
 * Created by CheloReyes on 29/04/15.
 */
public class BuildingImageLoader {

    private Context iContext;
    private  Alerts alerts;
    private Bitmap image;
    private String oldUrl;

    public BuildingImageLoader(Context context) {
        iContext = context;
        alerts = new Alerts(iContext);
        image = null;
        oldUrl = null;
        initImageLoader(iContext);

    }
    private static void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(5 * 1024 * 1024);
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs();
        ImageLoader.getInstance().init(config.build());
    }

    public void showImageBuilding(String url) {
        final ProgressDialog dialog = new ProgressDialog(iContext);
        ImageLoader imageLoader = ImageLoader.getInstance();
        dialog.setIndeterminate(true);
        dialog.setMessage("Descargando...");
        if(image == null || !oldUrl.equals(url)){
            oldUrl = url;
            imageLoader.loadImage(url, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    dialog.show();
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    dialog.dismiss();
                    new Alerts(iContext).showAlertDialog("Error al descargar imágen", "No fue posible descargar la imagen seleccionada", "OK");
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    dialog.dismiss();
                    image = loadedImage;
                    alerts.imageDialog(image);
                }
            });
        }
        else {
            alerts.imageDialog(image);
        }
    }
}
