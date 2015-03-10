package com.proyecto.uis.uismaps;

import android.content.Context;
import android.view.View;

/**
 * Created by cheloreyes on 9/03/15.
 */
public class ContentManager extends View {

    private Context miContext;
    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public ContentManager(Context context) {
        super(context);
        miContext = context;
    }
}
