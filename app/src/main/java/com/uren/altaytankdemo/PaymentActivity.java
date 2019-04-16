package com.uren.altaytankdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lai.library.ButtonStyle;
import com.uren.altaytankdemo.constants.StringConstants;
import com.uren.altaytankdemo.models.PaymentResponse;
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


public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.llContent)
    LinearLayout llContent;
    @BindView(R.id.llLoading)
    LinearLayout llLoading;
    @BindView(R.id.loadingView)
    AVLoadingIndicatorView loadingView;
    @BindView(R.id.btnVerifyPayment)
    ButtonStyle btnVerifyPayment;

    private String qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);

        qrCode = (String) getIntent().getStringExtra(GENERATED_QR_CODE);

        setListeners();

    }

    private void setListeners() {
        llLoading.setVisibility(View.GONE);
        btnVerifyPayment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view == btnVerifyPayment) {
            new AsyncInsertData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        }

    }

    private class AsyncInsertData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            llLoading.setVisibility(View.VISIBLE);
            loadingView.show();
            btnVerifyPayment.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            llLoading.setVisibility(View.VISIBLE);
            loadingView.show();
            btnVerifyPayment.setClickable(false);
            startProcess();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            llLoading.setVisibility(View.INVISIBLE);
            loadingView.hide();
            btnVerifyPayment.setClickable(true);
        }

        private void startProcess() {

            String endpointUrl = "https://sandbox-api.payosy.com/api/payment";

            OkHttpClient okHttpClient = new OkHttpClient();
            OkHttpClient client = trustAllSslClient(okHttpClient);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"returnCode\":1000,\"returnDesc\":\"success\",\"receiptMsgCustomer\":\"beko Campaign\",\"receiptMsgMerchant\":\"beko Campaign Merchant\",\"paymentInfoList\":[{\"paymentProcessorID\":67,\"paymentActionList\":[{\"paymentType\":3,\"amount\":100,\"currencyID\":949,\"vatRate\":800}]}],\"QRdata\":\"" + qrCode + "\"}");
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
                if (!CustomUtil.isNetworkConnected(PaymentActivity.this)) {
                    CustomUtil.networkErrorSnackBar(mainLayout, PaymentActivity.this);
                    return;
                }
                response = client.newCall(request).execute();
                checkResponse(response);
            } catch (Exception e) {
                showToast(e.toString());
                e.printStackTrace();
            }

        }


        private void checkResponse(Response response) {

            if (response.code() == API_CALL_OK) {
                setResponse(response);
            } else {
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
                PaymentResponse paymentResponse = gson.fromJson(responseText, PaymentResponse.class);
                showToast(paymentResponse.getReturnDesc());

                if (paymentResponse.getReturnCode() == 1000) {
                    Intent intent = new Intent(PaymentActivity.this, SuccessActivity.class);
                    startActivity(intent);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        private void showToast(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // WORK on UI thread here
                    Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_SHORT).show();

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
