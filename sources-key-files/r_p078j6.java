package p078j6;

import D3.AbstractC0228n4;
import Y6.j;
import com.tit.imagegenerator.model.data.VideoGenerationResult;
import i7.C1331h;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public final class r implements Callback {

    /* JADX INFO: renamed from: b, reason: collision with root package name */
    public final /* synthetic */ int f15818b;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    public final /* synthetic */ C1331h f15819e;

    public /* synthetic */ r(C1331h c1331h, int i8) {
        this.f15818b = i8;
        this.f15819e = c1331h;
    }

    @Override // okhttp3.Callback
    public final void onFailure(Call call, IOException iOException) {
        switch (this.f15818b) {
            case 0:
                j.e(call, "call");
                j.e(iOException, "e");
                this.f15819e.d(AbstractC0228n4.a(iOException));
                break;
            default:
                j.e(call, "call");
                j.e(iOException, "e");
                this.f15819e.d(AbstractC0228n4.a(iOException));
                break;
        }
    }

    @Override // okhttp3.Callback
    public final void onResponse(Call call, Response response) {
        switch (this.f15818b) {
            case 0:
                C1331h c1331h = this.f15819e;
                j.e(call, "call");
                j.e(response, "response");
                try {
                    String strString = response.body().string();
                    response.code();
                    if (response.isSuccessful()) {
                        JSONObject jSONObject = new JSONObject(strString);
                        String strOptString = jSONObject.optString("status", "");
                        j.d(strOptString, "optString(...)");
                        String strOptString2 = jSONObject.optString("video_url");
                        j.b(strOptString2);
                        if (strOptString2.length() <= 0) {
                            strOptString2 = null;
                        }
                        String strOptString3 = jSONObject.optString("image_url");
                        j.b(strOptString3);
                        String str = strOptString3.length() > 0 ? strOptString3 : null;
                        int iOptInt = jSONObject.optInt("credits_remaining", 0);
                        String strOptString4 = jSONObject.optString("message", "");
                        j.d(strOptString4, "optString(...)");
                        c1331h.h(new VideoGenerationResult(strOptString, strOptString2, str, iOptInt, strOptString4), C1357i.f15793n);
                    } else {
                        c1331h.d(AbstractC0228n4.a(new IOException("HTTP " + response.code() + ": " + strString)));
                    }
                } catch (Exception e8) {
                    c1331h.d(AbstractC0228n4.a(e8));
                    return;
                }
                break;
            default:
                String strOptString5 = "You are blocked, please contact us";
                C1331h c1331h2 = this.f15819e;
                j.e(call, "call");
                j.e(response, "response");
                try {
                    String strString2 = response.body().string();
                    response.code();
                    if (response.code() == 403) {
                        try {
                            strOptString5 = new JSONObject(strString2).optString("detail", "You are blocked, please contact us");
                            break;
                        } catch (Exception unused) {
                        }
                        j.b(strOptString5);
                        c1331h2.d(AbstractC0228n4.a(new K(strOptString5)));
                    } else if (response.isSuccessful()) {
                        String strOptString6 = new JSONObject(strString2).optString("request_id", "");
                        j.b(strOptString6);
                        if (strOptString6.length() == 0) {
                            c1331h2.d(AbstractC0228n4.a(new IOException("No request_id in response: " + strString2)));
                        } else {
                            c1331h2.h(strOptString6, C1357i.f15789F);
                        }
                    } else {
                        c1331h2.d(AbstractC0228n4.a(new IOException("HTTP " + response.code() + ": " + strString2)));
                    }
                } catch (Exception e9) {
                    c1331h2.d(AbstractC0228n4.a(e9));
                    return;
                }
                break;
        }
    }
}
