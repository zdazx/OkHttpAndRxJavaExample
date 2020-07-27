package com.thoughtworks.okhttpandrxjavaexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getDataBtn = findViewById(R.id.get_data_btn);
        getDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
    }

    private void getData() {

        Observable.create(new ObservableOnSubscribe<Response>() {
            @Override
            public void subscribe(ObservableEmitter<Response> emitter) throws Exception {
                getInternetData(emitter);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response response) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            showFirstPersonName(body);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "get data failed!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void showFirstPersonName(ResponseBody body) {
        try {
            String wrapperJson = body.string();
            Wrapper wrapper = new Gson().fromJson(wrapperJson, Wrapper.class);
            ArrayList<Person> people = wrapper.getData();
            if (people != null && people.size() > 0) {
                Toast.makeText(getApplicationContext(), people.get(0).getName(), Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getInternetData(ObservableEmitter<Response> emitter) throws IOException {
        final String url = "https://twc-android-bootcamp.github.io/fake-data/data/default.json";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            if (!emitter.isDisposed()) {
                emitter.onNext(response);
            }
            emitter.onComplete();
        } else if (!response.isSuccessful() && !emitter.isDisposed()) {
            emitter.onError(new Exception("error"));
        }

    }
}