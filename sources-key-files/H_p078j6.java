package p078j6;

import O6.a;
import O6.d;
import P6.i;
import Y6.j;
import i7.C1331h;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import p048f7.y;

/* JADX INFO: loaded from: classes2.dex */
public final class H {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static final H f15649a = new H();

    /* JADX INFO: renamed from: b, reason: collision with root package name */
    public static final OkHttpClient f15650b;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        TimeUnit timeUnit = TimeUnit.SECONDS;
        f15650b = builder.connectTimeout(80L, timeUnit).readTimeout(80L, timeUnit).writeTimeout(80L, timeUnit).callTimeout(80L, timeUnit).build();
    }

    public static Object a(String str, String str2, G g8) {
        C1331h c1331h = new C1331h(1, d.a(g8));
        c1331h.u();
        f15650b.newCall(new Request.Builder().url("https://techinfortainment.com/ai-generation/result?request_id=" + str + "&app_name=" + str2).get().addHeader("Accept", "application/json").build()).enqueue(new C1360l(c1331h, 1));
        Object objT = c1331h.t();
        a aVar = a.f6341b;
        return objT;
    }

    public static String c(String str) {
        String string = y.N(str).toString();
        if (j.a(string, "Flux")) {
            return "Flux1schnell";
        }
        j.a(string, "Text2Image");
        return "ZImageTurbo_INT8";
    }

    public static Object d(String str, String str2, String str3, int i8, int i9, String str4, String str5, String str6, i iVar) throws JSONException {
        C1331h c1331h = new C1331h(1, d.a(iVar));
        c1331h.u();
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("prompt", str);
        jSONObject.put("original_prompt", str2);
        jSONObject.put("model", str3);
        jSONObject.put("width", i8);
        jSONObject.put("height", i9);
        jSONObject.put("steps", 8);
        jSONObject.put("guidance", 7.5d);
        jSONObject.put("negative_prompt", "");
        jSONObject.put("device_id", str4);
        jSONObject.put("user_type", str5);
        jSONObject.put("version", "3");
        jSONObject.put("app_name", str6);
        RequestBody.Companion companion = RequestBody.Companion;
        String string = jSONObject.toString();
        j.d(string, "toString(...)");
        Request requestBuild = new Request.Builder().url("https://techinfortainment.com/ai-generation/generate/text2image").post(companion.create(string, MediaType.Companion.get("application/json; charset=utf-8"))).addHeader("Accept", "application/json").build();
        jSONObject.toString();
        f15650b.newCall(requestBuild).enqueue(new C1362n(c1331h, 1));
        Object objT = c1331h.t();
        a aVar = a.f6341b;
        return objT;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:21:0x0047 A[PHI: r9 r10
  0x0047: PHI (r9v4 java.lang.String) = 
  (r9v0 java.lang.String)
  (r9v7 java.lang.String)
  (r9v7 java.lang.String)
  (r9v7 java.lang.String)
  (r9v7 java.lang.String)
  (r9v7 java.lang.String)
  (r9v7 java.lang.String)
 binds: [B:20:0x0044, B:30:0x007b, B:45:0x00a9, B:42:0x00a0, B:39:0x0097, B:36:0x008e, B:33:0x0085] A[DONT_GENERATE, DONT_INLINE]
  0x0047: PHI (r10v2 java.lang.String) = 
  (r10v0 java.lang.String)
  (r10v5 java.lang.String)
  (r10v5 java.lang.String)
  (r10v5 java.lang.String)
  (r10v5 java.lang.String)
  (r10v5 java.lang.String)
  (r10v5 java.lang.String)
 binds: [B:20:0x0044, B:30:0x007b, B:45:0x00a9, B:42:0x00a0, B:39:0x0097, B:36:0x008e, B:33:0x0085] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:23:0x005d A[RETURN] */
    /* JADX WARN: Code duplicated, block: B:24:0x005e  */
    /* JADX WARN: Code duplicated, block: B:27:0x006d A[RETURN] */
    /* JADX WARN: Code duplicated, block: B:32:0x007f  */
    /* JADX WARN: Code duplicated, block: B:35:0x0088  */
    /* JADX WARN: Code duplicated, block: B:38:0x0091  */
    /* JADX WARN: Code duplicated, block: B:41:0x009a  */
    /* JADX WARN: Code duplicated, block: B:44:0x00a3  */
    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:26:0x006b -> B:14:0x002d). Please report as a decompilation issue!!! */
    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:59)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:19)
        */
    public final java.lang.Object b(java.lang.String r9, java.lang.String r10, P6.c r11) {
        /*
            r8 = this;
            boolean r0 = r11 instanceof p078j6.G
            if (r0 == 0) goto L13
            r0 = r11
            j6.G r0 = (p078j6.G) r0
            int r1 = r0.f15648u
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r3 = r1 & r2
            if (r3 == 0) goto L13
            int r1 = r1 - r2
            r0.f15648u = r1
            goto L18
        L13:
            j6.G r0 = new j6.G
            r0.<init>(r8, r11)
        L18:
            java.lang.Object r11 = r0.f15646n
            O6.a r1 = O6.a.f6341b
            int r2 = r0.f15648u
            r3 = 2
            r4 = 1
            if (r2 == 0) goto L44
            if (r2 == r4) goto L3c
            if (r2 != r3) goto L34
            java.lang.String r9 = r0.f15645m
            java.lang.String r10 = r0.j
            D3.AbstractC0228n4.b(r11)     // Catch: java.lang.Exception -> L31
        L2d:
            r7 = r10
            r10 = r9
            r9 = r7
            goto L6e
        L31:
            r9 = move-exception
            goto Lad
        L34:
            java.lang.IllegalStateException r9 = new java.lang.IllegalStateException
            java.lang.String r10 = "call to 'resume' before 'invoke' with coroutine"
            r9.<init>(r10)
            throw r9
        L3c:
            java.lang.String r9 = r0.f15645m
            java.lang.String r10 = r0.j
            D3.AbstractC0228n4.b(r11)
            goto L61
        L44:
            D3.AbstractC0228n4.b(r11)
        L47:
            int r11 = g7.a.j
            r11 = 3000(0xbb8, float:4.204E-42)
            g7.c r2 = g7.c.MILLISECONDS
            long r5 = D3.AbstractC0239o6.f(r11, r2)
            r0.j = r9
            r0.f15645m = r10
            r0.f15648u = r4
            java.lang.Object r11 = i7.AbstractC1347y.h(r5, r0)
            if (r11 != r1) goto L5e
            return r1
        L5e:
            r7 = r10
            r10 = r9
            r9 = r7
        L61:
            r0.j = r10     // Catch: java.lang.Exception -> L31
            r0.f15645m = r9     // Catch: java.lang.Exception -> L31
            r0.f15648u = r3     // Catch: java.lang.Exception -> L31
            java.lang.Object r11 = a(r10, r9, r0)     // Catch: java.lang.Exception -> L31
            if (r11 != r1) goto L2d
            return r1
        L6e:
            com.tit.imagegenerator.model.data.ImageGenerationResult r11 = (com.tit.imagegenerator.model.data.ImageGenerationResult) r11     // Catch: java.lang.Exception -> L31
            r11.getStatus()
            java.lang.String r2 = r11.getStatus()
            int r5 = r2.hashCode()
            switch(r5) {
                case -1402931637: goto La3;
                case -1309235419: goto L9a;
                case -1281977283: goto L91;
                case -984725445: goto L88;
                case -21437972: goto L7f;
                default: goto L7e;
            }
        L7e:
            goto L47
        L7f:
            java.lang.String r5 = "blocked"
            boolean r2 = r2.equals(r5)
            if (r2 != 0) goto Lac
            goto L47
        L88:
            java.lang.String r5 = "ad_required"
            boolean r2 = r2.equals(r5)
            if (r2 != 0) goto Lac
            goto L47
        L91:
            java.lang.String r5 = "failed"
            boolean r2 = r2.equals(r5)
            if (r2 != 0) goto Lac
            goto L47
        L9a:
            java.lang.String r5 = "expired"
            boolean r2 = r2.equals(r5)
            if (r2 != 0) goto Lac
            goto L47
        La3:
            java.lang.String r5 = "completed"
            boolean r2 = r2.equals(r5)
            if (r2 != 0) goto Lac
            goto L47
        Lac:
            return r11
        Lad:
            r9.getMessage()
            r9 = 0
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: p078j6.H.b(java.lang.String, java.lang.String, P6.c):java.lang.Object");
    }
}
