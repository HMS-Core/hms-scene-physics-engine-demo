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
import com.huawei.hms.scene.sdk.render.HingeConstraint;
import com.huawei.hms.scene.sdk.render.Model;
import com.huawei.hms.scene.sdk.render.Node;
import com.huawei.hms.scene.sdk.render.Point2PointConstraint;
import com.huawei.hms.scene.sdk.render.Resource;
import com.huawei.hms.scene.sdk.render.ResourceFactory;
import com.huawei.hms.scene.sdk.render.RigidBody;
import com.huawei.hms.scene.sdk.render.Transform;

import java.lang.ref.WeakReference;

/**
 * PhysicsEngineSampleTwoActivity.
 *
 * @author HUAWEI.
 * @since 2021-8-18
 */
public class PhysicsEngineSampleTwoActivity extends AppCompatActivity {
    private XRenderView renderView;
    private Model ballModel;
    private Model boxModel;
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
        Model.builder()
                .setUri(Uri.parse("Box/Box.gltf"))
                .load(this, new BoxModelLoadEventListener(new WeakReference<>(this)));
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
        if (ballModel != null) {
            Model.destroy(ballModel);
        }
        if (boxModel != null) {
            Model.destroy(boxModel);
        }
        if (groundModel != null) {
            Model.destroy(groundModel);
        }
        ResourceFactory.getInstance().gc();
    }

    private Node loadModel(Model model, Vector3 scale, Vector3 position) {
        Node node = model == null ? renderView.getScene().createNode() :
            renderView.getScene().createNodeFromModel(model);
        Transform transform = node.getComponent(Transform.descriptor());
        transform.setScale(scale);
        transform.setWorldPosition(position);
        return node;
    }

    private void addPoint2PointConstraint() {
        Node staticNode = loadModel(ballModel, new Vector3(0.1f, 0.1f, 0.1f), new Vector3(0, 15, 10));
        staticNode.addComponent(RigidBody.descriptor())
            .setMass(0)
            .setKinematic(false)
            .setGroup((short) 0)
            .setMask((short) 0);
        staticNode.addComponent(Collider.descriptor())
            .createSphereShape()
            .setRadius(0.1f);
        Node targetNode = loadModel(ballModel, new Vector3(1f, 1f, 1f), new Vector3(2, 15, 10));
        targetNode.addComponent(RigidBody.descriptor())
            .setMass(1)
            .setLinearDamping(0)
            .setAngularDamping(0);
        targetNode.addComponent(Collider.descriptor())
            .createSphereShape()
            .setRadius(0.77f);
        Point2PointConstraint constraint = targetNode.addComponent(Point2PointConstraint.descriptor());
        constraint.setPivotA(new Vector3(0, 0, 0));
        constraint.setPivotB(new Vector3(0, 3, 0));
        constraint.setNodeA(staticNode);
        constraint.setNodeB(targetNode);
    }

    private void addHingeConstraint() {
        Node staticNode = loadModel(boxModel, new Vector3(0.2f, 8, 0.25f), new Vector3(0.12f, 5, 10));
        staticNode.addComponent(Collider.descriptor()).createBoxShape().setExtent(new Vector3(0.1f, 4, 0.125f));
        staticNode.addComponent(RigidBody.descriptor())
            .setMass(0)
            .setGroup((short) 0)
            .setMask((short) 0);
        Node targetNode = loadModel(boxModel, new Vector3(4, 8, 0.25f), new Vector3(-2, 5, 10));
        targetNode.addComponent(Collider.descriptor()).createBoxShape().setExtent(new Vector3(1.98f, 4, 0.125f));
        targetNode.addComponent(RigidBody.descriptor())
            .setMass(1)
            .setAngularDamping(0)
            .setLinearDamping(0)
            .applyImpulse(new Vector3(15, 0, 15), new Vector3(1, 0, 0));
        HingeConstraint hinge = targetNode.addComponent(HingeConstraint.descriptor());
        hinge.setNodeA(targetNode);
        hinge.setNodeB(staticNode);
        hinge.setLowerLimit(0.f)
            .setUpperLimit((float) Math.PI)
            .setPivotA(new Vector3(2, 0, 0))
            .setPivotB(Vector3.ZERO)
            .setAxisA(Vector3.UP)
            .setAxisB(Vector3.UP);
    }

    private static final class ModelLoadEventListener implements Resource.OnLoadEventListener<Model> {
        private final WeakReference<PhysicsEngineSampleTwoActivity> weakRef;
        private final boolean isGround;

        ModelLoadEventListener(WeakReference<PhysicsEngineSampleTwoActivity> weakRef, boolean isGround) {
            this.weakRef = weakRef;
            this.isGround = isGround;
        }

        @Override
        public void onLoaded(Model model) {
            PhysicsEngineSampleTwoActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                Model.destroy(model);
                return;
            }
            if (isGround) {
                sampleActivity.groundModel = model;
                Node groundNode = sampleActivity.renderView.getScene().createNodeFromModel(model);
                groundNode.addComponent(Collider.descriptor())
                        .createBoxShape()
                        .setExtent(new Vector3(1000, 1, 1000));
                groundNode.getComponent(Transform.descriptor())
                        .setPosition(new Vector3(0, -3, 0)).setScale(new Vector3(20, 1, 20));
                groundNode.addComponent(RigidBody.descriptor())
                        .setRestitution(1)
                        .setGroup((short) 1)
                        .setKinematic(false)
                        .setFriction(10)
                        .setMask((short) 1)
                        .setMass(0);
                return;
            }
            sampleActivity.ballModel = model;
            sampleActivity.addPoint2PointConstraint();
        }

        @Override
        public void onException(Exception exception) {
            PhysicsEngineSampleTwoActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                return;
            }
            Toast.makeText(sampleActivity,
                "failed to load model: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static final class BoxModelLoadEventListener implements Resource.OnLoadEventListener<Model> {
        private final WeakReference<PhysicsEngineSampleTwoActivity> weakRef;

        BoxModelLoadEventListener(WeakReference<PhysicsEngineSampleTwoActivity> weakRef) {
            this.weakRef = weakRef;
        }

        @Override
        public void onLoaded(Model model) {
            PhysicsEngineSampleTwoActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                Model.destroy(model);
                return;
            }
            sampleActivity.boxModel = model;
            sampleActivity.addHingeConstraint();
        }

        @Override
        public void onException(Exception exception) {
            PhysicsEngineSampleTwoActivity sampleActivity = weakRef.get();
            if (sampleActivity == null || sampleActivity.isDestroyed()) {
                return;
            }
            Toast.makeText(sampleActivity,
                "failed to load model: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
