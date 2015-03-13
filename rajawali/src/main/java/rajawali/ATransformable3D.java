/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;
import rajawali.renderer.AFrameTask;
import rajawali.scenegraph.IGraphNode;
import rajawali.scenegraph.IGraphNodeMember;

/**
 * Base class for any object which will be positioned in the scene. Encapsulates
 * position, orientation, scene graph and scale. Provides methods for modifying each.
 * 
 * Orientation is stored by a Quaternion. You can specify a look at target which will 
 * be maintained as this object is translated around. If you explicitly set or alter the
 * rotation of this object, the look at target will be retained, but will no longer be 
 * enforced. {@link #resetToLookAt()} or {@link #resetToLookAt(Vector3)} can be called
 * to re-enable look at target enforcing, negating any manual rotations. 
 * 
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 *
 */
public abstract class ATransformable3D extends AFrameTask implements IGraphNodeMember {
	protected final Vector3 mPosition; //The position
	protected final Vector3 mScale; //The scale
	protected final Quaternion mOrientation; //The orientation
	protected final Quaternion mTmpOrientation; //A scratch quaternion
	protected final Vector3 mTempVec = new Vector3(); //Scratch vector
	protected Vector3 mLookAt; //The look at target
	protected boolean mLookAtValid = false; //Is the look at target up to date?
	protected boolean mLookAtEnabled = true; //Should we auto enforce look at target?
	protected final Vector3 mUpAxis; //The current up axis
	protected boolean mIsCamera; //is this a camera object?
	
	protected IGraphNode mGraphNode; //Which graph node are we in?
	protected boolean mInsideGraph = false; //Default to being outside the graph
	
	/**
	 * Default constructor for {@link ATransformable3D}.
	 */
	public ATransformable3D() {
		mPosition = new Vector3();
		mScale = new Vector3(1, 1, 1);
		mOrientation = new Quaternion();
		mTmpOrientation = new Quaternion();
		mUpAxis = new Vector3(Vector3.getAxisVector(Axis.Y)); //Default to +Y
	}
	
	
	
	//--------------------------------------------------
	// Translation methods
	//--------------------------------------------------
	
	/**
	 * Sets the position of this {@link ATransformable3D}. If this is 
	 * part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param position {@link Vector3} The new position. This is copied
	 * into an internal store and can be used after this method returns.
	 */
	public void setPosition(Vector3 position) {
		mPosition.setAll(position);
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) 
			resetToLookAt();
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	/**
	 * Sets the position of this {@link ATransformable3D}. If this is 
	 * part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param x double The x coordinate new position.
	 * @param y double The y coordinate new position.
	 * @param z double The z coordinate new position.
	 */
	public void setPosition(double x, double y, double z) {
		mPosition.setAll(x, y, z);
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) 
			resetToLookAt();
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	/**
	 * Sets the x component of the position for this {@link ATransformable3D}.
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param x double The new x component for the position.
	 */
	public void setX(double x) {
		mPosition.x = x;
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) 
			resetToLookAt();
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	/**
	 * Sets the y component of the position for this {@link ATransformable3D}.
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param y double The new y component for the position.
	 */
	public void setY(double y) {
		mPosition.y = y;
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) 
			resetToLookAt();
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	/**
	 * Sets the z component of the position for this {@link ATransformable3D}.
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param z double The new z component for the position.
	 */
	public void setZ(double z) {
		mPosition.z = z;
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) 
			resetToLookAt();
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	/**
	 * Gets the position of this {@link ATransformable3D}.
	 * 
	 * @return {@link Vector3} The position.
	 */
	public Vector3 getPosition() {
		return mPosition;
	}
	
	/**
	 * Gets the x component of the position of this {@link ATransformable3D}.
	 * 
	 * @return double The x component of the position.
	 */
	public double getX() {
		return mPosition.x;
	}

	/**
	 * Gets the y component of the position of this {@link ATransformable3D}.
	 * 
	 * @return double The y component of the position.
	 */
	public double getY() {
		return mPosition.y;
	}

	/**
	 * Gets the z component of the position of this {@link ATransformable3D}.
	 * 
	 * @return double The z component of the position.
	 */
	public double getZ() {
		return mPosition.z;
	}
	


	//--------------------------------------------------
	// Rotation methods
	//--------------------------------------------------
	
	/**
	 * Rotates this {@link ATransformable3D} by the rotation described by the provided
	 * {@link Quaternion}. If this is part of a scene graph, the graph will be notified 
	 * of the change.
	 * 
	 * @param quat {@link Quaternion} describing the additional rotation.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D rotate(final Quaternion quat) {
		mOrientation.multiply(quat);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Rotates this {@link ATransformable3D} by the rotation described by the provided
	 * {@link Vector3} axis and angle of rotation. If this is part of a scene graph, the 
	 * graph will be notified of the change.
	 * 
	 * @param axis {@link Vector3} The axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D rotate(final Vector3 axis, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Rotates this {@link ATransformable3D} by the rotation described by the provided
	 * {@link Axis} cardinal axis and angle of rotation. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param axis {@link Axis} The axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D rotate(final Axis axis, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Rotates this {@link ATransformable3D} by the rotation described by the provided
	 * axis and angle of rotation. If this is part of a scene graph, the graph will be 
	 * notified of the change.
	 * 
	 * @param x double The x component of the axis of rotation.
	 * @param y double The y component of the axis of rotation.
	 * @param z double The z component of the axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D rotate(double x, double y, double z, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(x, y, z, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Rotates this {@link ATransformable3D} by the rotation described by the provided
	 * {@link Matrix4}. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param matrix {@link Matrix4} describing the rotation to apply.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D rotate(final Matrix4 matrix) {
		mOrientation.multiply(mTmpOrientation.fromMatrix(matrix));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by 
	 * the provided {@link Quaternion}. If this is part of a scene graph, the graph will 
	 * be notified of the change.
	 * 
	 * @param quat {@link Quaternion} describing the additional rotation.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(final Quaternion quat) {
		mOrientation.multiply(quat);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by 
	 * the provided {@link Vector3} axis and angle of rotation. If this is part of a scene 
	 * graph, the graph will be notified of the change.
	 * 
	 * @param axis {@link Vector3} The axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(final Vector3 axis, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided {@link Axis} cardinal axis and angle of rotation. If this is part of a 
	 * scene graph, the graph will be notified of the change.
	 * 
	 * @param axis {@link Axis} The axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(final Axis axis, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided axis and angle of rotation. If this is part of a scene graph, the graph will be 
	 * notified of the change.
	 * 
	 * @param x double The x component of the axis of rotation.
	 * @param y double The y component of the axis of rotation.
	 * @param z double The z component of the axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(double x, double y, double z, double angle) {
		mOrientation.multiply(mTmpOrientation.fromAngleAxis(x, y, z, angle));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
	 * provided {@link Matrix4}. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param matrix {@link Matrix4} describing the rotation to apply.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(final Matrix4 matrix) {
		mOrientation.multiply(mTmpOrientation.fromMatrix(matrix));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided Euler angles. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param rotation {@link Vector3} whose components represent the Euler angles in degrees. 
	 * X = Roll, Y = Yaw, Z = Pitch.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(Vector3 rotation) {
		mOrientation.fromEuler(rotation.y, rotation.z, rotation.x);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided Euler angles. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param rotX double The roll angle in degrees.
	 * @param rotY double The yaw angle in degrees.
	 * @param rotZ double The pitch angle in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotation(double rotX, double rotY, double rotZ) {
		mOrientation.fromEuler(rotY, rotZ, rotX);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided Euler angle. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param rotX double The roll angle in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotX(double rotX) {
		mTmpOrientation.setAll(mOrientation);
		mOrientation.fromEuler(mTmpOrientation.getYaw(false), mTmpOrientation.getPitch(false), rotX);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided Euler angle. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param rotY double The yaw angle in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotY(double rotY) {
		mTmpOrientation.setAll(mOrientation);
		mOrientation.fromEuler(rotY, mTmpOrientation.getPitch(false), mTmpOrientation.getRoll(false));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the 
	 * provided Euler angle. If this is part of a scene graph, the graph will be notified of 
	 * the change.
	 * 
	 * @param rotZ double The pitch angle in degrees.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setRotZ(double rotZ) {
		mTmpOrientation.setAll(mOrientation);
		mOrientation.fromEuler(mTmpOrientation.getYaw(false), rotZ, mTmpOrientation.getRoll(false));
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Extracts the roll Euler angle from the current orientation.
	 * 
	 * @return double The roll Euler angle.
	 */
	public double getRotX() {
		return mOrientation.getRoll(false);
	}

	/**
	 * Extracts the yaw Euler angle from the current orientation.
	 * 
	 * @return double The yaw Euler angle.
	 */
	public double getRotY() {
		return mOrientation.getYaw(false);
	}

	/**
	 * Extracts the pitch Euler angle from the current orientation.
	 * 
	 * @return double The pitch Euler angle.
	 */
	public double getRotZ() {
		return mOrientation.getPitch(false);
	}
	
	
	
	//--------------------------------------------------
	// Orientation methods
	//--------------------------------------------------
	
	/**
	 * Sets the orientation of this {@link ATransformable3D} object.
	 * 
	 * @param quat {@link Quaternion} to copy the orientation from. The values of this
	 * object are copied and the passed object is not retained.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setOrientation(Quaternion quat) {
		mOrientation.setAll(quat);
		mLookAtValid = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Gets the current orientation of this {@link ATransformable3D} object.
	 * 
	 * @param quat {@link Quaternion} To copy the orientation into.
	 * @return The provided {@link Quaternion} to fascilitate chaining.
	 */
	public Quaternion getOrientation(Quaternion quat) {
		quat.setAll(mOrientation); 
		return quat;
	}
	
	/**
	 * Orients this {@link ATransformable3D} object to 'look at' the specified point. 
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param lookAt {@link Vector3} The look at target. Must not be null.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setLookAt(Vector3 lookAt) {
		if (lookAt == null) {
			throw new IllegalArgumentException("As of Rajawali v0.10, you cannot set a " +
					"null look target. If you want to remove the look target, use " +
					"clearLookAt(boolean) instead.");
		}
		if (mLookAt == null) mLookAt = new Vector3();
		mLookAt.setAll(lookAt);
		resetToLookAt();
		mLookAtValid = true;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Orients this {@link ATransformable3D} object to 'look at' the specified point. 
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @param x {@code double} The look at target x coordinate.
     * @param y {@code double} The look at target y coordinate.
     * @param z {@code double} The look at target z coordinate.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setLookAt(double x, double y, double z) {
		if (mLookAt == null) mLookAt = new Vector3();
		mLookAt.x = x;
		mLookAt.y = y;
		mLookAt.z = z;
		resetToLookAt();
		mLookAtValid = true;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Clears the look at target. If this is part of a scene graph, the graph will be 
	 * notified of the change.
	 * 
	 * @param retainOrientation boolean If true, the orientation will not be affected. If false,
	 * resets the orientation of this object to its as loaded state (identity Quaternion).
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D clearLookAt(boolean retainOrientation) {
		mLookAt = null;
		mLookAtValid = true;
		if (!retainOrientation) {
			mOrientation.identity();
			if (mGraphNode != null) mGraphNode.updateObject(this);
		}
		return this;
	}
	
	/**
	 * Enables auto-enforcement of look at target.
	 */
	public void enableLookAt() {
		mLookAtEnabled = true;
	}
	
	/**
	 * Disables auto-enforcement of look at target.
	 */
	public void disableLookAt() {
		mLookAtEnabled = false;
	}
	
	/**
	 * Check the current state of look target tracking.
	 * 
	 * @return boolean The look target tracking state.
	 */
	public boolean isLookAtEnabled() {
		return mLookAtEnabled;
	}
	
	/**
	 * Check the current state of the look at target.
	 * 
	 * @return boolean True if the current look at target is correct.
	 */
	public boolean isLookAtValid() {
		return mLookAtValid;
	}
	
	/**
	 * Resets the orientation of this {@link ATransformable3D} object to look at its look at
	 * target and use the current up axis. If this is part of a scene graph, the graph 
	 * will be notified of the change.
	 * 
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D resetToLookAt() {
		resetToLookAt(mUpAxis);
		return this;
	}
	
	/**
	 * Resets the orientation of this {@link ATransformable3D} object to look at its look at
	 * target and use the specified {@link Vector3} as up. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param {@link Vector3} The direction to use as the up axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D resetToLookAt(Vector3 upAxis) {
		if (mLookAt == null) {
			mOrientation.identity();
		} else {
			mOrientation.lookAt(mTempVec.subtractAndSet(mPosition, mLookAt), upAxis, mIsCamera);
		}
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
	 * graph, the graph will be notified of the change.
	 * 
	 * @param upAxis {@link Vector3} The new up axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setUpAxis(Vector3 upAxis) {
		mUpAxis.setAll(upAxis);
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) {
			mOrientation.lookAt(mLookAt, mUpAxis, mIsCamera);
			if (mGraphNode != null) mGraphNode.updateObject(this);
		}
		return this;
	}
	
	/**
	 * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
	 * graph, the graph will be notified of the change.
	 * 
	 * @param upAxis {@link Axis} The new up axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setUpAxis(Axis upAxis) {
		mUpAxis.setAll(upAxis);
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) {
			mOrientation.lookAt(mLookAt, mUpAxis, mIsCamera);
			if (mGraphNode != null) mGraphNode.updateObject(this);
		}
		return this;
	}
	
	/**
	 * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
	 * graph, the graph will be notified of the change.
	 * 
	 * @param x double The x component of the new up axis.
	 * @param y double The y component of the new up axis.
	 * @param z double The z component of the new up axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setUpAxis(double x, double y, double z) {
		mUpAxis.setAll(x, y, z);
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) {
			mOrientation.lookAt(mLookAt, mUpAxis, mIsCamera);
			if (mGraphNode != null) mGraphNode.updateObject(this);
		}
		return this;
	}
	
	/**
	 * Resets the up axis for this {@link ATransformable3D} object to the +Y axis.
	 * If this is part of a scene graph, the graph will be notified of the change.
	 * 
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D resetUpAxis() {
		mUpAxis.setAll(Vector3.getAxisVector(Axis.Y));
		if (mLookAtEnabled && mLookAt != null && mLookAtValid) {
			mOrientation.lookAt(mLookAt, mUpAxis, mIsCamera);
			if (mGraphNode != null) mGraphNode.updateObject(this);
		}
		return this;
	}
	
	
	
	//--------------------------------------------------
	// Scaling methods
	//--------------------------------------------------
	
	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scale {@link Vector3} Containing the scaling factor in each axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScale(Vector3 scale) {
		mScale.setAll(scale);
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scaleX double The scaling factor on the x axis.
	 * @param scaleY double The scaling factor on the y axis.
	 * @param scaleZ double The scaling factor on the z axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScale(double scaleX, double scaleY, double scaleZ) {
		mScale.x = scaleX;
		mScale.y = scaleY;
		mScale.z = scaleZ;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scale double The scaling factor on axes.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScale(double scale) {
		mScale.x = scale;
		mScale.y = scale;
		mScale.z = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scale double The scaling factor on the x axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScaleX(double scale) {
		mScale.x = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scale double The scaling factor on the y axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScaleY(double scale) {
		mScale.y = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}

	/**
	 * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph, 
	 * the graph will be notified of the change.
	 * 
	 * @param scale double The scaling factor on the z axis.
	 * @return A reference to this {@link ATransformable3D} to facilitate chaining.
	 */
	public ATransformable3D setScaleZ(double scale) {
		mScale.z = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
		return this;
	}
	
	/**
	 * Gets the scaling factor along each axis.
	 * 
	 * @return {@link Vector3} containing the scaling factors for each axis.
	 */
	public Vector3 getScale() {
		return mScale;
	}

	public Vector3 getLookAt() {
		return mLookAt;
	}

	/**
	 * Gets the scaling factor along the x axis.
	 * 
	 * @return double containing the scaling factor for the x axis.
	 */
	public double getScaleX() {
		return mScale.x;
	}
	
	/**
	 * Gets the scaling factor along the y axis.
	 * 
	 * @return double containing the scaling factor for the y axis.
	 */
	public double getScaleY() {
		return mScale.y;
	}

	/**
	 * Gets the scaling factor along the z axis.
	 * 
	 * @return double containing the scaling factor for the z axis.
	 */
	public double getScaleZ() {
		return mScale.z;
	}


	
	//--------------------------------------------------
	// Scene graph methods
	//--------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#setGraphNode(rajawali.scenegraph.IGraphNode)
	 */
	public void setGraphNode(IGraphNode node, boolean inside) {
		mGraphNode = node;
		mInsideGraph = inside;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getGraphNode()
	 */
	public IGraphNode getGraphNode() {
		return mGraphNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#isInGraph()
	 */
	public boolean isInGraph() {
		return mInsideGraph;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getTransformedBoundingVolume()
	 */
	public IBoundingVolume getTransformedBoundingVolume() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getScenePosition()
	 */
	public Vector3 getScenePosition() {
		return mPosition;
	}
}
