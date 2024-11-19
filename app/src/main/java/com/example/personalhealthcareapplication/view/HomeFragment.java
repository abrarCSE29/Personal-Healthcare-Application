package com.example.personalhealthcareapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.viewmodel.MedicineReminderViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private ViewPager2 healthTipsViewPager;
    private Handler autoScrollHandler;
    private int currentPage = 0;

    // Location Permission Request Code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        MedicineReminderViewModel viewModel = new ViewModelProvider(this).get(MedicineReminderViewModel.class);

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Emergency Card Click Listener
        View emergencyCard = view.findViewById(R.id.emergency_card);
        emergencyCard.setOnClickListener(v -> {
            // Open the dialer with 999
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:999"));
            startActivity(intent);
        });
        healthTipsViewPager = view.findViewById(R.id.health_tips_viewpager);
        List<String> healthTips = Arrays.asList(
                "Drink plenty of water to stay hydrated.",
                "Exercise for at least 30 minutes daily.",
                "Eat a balanced diet rich in fruits and vegetables.",
                "Get 7-8 hours of sleep every night.",
                "Practice mindfulness to reduce stress."
        );

        HealthTipsPagerAdapter adapter = new HealthTipsPagerAdapter(healthTips);
        healthTipsViewPager.setAdapter(adapter);

        // Set up auto-scroll
        autoScrollHandler = new Handler(Looper.getMainLooper());
        startAutoScroll(healthTips.size());

        // Chatbot Card Click Listener
        CardView chatbotCard = view.findViewById(R.id.chatbot_card);
        chatbotCard.setOnClickListener(v -> {
            // Navigate to Chatbot Activity (uncomment to implement navigation)
             Intent intent = new Intent(getActivity(), ChatbotActivity.class);
             startActivity(intent);
        });

        // Initialize the map view
        mapView = view.findViewById(R.id.map);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0); // Set initial zoom level

        // Get current location and fetch nearby hospitals
        getCurrentLocation();
        TextView medicineNameView = view.findViewById(R.id.tvMedicineName);
        TextView reminderTimeView = view.findViewById(R.id.tvReminderTime);
        TextView quantityView = view.findViewById(R.id.tvQuantity);

        viewModel.getUpcomingReminder().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                MedicineReminder reminder = task.getResult();

                // Format time for display
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String formattedTime = sdf.format(reminder.getReminderTimeInMillis());

                // Update UI
                medicineNameView.setText(reminder.getMedicineName());
                reminderTimeView.setText(formattedTime);
                quantityView.setText("Quantity: " + reminder.getQuantity());
            } else {
                // No upcoming reminder found
                medicineNameView.setText("No upcoming reminders");
                reminderTimeView.setText("");
                quantityView.setText("");
            }
        });

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get the last known location
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double userLatitude = location.getLatitude();
                double userLongitude = location.getLongitude();

                // Add a marker for the current location with a custom icon
                GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                Marker userMarker = new Marker(mapView);
                userMarker.setPosition(userLocation);
                userMarker.setTitle("You are here");
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                userMarker.setIcon(getResources().getDrawable(R.drawable.user_location_icon, null)); // Use custom icon
                mapView.getOverlays().add(userMarker);

                // Center map on user's location
                mapView.getController().setCenter(userLocation);

                // Fetch and display nearby hospitals
                fetchNearbyHospitals(userLatitude, userLongitude);
            } else {
                Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNearbyHospitals(double userLatitude, double userLongitude) {
        String url = "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=hospital](around:5000," + userLatitude + "," + userLongitude + ");out;";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray elements = jsonObject.getJSONArray("elements");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);
                        double lat = element.getDouble("lat");
                        double lon = element.getDouble("lon");

                        String name = element.has("tags") ? element.getJSONObject("tags").optString("name", "Unnamed Hospital") : "Unnamed Hospital";
                        String phone = element.has("tags") ? element.getJSONObject("tags").optString("phone", "No contact info") : "No contact info";
                        String street = element.has("tags") ? element.getJSONObject("tags").optString("addr:street", "") : "";
                        String city = element.has("tags") ? element.getJSONObject("tags").optString("addr:city", "") : "";
                        String postcode = element.has("tags") ? element.getJSONObject("tags").optString("addr:postcode", "") : "";

                        String address = (!street.isEmpty() ? street + ", " : "") +
                                (!city.isEmpty() ? city + ", " : "") +
                                (!postcode.isEmpty() ? postcode : "");

                        if (address.isEmpty()) {
                            address = "No address available";
                        }

                        String markerTitle = name + "\nContact: " + phone + "\nAddress: " + address;

                        // Add a marker for each hospital
                        Marker hospitalMarker = new Marker(mapView);
                        hospitalMarker.setPosition(new GeoPoint(lat, lon));
                        hospitalMarker.setTitle(markerTitle);
                        mapView.getOverlays().add(hospitalMarker);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void startAutoScroll(int itemCount) {
        autoScrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentPage >= itemCount) {
                    currentPage = 0; // Loop back to the first item
                }
                healthTipsViewPager.setCurrentItem(currentPage++, true);
                autoScrollHandler.postDelayed(this, 3000); // Change every 3 seconds
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the handler to avoid memory leaks
        if (autoScrollHandler != null) {
            autoScrollHandler.removeCallbacksAndMessages(null);
        }
    }
}
