package D3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import p065i.ActivityC1322j;
import p065i.DialogInterfaceC1320h;
import p078j6.ViewOnClickListenerC1355g;

/* JADX INFO: renamed from: D3.x6, reason: case insensitive filesystem */
/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractC0316x6 {
    public static final String a(Context context) {
        Y6.j.e(context, "context");
        String string = Settings.Secure.getString(context.getContentResolver(), "android_id");
        return string == null ? "" : string;
    }

    public static final String b(Context context, String str) throws IOException {
        Y6.j.e(str, "<this>");
        byte[] bArrDecode = Base64.decode(str, 0);
        Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length);
        File file = new File(context.getFilesDir(), "image_" + System.currentTimeMillis() + '_' + UUID.randomUUID() + ".jpg");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            bitmapDecodeByteArray.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            S4.a(fileOutputStream, null);
            file.getAbsolutePath();
            String absolutePath = file.getAbsolutePath();
            Y6.j.d(absolutePath, "getAbsolutePath(...)");
            return absolutePath;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                S4.a(fileOutputStream, th);
                throw th2;
            }
        }
    }

    public static final String c(Context context, String str) {
        Y6.j.e(str, "videoUrl");
        File file = new File(context.getFilesDir(), "video_" + System.currentTimeMillis() + '_' + UUID.randomUUID() + ".mp4");
        try {
            URL url = new URL(str);
            url.openConnection().connect();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[1024];
            while (true) {
                int i8 = bufferedInputStream.read(bArr);
                if (i8 == -1) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    bufferedInputStream.close();
                    file.getAbsolutePath();
                    String absolutePath = file.getAbsolutePath();
                    Y6.j.d(absolutePath, "getAbsolutePath(...)");
                    return absolutePath;
                }
                fileOutputStream.write(bArr, 0, i8);
            }
        } catch (Exception e8) {
            e8.printStackTrace();
            e8.getMessage();
            return "";
        }
    }

    public static final void d(ActivityC1322j activityC1322j) {
        A6.h(activityC1322j, activityC1322j.getColor(p006a6.h.white), activityC1322j.getColor(p006a6.h.white));
    }

    public static final void e(Context context) {
        Y6.j.e(context, "context");
        View viewInflate = LayoutInflater.from(context).inflate(p006a6.l.something_went_wrong_dialog, (ViewGroup) null);
        p029d4.b bVarF = new p029d4.b(context).f(viewInflate);
        bVarF.f15153a.k = true;
        DialogInterfaceC1320h dialogInterfaceC1320hCreate = bVarF.create();
        ((TextView) viewInflate.findViewById(p006a6.k.tvMessage)).setText(Html.fromHtml("Please Try Again in an <b>Hour</b>,<br>Select a Different Generation Model from", 0));
        ((MaterialButton) viewInflate.findViewById(p006a6.k.btnOk)).setOnClickListener(new ViewOnClickListenerC1355g(dialogInterfaceC1320hCreate, 0));
        dialogInterfaceC1320hCreate.show();
    }

    public static final J6.l f(String str) {
        List listJ = p048f7.y.J(str, new String[]{"x"});
        return new J6.l(Integer.valueOf(Integer.parseInt((String) listJ.get(0))), Integer.valueOf(Integer.parseInt((String) listJ.get(1))));
    }
}
