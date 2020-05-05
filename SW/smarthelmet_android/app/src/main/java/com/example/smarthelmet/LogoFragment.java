package com.example.smarthelmet;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class LogoFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_logo, container, false);
        final ImageView logoImg = view.findViewById(R.id.logoImg);
        final TextView logoTv = view.findViewById(R.id.logoTv);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                logoImg.setVisibility(View.GONE);
                logoTv.setVisibility(View.GONE);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                         .setCustomAnimations(R.anim.slide_down, R.anim.slide_up)
                        .replace(R.id.frame_container, new ConnectFragment()) // replace flContainer
                        .commit();
            }
        }, 2000);

        return view;
    }
}