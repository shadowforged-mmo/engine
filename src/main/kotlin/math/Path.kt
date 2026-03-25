package com.shadowforgedmmo.engine.math

import com.fasterxml.jackson.annotation.JsonProperty

data class Path(@JsonProperty("corners") val corners: List<Vector3>)
