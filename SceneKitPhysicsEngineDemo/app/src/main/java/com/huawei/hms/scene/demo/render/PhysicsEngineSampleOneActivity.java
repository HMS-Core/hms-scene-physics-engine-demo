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

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.scene.math.Vector3;
import com.huawei.hms.scene.sdk.render.Collider;
import com.huawei.hms.scene.sdk.render.Model;
import com.huawei.hms.scene.sdk.render.Node;
import com.huawei.hms.scene.sdk.render.Resource;
import com.huawei.hms.scene.sdk.render.ResourceFactory;
import com.huawei.hms.scene.sdk.render.RigidBody;
import com.huawei.hms.scene.sdk.render.Transform;

import java.lang.ref.WeakReference;

/**
 * PhysicsEngineSampleOneActivity.
 *
 * @author HUAWEI.
 * @since 2021-8-18
 */
public class PhysicsEngineSampleOneActivity extends AppCompatActivity {
    private XRenderView renderView;
    private Model sphereModel;
    private Model groundModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderView = new XRenderView(this);
        setContentView(renderView);
        Model.builder()
            .setUri(Uri.parse("Cube/cube.gltf"))
            .load(this, new ModelLoadEventListener(new WeakReference<>(this), true));
        Model.builder()
            .setUri(Uri.parse("Ball/ball.glb"))
            .load(this, new ModelLoadEventListener(new WeakReference<>(this), false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        renderView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        renderView.destroy();
        if (sphereModel != null) {
            Model.destroy(sphereModel);
        }
        if (groundModel != null) {
            Model.destroy(groundModel);
        }
        ResourceFactory.getInstance().gc();
    }

    private static final class ModelLoadEventListener implements Resource.OnLoadEventListener<Model> {
        private final WeakReference<PhysicsEngineSampleOneActivity> weakRef;
        private final boolean isGround;

        ModelLoadEventListener(WeakReference<PhysicsEngineSampleOneActivity> weakRef, boolean isGround) {
            this.weakRef = weakRef;
            this.isGround = isGround;
        }

        @Override
        public void onLoaded(Model model) {
            PhysicsEngineSampleOneActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                Model.destroy(model);
                return;
            }
            if (isGround) {
                sampleActivity.groundModel = model;
                Node groundNode = sampleActivity.renderView.getScene().createNodeFromModel(model);
                groundNode.addComponent(Collider.descriptor())
                    .createBoxShape()
                    .setExtent(new Vector3(1000f, 1f, 1000f));
                groundNode.getComponent(Transform.descriptor())
                    .setPosition(new Vector3(0f, -3f, 0f)).setScale(new Vector3(20f, 1f, 20f));
                groundNode.addComponent(RigidBody.descriptor())
                    .setRestitution(1f)
                    .setGroup((short) 1)
                    .setKinematic(false)
                    .setFriction(10f)
                    .setMask((short) 1)
                    .setMass(0);
                return;
            }
            sampleActivity.sphereModel = model;
            for (int index = 0; index < 4; index++) {
                Node node = sampleActivity.renderView.getScene().createNodeFromModel(model);
                Transform transform = node.getComponent(Transform.descriptor());
                transform.setScale(new Vector3(0.6f, 0.6f, 0.6f));
                transform.setPosition(new Vector3(-3.0f + 2 * index, 10.0f, 10.0f));
                node.addComponent(Collider.descriptor())
                    .createSphereShape().setRadius(0.6f);
                node.addComponent(RigidBody.descriptor())
                    .setMask((short) 1)
                    .setGroup((short) 1)
                    .setFriction(10.0f)
                    .setKinematic(false)
                    .setMass(1.0f)
                    .setRestitution( 0.3f * index);
            }
        }

        @Override
        public void onException(Exception exception) {
            PhysicsEngineSampleOneActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                return;
            }
            Toast.makeText(sampleActivity,
                "failed to load model: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
