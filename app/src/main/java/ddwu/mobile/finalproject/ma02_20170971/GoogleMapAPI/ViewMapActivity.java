package ddwu.mobile.finalproject.ma02_20170971.GoogleMapAPI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ddwu.mobile.finalproject.ma02_20170971.BreadReview.PostDto;
import ddwu.mobile.finalproject.ma02_20170971.R;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

// 현재 위치 검색 가능, 특정 위치 검색 가능, 마커 클릭 시 가게 정보 보여줌
// 마커 윈도우 클릭시 액티비티 이동, 재구매 의사에 따라서 마커 색깔 구분
public class ViewMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    final static String TAG = "ViewMapActivity";
    final static int PERMISSION_REQ_CODE = 100;

    private EditText editText; // 검색창에 입력한 주소
    private String bestProvider;
    private LocationManager locManager;
    private LatLng latLng;
    private String lat;
    private String lng;
    private LatLngResultReceiver latLngResultReceiver;

    /*UI*/
    private GoogleMap mGoogleMap;
    private MarkerOptions markerOptions;

    /*DATA*/
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        editText = (EditText)findViewById(R.id.et_addr);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        bestProvider = LocationManager.PASSIVE_PROVIDER;
        latLngResultReceiver = new LatLngResultReceiver(new Handler());

        // 권한 확인 후 권한이 있을 경우 맵 로딩 실행
        if (checkPermission()) {
            mapLoad();
        };

        // 마지막 위치 지도에 갱신
        Location lastLocation = locManager.getLastKnownLocation(bestProvider);
        if (lastLocation == null) { latLng = new LatLng(37.601822402047915, 127.04156039013381 );}
        else { latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()); }

        // Places 초기화 및 클라이언트 생성
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        placesClient = Places.createClient(this);

        // AddActivity 에서 넘어옴
        String it_store = (String) getIntent().getStringExtra("store");
        editText.setText(it_store);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        /* 지도가 뜨면 마커 일단 모두 등록해주기
        *   1. 디비에서 select location ,, where 재방문 O
        *   2. 디비에서 select location ,, where 재방문 X
        *   3. 각각 다른 색깔의 마커를 이용해서 마커 생성. -> 이거는 지도 만들어지고 한번만 실행되어야 함
        *   4. 여기서 주의해야 할 것은 디비에 있는 location 주소가 실제 주소여야 함 .
        *  */

        markerOptions = new MarkerOptions();

        // 현재 위치 검색 활성화
        if (checkPermission()){ mGoogleMap.setMyLocationEnabled(true);}
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(ViewMapActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // 마커 윈도우 클릭 시 - detailActivity로 인텐트 넘겨주기 : 주변 장소의 정보 보여주기
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                String placeId = marker.getTag().toString();
                getPlaceDetail(placeId);
            }
        });

    }

    public void onClick(View v) { // 주소를 입력하면 그 주변의 빵집을 검색해줌
        switch(v.getId()) {
            case R.id.btn_now: // 현재 위치 어디서 가져오는거????
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                searchStart(PlaceType.BAKERY, latLng);
                break;
            case R.id.btn_addr: // 검색한 위치의 위도 경도 어디서 셋해주는거???/
                startLatLngService();
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                searchStart(PlaceType.BAKERY, latLng);
                break;
        }
    }

    private void searchStart(String type, LatLng latLng) {
        new NRPlaces.Builder().listener(placesListener)
                .key(getString(R.string.google_api_key))
                .latlng(latLng.latitude, latLng.longitude)
                .radius(100)
                .type(type)
                .build()
                .execute();
    }

    PlacesListener placesListener = new PlacesListener() {
        @Override
        public void onPlacesSuccess(final List<Place> places) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.clear();     // 기존의 마커 삭제
                    for (noman.googleplaces.Place place : places) {
                        markerOptions.title(place.getName());
                        markerOptions.position(new LatLng(place.getLatitude(), place.getLongitude()));
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                        ));
                        Marker newMarker = mGoogleMap.addMarker(markerOptions);
                        newMarker.setTag(place.getPlaceId());
                        Log.d(TAG, "ID: " + place.getPlaceId());
                    }
                }
            });
        }
        @Override
        public void onPlacesFailure(PlacesException e) {}
        @Override
        public void onPlacesStart() {}
        @Override
        public void onPlacesFinished() {}
    };

    /* 주소 → 위도/경도 변환 IntentService 실행 */
    private void startLatLngService() {
        Intent intent = new Intent(this, FetchLatLngIntentService.class);
        String address = editText.getText().toString();

        intent.putExtra(Constants.RECEIVER, latLngResultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, address);

        startService(intent);
    }

    /* 주소 → 위도/경도 변환 ResultReceiver */
    class LatLngResultReceiver extends ResultReceiver {
        public LatLngResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            ArrayList<LatLng> latLngList = null;

            if (resultCode == Constants.SUCCESS_RESULT) {
                if (resultData == null)
                    return;

                latLngList = (ArrayList<LatLng>) resultData.getSerializable(Constants.RESULT_DATA_KEY);

                if (latLngList == null) {
                    lat = null;
                    lng = null;
                } else {
                    LatLng mlatlng = latLngList.get(0);
                    latLng = new LatLng(mlatlng.latitude, mlatlng.longitude);
                }
            } else {
                editText.setText(getString(R.string.no_address_found));
            }
        }
    }

    /*Place ID 의 장소에 대한 세부정보 획득*/
    private void getPlaceDetail(String placeId) {
        List<com.google.android.libraries.places.api.model.Place.Field> placeFields
                = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME
                , com.google.android.libraries.places.api.model.Place.Field.PHONE_NUMBER, com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                com.google.android.libraries.places.api.model.Place place = fetchPlaceResponse.getPlace();

                Intent intent = new Intent();
                intent.putExtra("location",  place.getAddress());
                setResult(RESULT_OK, intent);
                finish();

//                Intent intent = new Intent(ViewMapActivity.this, DetailActivity.class);
//                intent.putExtra("name", place.getName());
//                intent.putExtra("phone", place.getPhoneNumber());
//                intent.putExtra("address", place.getAddress());
//                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    Log.e(TAG, "Place not found : " + statusCode + "  " + e.getMessage());
                }
            }
        });
    }

    /*구글맵을 멤버변수로 로딩*/
    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /* 필요 permission 요청 */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQ_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 퍼미션을 획득하였을 경우 맵 로딩 실행
                mapLoad();
            } else {
                // 퍼미션 미획득 시 액티비티 종료
                Toast.makeText(this, "앱 실행을 위해 권한 허용이 필요함", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}
