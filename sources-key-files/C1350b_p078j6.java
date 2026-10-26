package p078j6;

import B3.o;
import D3.AbstractC0316x6;
import U.C0967j;
import X6.a;
import X6.l;
import Y6.j;
import android.content.Context;
import androidx.appcompat.widget.C0983a;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import p041e7.m;
import p043f2.b;
import p056g6.g;
import p075j2.d;

/* JADX INFO: renamed from: j6.b, reason: case insensitive filesystem */
/* JADX INFO: loaded from: classes2.dex */
public final class C1350b {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static String f15675a = "";

    /* JADX INFO: renamed from: b, reason: collision with root package name */
    public static boolean f15676b;

    /* JADX INFO: renamed from: c, reason: collision with root package name */
    public static boolean f15677c;

    public static void a(Context context, l lVar, l lVar2) {
        j.e(context, "context");
        String strA = AbstractC0316x6.a(context);
        StringBuilder sbM = b.m("https://techinfortainment.com/ai-generation/users/", strA, "/credits?app_name=");
        sbM.append(AbstractC1353e.P);
        new OkHttpClient().newCall(new Request.Builder().url(sbM.toString()).addHeader("Content-Type", "application/json").get().build()).enqueue(new C0967j(lVar2, strA, lVar, 19));
    }

    public static void b(g gVar) {
        if (f15676b) {
            return;
        }
        f15676b = true;
        HttpUrl httpUrlBuild = HttpUrl.Companion.get("https://techinfortainment.com/ai-generation/suggestions/all").newBuilder().addQueryParameter("appName", AbstractC1353e.P).build();
        Objects.toString(httpUrlBuild);
        int i8 = gVar.f14628a.getInt("IMAGE_CATE_KEY", 2);
        boolean z8 = true ^ gVar.f14628a.getBoolean("IS_USER_WHITELISTED", false);
        new OkHttpClient().newCall(new Request.Builder().url(httpUrlBuild).get().build()).enqueue(new C0983a(i8, z8, gVar));
    }

    public static void c(Context context, boolean z8, boolean z9, l lVar) {
        j.e(context, "context");
        if (f15677c) {
            lVar.g(Boolean.FALSE);
            return;
        }
        f15677c = true;
        HttpUrl.Builder builderAddQueryParameter = HttpUrl.Companion.get("https://techinfortainment.com/ai-generation/live_generation").newBuilder().addQueryParameter("refresh", String.valueOf(z8));
        if (!z8 && f15675a.length() > 0 && !f15675a.equals("null")) {
            builderAddQueryParameter.addQueryParameter("cursor", f15675a);
        }
        Objects.toString(builderAddQueryParameter.build());
        Call callNewCall = new OkHttpClient().newCall(new Request.Builder().url(builderAddQueryParameter.build()).addHeader("device-id", AbstractC0316x6.a(context)).addHeader("X-Is-Whitelisted", String.valueOf(z9)).get().build());
        o oVar = new o();
        oVar.f376b = z8;
        oVar.f377e = lVar;
        callNewCall.enqueue(oVar);
    }

    public static void d(Context context, boolean z8) {
        F0.b bVar = new F0.b(4);
        j.e(context, "context");
        c(context, false, z8, new m(1, bVar));
    }

    public static void e(String str, String str2, String str3, String str4, a aVar, a aVar2) throws JSONException {
        j.e(str, "deviceId");
        j.e(str3, "purchaseToken");
        j.e(str4, "productId");
        j.e(aVar, "onSuccess");
        j.e(aVar2, "onFailure");
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("device_id", str);
        jSONObject.put("app_name", AbstractC1353e.P);
        jSONObject.put("user_type", str2);
        jSONObject.put("purchase_token", str3);
        jSONObject.put("product_id", str4);
        RequestBody.Companion companion = RequestBody.Companion;
        String string = jSONObject.toString();
        j.d(string, "toString(...)");
        okHttpClient.newCall(new Request.Builder().url("https://techinfortainment.com/ai-generation/users/register").post(companion.create(string, MediaType.Companion.get("application/json; charset=utf-8"))).build()).enqueue(new d(3, aVar2, aVar));
    }
}
