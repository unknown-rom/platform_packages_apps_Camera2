/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.ProgressOverlay;
import com.android.camera.util.UsageStatistics;
import com.android.camera2.R;

/**
 * Contains the UI for the CaptureModule.
 */
public class CaptureModuleUI implements
        PreviewStatusListener {

    private final CameraActivity mActivity;
    private final CaptureModule mModule;
    private final View mRootView;

    private final ProgressOverlay mProgressOverlay;
    private final View.OnLayoutChangeListener mLayoutListener;
    private final TextureView mPreviewView;
    private final ImageView mPreviewThumb;

    private final GestureDetector.OnGestureListener mPreviewGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            mModule.onSingleTapUp(null, (int) ev.getX(), (int) ev.getY());
            return true;
        }
    };
    private final FocusOverlayManager.FocusUI mFocusUI;
    private int mPreviewAreaWidth;
    private int mPreviewAreaHeight;

    @Override
    public void onPreviewLayoutChanged(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (mLayoutListener != null) {
            mLayoutListener.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight,
                    oldBottom);
        }
    }

    @Override
    public boolean shouldAutoAdjustTransformMatrixOnLayout() {
        return false;
    }

    @Override
    public boolean shouldAutoAdjustBottomBar() {
        return true;
    }

    @Override
    public void onPreviewFlipped() {
        // Do nothing because when preview is flipped, TextureView will lay
        // itself out again, which will then trigger a transform matrix update.
    }

    @Override
    public GestureDetector.OnGestureListener getGestureListener() {
        return mPreviewGestureListener;
    }

    @Override
    public View.OnTouchListener getTouchListener() {
        return null;
    }

    public CaptureModuleUI(CameraActivity activity, CaptureModule module, View parent,
            View.OnLayoutChangeListener layoutListener) {
        mActivity = activity;
        mModule = module;
        mRootView = parent;
        mLayoutListener = layoutListener;

        ViewGroup moduleRoot = (ViewGroup) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.capture_module,
                moduleRoot, true);

        mPreviewView = (TextureView) mRootView.findViewById(R.id.preview_content);

        mProgressOverlay = (ProgressOverlay) mRootView.findViewById(R.id.progress_overlay);

        mPreviewThumb = (ImageView) mRootView.findViewById(R.id.gcam_preview_thumb);
        mPreviewThumb.setScaleType(ImageView.ScaleType.MATRIX);
        mPreviewThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.gotoGallery();
            }
        });

        mFocusUI = (FocusOverlayManager.FocusUI) mRootView.findViewById(R.id.focus_overlay);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mModule.onSurfaceTextureAvailable(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mModule.onSurfaceTextureDestroyed(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mModule.onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        mModule.onSurfaceTextureUpdated(surface);
    }

    public void positionProgressOverlay(RectF area) {
        mProgressOverlay.setBounds(area);
    }

    /**
     * Getter for the width of the visible area of the preview.
     */
    public int getPreviewAreaWidth() {
        return mPreviewAreaWidth;
    }

    /**
     * Getter for the height of the visible area of the preview.
     */
    public int getPreviewAreaHeight() {
        return mPreviewAreaHeight;
    }

    public Matrix getPreviewTransform(Matrix m) {
        return mPreviewView.getTransform(m);
    }

    public void showAutoFocusInProgress() {
        mFocusUI.onFocusStarted();
    }

    public void showAutoFocusSuccess() {
        mFocusUI.onFocusSucceeded();
    }

    public void showAutoFocusFailure() {
        mFocusUI.onFocusFailed();
    }

    public void setAutoFocusTarget(int x, int y) {
        // TODO: refactor.
        boolean isAutoFocus = false;
        mFocusUI.setFocusPosition(x, y, isAutoFocus);
    }

    public void clearAutoFocusIndicator() {
        mFocusUI.clearFocus();
    }

    public void clearAutoFocusIndicator(boolean waitUntilProgressIsHidden) {
    }
    /**
     * Sets the progress of the gcam picture taking.
     *
     * @param percent amount of process done in percent 0-100.
     */
    public void setPictureTakingProgress(int percent) {
        mProgressOverlay.setProgress(percent);
    }

    public Bitmap getBitMapFromPreview() {
        Matrix m = new Matrix();
        m = getPreviewTransform(m);
        Bitmap src = mPreviewView.getBitmap();
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }
}