/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.CamerasAdapter;
import nl.hnogames.domoticz.CameraActivity;
import nl.hnogames.domoticz.Containers.CameraInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.CameraReceiver;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzCardFragment;

public class Cameras extends DomoticzCardFragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = Cameras.class.getSimpleName();

    private ProgressDialog progressDialog;
    private Activity mActivity;
    private Domoticz mDomoticz;
    private RecyclerView mRecyclerView;
    private CamerasAdapter mAdapter;
    private ArrayList<CameraInfo> mCameras;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void refreshFragment() {
        getCameras();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void getCameras() {
        showProgressDialog();

        coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id
                .coordinatorLayout);
        mDomoticz = new Domoticz(mActivity);
        mDomoticz.getCameras(new CameraReceiver() {

            @Override
            public void OnReceiveCameras(ArrayList<CameraInfo> Cameras) {
                successHandling(Cameras.toString(), false);

                Cameras.this.mCameras = Cameras;
                mAdapter = new CamerasAdapter(Cameras, getActivity());
                mAdapter.setOnItemClickListener(new CamerasAdapter.onClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        ImageView cameraImage = (ImageView)v.findViewById(R.id.image);
                        TextView cameraTitle = (TextView)v.findViewById(R.id.name);
                        Bitmap savePic = ((BitmapDrawable)cameraImage.getDrawable()).getBitmap();

                        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/Domoticz/SnapShot";
                        File dir = new File(file_path);
                        if(!dir.exists())
                            dir.mkdirs();

                        File file = new File(dir, "snapshot" + cameraTitle.getText() + ".jpg");
                        FileOutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(file);
                            savePic.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                            fOut.flush();
                            fOut.close();

                            Intent intent = new Intent(getActivity(), CameraActivity.class);
                            intent.putExtra("IMAGETITLE", cameraTitle.getText());
                            intent.putExtra("IMAGEURL", file.getPath());
                            startActivity(intent);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
                hideProgressDialog();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_cameras);
    }

    /**
     * Initializes the progress dialog
     */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage(getString(R.string.msg_please_wait));
        progressDialog.setCancelable(false);
    }

    /**
     * Shows the progress dialog if isn't already showing
     */
    private void showProgressDialog() {
        if (progressDialog == null) initProgressDialog();
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * Hides the progress dialog if it is showing
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    public void errorHandling(Exception error) {
        // Let's check if were still attached to an activity
        if (isAdded()) {
            super.errorHandling(error);
            hideProgressDialog();
        }
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        mDomoticz = new Domoticz(getActivity());
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getCameras();
    }
}