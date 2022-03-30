package com.example.ms_health;
//현재 : 거리 측정이 반복으로 안되고 종료시에만 직선거리가 측정되는듯함.

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private MapView mapView;
    private TextView total; //총 이동거리 띄울 텍스트 뷰
    private Button start, stop;
    private double beforelat, beforelon; //과거 위치
    private double nowlat, nowlon; //현재 위치
    private static double distance, distance2; // 과거와 현재 위치에 따른 거리 합
    private double lat, lon; //위도 경도
    private boolean check; // 시작 = true, 종료 = false
    private TextView cal;
    private TextView weight;
    Button user;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cal = (TextView)findViewById(R.id.cal);
        weight = (TextView) findViewById(R.id.Weight);
        total = (TextView)findViewById(R.id.total);
        start = (Button)findViewById(R.id.start);
        stop = (Button)findViewById(R.id.stop);
        user = (Button)findViewById(R.id.user);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        

        mapView.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        //시작 구문
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weight.getText().toString() == "") {
                    Toast.makeText(getApplicationContext(), "사용자 정보에서 체중을 입력하세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "앱 실행 중에 종료하지마세요.", Toast.LENGTH_LONG).show();
                    onMapReady(naverMap);

                    distance = 0;
                    check = true;
                    nowlat = locationSource.getLastLocation().getLatitude();
                    nowlon = locationSource.getLastLocation().getLongitude();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (check) {
                                //종료 버튼이 눌리기 전까지 거리를 계속 더해줌
                                beforelat = nowlat;
                                beforelon = nowlon; // 현재 값이었던 좌표값을 이전 좌표값으로 넣기
                                nowlat = locationSource.getLastLocation().getLatitude();
                                nowlon = locationSource.getLastLocation().getLongitude();
//
                                distance = distance + getDistance(nowlat, nowlon, beforelat, beforelon);
                                distance2 = Math.ceil(distance * 100 / 100.0);
                                handler.post(new Runnable() {
                                    public void run() {
                                        total.setText(distance2 + "m");
                                        kcal();
                                    }

                                });
                                try {
                                    Thread.sleep(1000); // 10초마다 반복
                                } catch (Exception e) {
                                }
                            }
                        }
                    }).start();
                }
            }
        });

        //종료 구문
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check = false;
                distance = distance + getDistance(nowlat, nowlon, beforelat, beforelon);
                distance = Math.round(distance2 * 100 )/ 100.0;
                total.setText(distance2 + "m");
                distance = 0;
            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivityForResult(intent, 101);

            }
        });

    }

    @Override
    public void onMapReady(NaverMap naverMap) {
         this.naverMap = naverMap;
         naverMap.setLocationSource(locationSource);
         naverMap.setLocationTrackingMode(LocationTrackingMode.Face);

         naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
             @Override
             public void onLocationChange(Location location) {
                 lat = location.getLatitude();
                 lon = location.getLongitude(); //위도 경도 받아오기
             }
         });

    }

    public void onRequestPermissonsResult(int requestCode, String[] permissions, int[] grantResult){
        if(locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResult)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResult);
    }


    //아래 메서드들은 두 좌표간 거리를 구하는 메서드들
    public double getDistance(double nowlat, double nowlon, double beforelat, double beforelon){
        double theta;
        double dist;
        if((nowlat == beforelat) && (nowlon == beforelon)){
            return 0;
        }
        else {
            theta = nowlon - beforelon;
            dist = Math.sin(deg2rad(nowlat)) * Math.sin(deg2rad(beforelat)) + Math.cos(deg2rad(nowlat)) * Math.cos(deg2rad(beforelat)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1609.344;

            return dist;
        }
    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    //아래는 1m당 소모한 칼로리 계산 식
    public void kcal(){
        double kcal =  Integer.parseInt(weight.getText().toString());
        kcal = Math.round(kcal * 0.6 * 0.001 * distance * 100) / 100.0;
        String kcal2 = Double.toString(kcal);
        cal.setText(kcal2);

        return;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && data != null){
                weight.setText(data.getStringExtra("Weight"));
        }else if(requestCode == 101 && data  == null){
                weight.setText("");
        }
    }

}
