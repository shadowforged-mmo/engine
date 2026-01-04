package com.shadowforgedmmo.engine.instance

import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.math.Vector3

class RaycastHit<T : GameObject>(val gameObject: T, val point: Vector3)
