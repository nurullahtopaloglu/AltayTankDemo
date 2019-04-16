package com.uren.altaytankdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lai.library.ButtonStyle;
import com.uren.altaytankdemo.constants.StringConstants;
import com.uren.altaytankdemo.models.QrResponse;
import com.uren.altaytankdemo.utils.CustomUtil;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.uren.altaytankdemo.constants.NumericConstants.API_CALL_OK;
import static com.uren.altaytankdemo.constants.StringConstants.GENERATED_QR_CODE;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.loadingView)
    AVLoadingIndicatorView loadingView;
    @BindView(R.id.imgQr)
    ImageView imgQr;
    @BindView(R.id.btnGenerateQR)
    ButtonStyle btnGenerateQR;
    @BindView(R.id.btnScanQR)
    ButtonStyle btnScanQR;

    private String qrResponseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setListeners();

    }

    private void setListeners() {
        btnGenerateQR.setOnClickListener(this);
        btnScanQR.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view == btnGenerateQR) {
            new AsyncInsertData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (view == btnScanQR) {
            if (!qrResponseCode.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                intent.putExtra(GENERATED_QR_CODE, qrResponseCode);
                startActivity(intent);
            }
        }

    }

    private class AsyncInsertData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingView.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadingView.show();
            startProcess();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loadingView.hide();
        }

        private void startProcess() {

            String endpointUrl = "https://sandbox-api.payosy.com/api/get_qr_sale";

            OkHttpClient okHttpClient = new OkHttpClient();
            OkHttpClient client = trustAllSslClient(okHttpClient);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"totalReceiptAmount\":100}");
            Request request = new Request.Builder()
                    .url(endpointUrl)
                    .post(body)
                    .addHeader("x-ibm-client-id", StringConstants.CLIENT_ID)
                    .addHeader("x-ibm-client-secret", StringConstants.CLIENT_SECRET)
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/json")
                    .build();

            Response response = null;

            try {
                if (!CustomUtil.isNetworkConnected(MainActivity.this)) {
                    CustomUtil.networkErrorSnackBar(mainLayout, MainActivity.this);
                    return;
                }
                response = client.newCall(request).execute();
                checkResponse(response);
            } catch (Exception e) {
                showScanButton(false);
                showToast(e.toString());
                e.printStackTrace();
            }

        }


        private void checkResponse(Response response) {

            if (response.code() == API_CALL_OK) {
                setResponse(response);
            } else {
                showScanButton(false);
                try {
                    String responseText = response.body().string();
                    showToast(responseText);
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(e.toString());
                }
            }
        }


        private void setResponse(Response response) {

            try {
                String responseText = response.body().string();
                Gson gson = new Gson();
                QrResponse qrResponse = gson.fromJson(responseText, QrResponse.class);
                qrResponseCode = qrResponse.getQRdata();
                animateQrImage();
                //showToast(qrResponse.getQRdata());
                showScanButton(true);
            } catch (IOException e) {
                showScanButton(false);
                e.printStackTrace();
            }


        }

        private void animateQrImage() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // WORK on UI thread here
                    int cx = imgQr.getWidth() / 2;
                    int cy = imgQr.getHeight() / 2;
                    float finalRadius = (float) Math.hypot(cx, cy);

                    Animator animator = ViewAnimationUtils.createCircularReveal(imgQr, cx, cy, 0, finalRadius);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            imgQr.setImageResource(R.mipmap.img_qr);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });

                    animator.start();
                }
            });


        }

        private void showToast(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // WORK on UI thread here
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                }
            });
        }

        private void showScanButton(final boolean isShow) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // WORK on UI thread here
                    if (isShow) {
                        btnScanQR.setVisibility(View.VISIBLE);
                    } else {
                        btnScanQR.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }

    }

    /**
     * these variables/methods for jumping SSl Handshake certificate controls
     **/

    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };
    private static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    public static OkHttpClient trustAllSslClient(OkHttpClient client) {
        //log.warn("Using the trustAllSslClient is highly discouraged and should not be used in Production!");
        OkHttpClient.Builder builder = client.newBuilder();
        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return builder.build();
    }


}
