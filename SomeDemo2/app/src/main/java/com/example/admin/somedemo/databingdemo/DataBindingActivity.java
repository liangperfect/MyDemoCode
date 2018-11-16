package com.example.admin.somedemo.databingdemo;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.admin.somedemo.BR;
import com.example.admin.somedemo.R;
import com.example.admin.somedemo.databinding.ActivityDataBindingBinding;

import java.util.Random;

public class DataBindingActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityDataBindingBinding bindingBinding;
    private Goods mGoods;
    private Button btnChangeName, btnChangeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_data_binding);
        bindingBinding = DataBindingUtil.setContentView(this, R.layout.activity_data_binding);

        initView();
        mGoods = new Goods("fanqie", "color hong", 1.0f);
        mGoods.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {

                if (propertyId == BR.name) {
                    Log.d("chenliang", "change br name");
                } else if (propertyId == BR.details) {
                    Log.d("chenliang", "change br details");
                } else if (propertyId == BR._all) {
                    Log.d("chenliang", "change br all");
                } else {
                    Log.d("chenliang", "change br add");
                }
//                switch (propertyId) {
//                    case BR.name:
//                        break;
//
//
//                }
            }
        });
        User user = new User("Chen", "Liang");
        bindingBinding.setUser(user);
        bindingBinding.setTempData("临时的数据");
        bindingBinding.setGoods(mGoods);
        bindingBinding.btnBindingdata.setOnClickListener(this);
    }

    private void initView() {
        btnChangeName = findViewById(R.id.btn_changeName);
        btnChangeDetails = findViewById(R.id.btn_changeDetails);
        btnChangeName.setOnClickListener(this);
        btnChangeDetails.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bindingdata:
                User user = new User("xiao", "mei");
                bindingBinding.setUser(user);
                break;

            case R.id.btn_changeName:
                Log.d("chenliang", "goodDetailBtnClick()");
                mGoods.setName("code" + new Random().nextInt(100));
                mGoods.setPrice(new Random().nextInt(100));
                break;

            case R.id.btn_changeDetails:
                mGoods.setDetails("hi" + new Random().nextInt(100));
                mGoods.setPrice(new Random().nextInt(100));
                break;
        }
    }



    public class GoodEventHandler {
        public void goodDetailBtnClick() {
            Log.d("chenliang", "goodDetailBtnClick()");
            mGoods.setDetails("hi" + new Random().nextInt(100));
            mGoods.setPrice(new Random().nextInt(100));
        }

        public void goodNameBtnClick() {
            Log.d("chenliang", "goodNameBtnClick()");
            mGoods.setName("code" + new Random().nextInt(100));
            mGoods.setPrice(new Random().nextInt(100));
        }
    }


}
