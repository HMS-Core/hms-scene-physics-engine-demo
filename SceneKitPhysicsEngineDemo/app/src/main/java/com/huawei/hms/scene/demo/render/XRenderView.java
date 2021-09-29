/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.scene.demo.render;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.huawei.hms.scene.math.Vector3;
import com.huawei.hms.scene.sdk.render.Camera;
import com.huawei.hms.scene.sdk.render.Light;
import com.huawei.hms.scene.sdk.render.Node;
import com.huawei.hms.scene.sdk.render.RenderView;
import com.huawei.hms.scene.sdk.render.Resource;
import com.huawei.hms.scene.sdk.render.Texture;
import com.huawei.hms.scene.sdk.render.Transform;

import java.lang.ref.WeakReference;

/**
 * XRenderView.
 *
 * @author HUAWEI.
 * @since 2021-8-18
 */
public class XRenderView extends RenderView {
    private Texture skyBoxTexture;
    private Texture specularEnvTexture;
    private Texture diffuseEnvTexture;
    private boolean isDestroyed = false;

    public XRenderView(Context context) {
        super(context);
        prepareScene(context);
        Texture.builder()
            .setUri(Uri.parse("Scene/output_skybox.dds"))
            .load(context, new SkyBoxTextureLoadEventListener(new WeakReference<>(this)));
        Texture.builder()
            .setUri(Uri.parse("Scene/output_specular.dds"))
            .load(context, new SpecularEnvTextureLoadEventListener(new WeakReference<>(this)));
        Texture.builder()
            .setUri(Uri.parse("Scene/output_diffuse.dds"))
            .load(context, new DiffuseEnvTextureLoadEventListener(new WeakReference<>(this)));
    }

    public XRenderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        prepareScene(context);
        Texture.builder()
                .setUri(Uri.parse("Scene/output_skybox.dds"))
                .load(context, new SkyBoxTextureLoadEventListener(new WeakReference<>(this)));
        Texture.builder()
                .setUri(Uri.parse("Scene/output_specular.dds"))
                .load(context, new SpecularEnvTextureLoadEventListener(new WeakReference<>(this)));
        Texture.builder()
                .setUri(Uri.parse("Scene/output_diffuse.dds"))
                .load(context, new DiffuseEnvTextureLoadEventListener(new WeakReference<>(this)));
    }

    @Override
    public void destroy() {
        isDestroyed = true;
        if (skyBoxTexture != null) {
            Texture.destroy(skyBoxTexture);
        }
        if (specularEnvTexture != null) {
            Texture.destroy(specularEnvTexture);
        }
        if (diffuseEnvTexture != null) {
            Texture.destroy(diffuseEnvTexture);
        }
        super.destroy();
    }

    private void prepareScene(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Node cameraNode = getScene().createNode("mainCameraNode");
        cameraNode.addComponent(Camera.descriptor())
                .setProjectionMode(Camera.ProjectionMode.PERSPECTIVE)
                .setNearClipPlane(.1f)
                .setFarClipPlane(1000.f)
                .setFOV(60.f)
                .setAspect((float) displayMetrics.widthPixels / displayMetrics.heightPixels)
                .setActive(true);
        cameraNode.getComponent(Transform.descriptor())
                .setPosition(new Vector3(0, 5.f, 30.f));

        Node lightNode = getScene().createNode("mainLightNode");
        lightNode.addComponent(Light.descriptor())
                .setType(Light.Type.POINT)
                .setColor(new Vector3(1.f, 1.f, 1.f))
                .setIntensity(1.f)
                .setCastShadow(false);
        lightNode.getComponent(Transform.descriptor())
                .setPosition(new Vector3(3.f, 3.f, 3.f));
    }

    private static final class SkyBoxTextureLoadEventListener implements Resource.OnLoadEventListener<Texture> {
        private final WeakReference<XRenderView> weakRef;

        SkyBoxTextureLoadEventListener(WeakReference<XRenderView> weakRef) {
            this.weakRef = weakRef;
        }

        @Override
        public void onLoaded(Texture texture) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                Texture.destroy(texture);
                return;
            }
            renderView.skyBoxTexture = texture;
            renderView.getScene().setSkyBoxTexture(texture);
        }

        @Override
        public void onException(Exception exception) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                return;
            }
            Toast.makeText(renderView.getContext(),
                "failed to load texture: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static final class SpecularEnvTextureLoadEventListener implements Resource.OnLoadEventListener<Texture> {
        private final WeakReference<XRenderView> weakRef;

        SpecularEnvTextureLoadEventListener(WeakReference<XRenderView> weakRef) {
            this.weakRef = weakRef;
        }

        @Override
        public void onLoaded(Texture texture) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                Texture.destroy(texture);
                return;
            }

            renderView.specularEnvTexture = texture;
            renderView.getScene().setSpecularEnvTexture(texture);
        }

        @Override
        public void onException(Exception exception) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                return;
            }
            Toast.makeText(renderView.getContext(),
                "failed to load texture: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static final class DiffuseEnvTextureLoadEventListener implements Resource.OnLoadEventListener<Texture> {
        private final WeakReference<XRenderView> weakRef;

        DiffuseEnvTextureLoadEventListener(WeakReference<XRenderView> weakRef) {
            this.weakRef = weakRef;
        }

        @Override
        public void onLoaded(Texture texture) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                Texture.destroy(texture);
                return;
            }

            renderView.diffuseEnvTexture = texture;
            renderView.getScene().setDiffuseEnvTexture(texture);
        }

        @Override
        public void onException(Exception exception) {
            XRenderView renderView = weakRef.get();
            if (renderView == null || renderView.isDestroyed) {
                return;
            }
            Toast.makeText(renderView.getContext(),
                "failed to load texture: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
